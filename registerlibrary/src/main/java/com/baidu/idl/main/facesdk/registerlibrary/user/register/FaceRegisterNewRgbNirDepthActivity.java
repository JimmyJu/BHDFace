package com.baidu.idl.main.facesdk.registerlibrary.user.register;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.BaseOrbbecActivity;
import com.example.datalibrary.api.FaceApi;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.DensityUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.CircleImageView;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.FaceRoundProView;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.PreviewTexture;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.LivenessModel;
import com.example.datalibrary.model.User;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;
import org.openni.android.OpenNIView;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * ?????????????????????????????????
 * Created by v_liujialu01 on 2020/02/19.
 */
public class FaceRegisterNewRgbNirDepthActivity extends BaseOrbbecActivity implements View.OnClickListener,
        OpenNIHelper.DeviceOpenListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = FaceRegisterNewRgbNirDepthActivity.class.getSimpleName();
    private static final int DEPTH_NEED_PERMISSION = 33;

    // RGB????????????????????????
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth????????????????????????
    private int depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private int depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();

    // nir????????????????????????
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private AutoTexturePreviewView mAutoTexturePreviewView;
    private FaceRoundProView mFaceRoundProView;
    private RelativeLayout mRelativePreview;     // ??????????????????

    // ??????????????????
    private RelativeLayout mRelativeCollectSuccess;
    private CircleImageView mCircleHead;
    private EditText mEditName;
    private TextView mTextError;
    private Button mBtnCollectConfirm;
    private ImageView mImageInputClear;

    // ??????Depth???
    private OpenNIView mDepthGLView;

    // ????????????????????????
    private RelativeLayout mRelativeRegisterSuccess;
    private CircleImageView mCircleRegSucHead;

    // ?????????????????????????????????x?????????y????????????width
    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mCropBitmap;
    private boolean mCollectSuccess = false;
    // ?????????????????????
    private int cameraType;
    // ???????????????
    private Device mDevice;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;
    // ???????????????????????????
    private boolean initOk = false;
    // ?????????????????????
    private volatile byte[] rgbData;
    private volatile byte[] depthData;
    private volatile byte[] irData;
    private Object sync = new Object();
    // ???????????????????????????
    private boolean exit = false;

    // ???????????????
    private int mCameraNum;
    // RGB+IR ??????
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;
    private TextureView irPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_new_registerlibrary_rgbnirdepth);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
        initView();
        FaceSDKManager.getInstance().setCropFace(true);
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    ToastUtils.toast(FaceRegisterNewRgbNirDepthActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRegisterNewRgbNirDepthActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    private void initView() {
        depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
        depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();
        mAutoTexturePreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoTexturePreviewView.setIsRegister(true);
        mFaceRoundProView = findViewById(R.id.round_view);
        mRelativePreview = findViewById(R.id.relative_preview);

        mRelativeCollectSuccess = findViewById(R.id.relative_collect_success);
        mCircleHead = findViewById(R.id.circle_head);
        mCircleHead.setBorderWidth(DensityUtils.dip2px(FaceRegisterNewRgbNirDepthActivity.this,
                3));
        mCircleHead.setBorderColor(Color.parseColor("#0D9EFF"));
        mEditName = findViewById(R.id.edit_name);
        mTextError = findViewById(R.id.text_error);
        mBtnCollectConfirm = findViewById(R.id.btn_collect_confirm);
        mBtnCollectConfirm.setOnClickListener(this);
        mImageInputClear = findViewById(R.id.image_input_delete);
        mImageInputClear.setOnClickListener(this);

        mRelativeRegisterSuccess = findViewById(R.id.relative_register_success);
        mCircleRegSucHead = findViewById(R.id.circle_reg_suc_head);
        findViewById(R.id.btn_return_home).setOnClickListener(this);
        findViewById(R.id.btn_continue_reg).setOnClickListener(this);

        ImageView imageBack = findViewById(R.id.image_register_back);
        imageBack.setOnClickListener(this);

        // ?????????????????????
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mImageInputClear.setVisibility(View.VISIBLE);
                    mBtnCollectConfirm.setEnabled(true);
                    mBtnCollectConfirm.setTextColor(Color.WHITE);
                    mBtnCollectConfirm.setBackgroundResource(R.drawable.button_selector);
                    List<User> listUsers = FaceApi.getInstance().getUserListByUserName(s.toString());
                    if (listUsers != null && listUsers.size() > 0) {     // ?????????????????????
                        mTextError.setVisibility(View.VISIBLE);
                        mBtnCollectConfirm.setEnabled(false);
                    } else {
                        mTextError.setVisibility(View.INVISIBLE);
                        mBtnCollectConfirm.setEnabled(true);
                    }
                } else {
                    mImageInputClear.setVisibility(View.GONE);
                    mBtnCollectConfirm.setEnabled(false);
                    mBtnCollectConfirm.setTextColor(Color.parseColor("#666666"));
                    mBtnCollectConfirm.setBackgroundResource(R.mipmap.btn_all_d);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDepthGLView = findViewById(R.id.open_view);
        mDepthGLView.setVisibility(View.INVISIBLE);

        irPreviewView = findViewById(R.id.ir_preview_view);
        // ????????????
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        }
        mPreview = new PreviewTexture[mCameraNum];
        mCamera = new Camera[mCameraNum];
        mPreview[1] = new PreviewTexture(this, irPreviewView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                // ?????????????????????
                startCameraPreview();
                // ????????? ???????????????
                mOpenNIHelper = new OpenNIHelper(this);
                mOpenNIHelper.requestDeviceOpen(this);
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                }else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParams = irPreviewView.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = w;
                    layoutParams.width = h;
                    // ??????90?????????270?????????????????????
                } else {
                    layoutParams.height = h;
                    layoutParams.width = w;
                }
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (!mCollectSuccess) {
                            dealIr(data);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Log.e(TAG, "start camera");
    }

    @Override
    protected void onStop() {
        super.onStop();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
            mOpenNIHelper = null;
        }
        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCameraNum >= 2) {
                    if (mCamera[i] != null) {
                        mCamera[i].setPreviewCallback(null);
                        mCamera[i].stopPreview();
                        mPreview[i].release();
                        mCamera[i].release();
                        mCamera[i] = null;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ???????????????
        CameraPreviewManager.getInstance().stopPreview();
        FaceSDKManager.getInstance().setCropFace(false);
        if (mCropBitmap != null) {
            if (!mCropBitmap.isRecycled()) {
                mCropBitmap.recycle();
            }
            mCropBitmap = null;
        }
    }

    /**
     * ?????????????????????
     */
    private void startCameraPreview() {
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }

        CameraPreviewManager.getInstance().startPreview(this, mAutoTexturePreviewView,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {

                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        Log.e(TAG, "data = " + data);
                        if (!mCollectSuccess) {
                            Log.e(TAG, "data1 = " + data);
                            dealRgb(data);
                        }
                    }
                });
    }

    private void dealDepth(byte[] data) {
        depthData = data;
        checkData();
    }

    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }

    private void dealIr(byte[] data) {
        irData = data;
        checkData();
    }

    /**
     * ?????????????????????
     */
    private void checkData() {
        if (mCollectSuccess) {
            return;
        }

        // ???????????????????????????????????????
        FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, depthData, RGB_HEIGHT,
                RGB_WIDTH, 4, 2, new FaceDetectCallBack() {
                    @Override
                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                        checkFaceBound(livenessModel);
                    }

                    @Override
                    public void onTip(int code, final String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mFaceRoundProView == null) {
                                    return;
                                }
                                mFaceRoundProView.setTipText("??????????????????????????????");
                                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
                            }
                        });
                    }

                    @Override
                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

                    }
                });
    }

    /**
     * ??????????????????
     *
     * @param livenessModel LivenessModel??????
     */
    private void checkFaceBound(final LivenessModel livenessModel) {
        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    mFaceRoundProView.setTipText("??????????????????????????????");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
                    return;
                }

                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);

                if (livenessModel.getFaceSize() > 1){
                    mFaceRoundProView.setTipText("???????????????????????????????????????");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    return;
                }

                mPointXY[0] = livenessModel.getFaceInfo().centerX;   // ??????X??????
                mPointXY[1] = livenessModel.getFaceInfo().centerY;   // ??????Y??????
                mPointXY[2] = livenessModel.getFaceInfo().width;     // ????????????
                mPointXY[3] = livenessModel.getFaceInfo().height;    // ????????????

                FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, mAutoTexturePreviewView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);

                float leftLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
                float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
                float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
                float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;
                float previewWidth = AutoTexturePreviewView.circleRadius * 2;

                Log.e(TAG, "faceX = " + mPointXY[0] + ", faceY = " + mPointXY[1]
                        + ", faceW = " + mPointXY[2] + ", prw = " + previewWidth);
//                Log.e(TAG, "leftLimitX = " + leftLimitX + ", rightLimitX = " + rightLimitX
//                        + ", topLimitY = " + topLimitY + ", bottomLimitY = " + bottomLimitY);
//                Log.e(TAG, "cX = " + AutoTexturePreviewView.circleX + ", cY = " + AutoTexturePreviewView.circleY
//                        + ", cR = " + AutoTexturePreviewView.circleRadius);

                if (mPointXY[2] < 50 || mPointXY[3] < 50) {
                    mFaceRoundProView.setTipText("????????????????????????????????????");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // ????????????
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[2] > previewWidth || mPointXY[3] > previewWidth) {
                    mFaceRoundProView.setTipText("????????????????????????????????????");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // ????????????
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                        || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                        || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                        || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
                    mFaceRoundProView.setTipText("????????????????????????????????????");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // ????????????
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                mFaceRoundProView.setTipText("??????????????????????????????");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                // ??????????????????
                checkLiveScore(livenessModel);
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param livenessModel LivenessModel??????
     */
    private void checkLiveScore(LivenessModel livenessModel) {
        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
            mFaceRoundProView.setTipText("??????????????????????????????");
            return;
        }

        float rgbLivenessScore = livenessModel.getRgbLivenessScore();
        float depthLiveScore = livenessModel.getDepthLivenessScore();
        float irLivenessScore = livenessModel.getIrLivenessScore();
        float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        float depthLiveThreadHold = SingleBaseConfig.getBaseConfig().getDepthLiveScore();
        float liveIrThreadHold = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        Log.e(TAG, "score = " + rgbLivenessScore);
        if (! livenessModel.isQualityCheck()){
            mFaceRoundProView.setTipText("????????????????????????????????????");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
            return;
        } else if (rgbLivenessScore < liveThreadHold || depthLiveScore < depthLiveThreadHold
                || irLivenessScore < liveIrThreadHold) {
            mFaceRoundProView.setTipText("??????????????????????????????");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
            // ????????????
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }
        // ???????????????
        getFeatures(livenessModel);
    }

    /**
     * ???????????????
     *
     * @param model ????????????
     */
    private void getFeatures(final LivenessModel model) {
        if (model == null) {
            return;
        }

        float ret = model.getFeatureCode();
        displayCompareResult(ret, model.getFeature(), model);
    }

    // ??????????????????????????? ????????????
    private void displayCompareResult(float ret, byte[] faceFeature, LivenessModel model) {
        if (model == null) {
            mFaceRoundProView.setTipText("??????????????????????????????");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
            return;
        }

        // ??????????????????
        if (ret == 128) {
            // ??????
            BDFaceImageInstance cropInstance = model.getBdFaceImageInstanceCrop();
            if (cropInstance == null) {
                mFaceRoundProView.setTipText("????????????");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                return;
            }
            mCropBitmap = BitmapUtils.getInstaceBmp(cropInstance);
            // ????????????
            if (mCropBitmap != null) {
                mCollectSuccess = true;
                mCircleHead.setImageBitmap(mCropBitmap);
            }
            cropInstance.destory();

            mRelativeCollectSuccess.setVisibility(View.VISIBLE);
            mRelativePreview.setVisibility(View.GONE);
            mFaceRoundProView.setTipText("");

            for (int i = 0; i < faceFeature.length; i++) {
                mFeatures[i] = faceFeature[i];
            }
        } else {
            mFaceRoundProView.setTipText("??????????????????");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
        }
    }

    /**
     * ????????????
     *
     * @param imageInstance
     */
    private void destroyImageInstance(BDFaceImageInstance imageInstance) {
        if (imageInstance != null) {
            imageInstance.destory();
        }
    }

    @Override
    public void onDeviceOpened(UsbDevice usbDevice) {
        initUsbDevice(usbDevice);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
                int fps = mode.getFps();
                if (cameraType == 1) {
                    if (x == depthHeight && y == depthWidth && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                } else {
                    if (x == depthWidth && y == depthHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                }

            }
            startThread();
        }
    }

    @Override
    public void onDeviceOpenFailed(String s) {
        Log.e(TAG, "device error = " + s);
        showAlertAndExit("Open Device failed: " + s);
    }

    @Override
    public void onDeviceNotFound() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Grant",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    /**
     * ???device ?????????????????????USB??????
     */
    private void initUsbDevice(UsbDevice device) {
        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices",
                    Toast.LENGTH_LONG).show();
            return;
        }
        this.mDevice = null;
        // Find mDevice ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.mDevice = Device.open();
                break;
            }
        }

        if (this.mDevice == null) {
            Toast.makeText(this, " openni open devices failed: "
                    + device.getDeviceName(), Toast.LENGTH_LONG).show();
            return;
        }
    }

    /**
     * ??????????????????????????????
     */
    private void startThread() {
        initOk = true;
        thread = new Thread() {
            @Override
            public void run() {
                List<VideoStream> streams = new ArrayList<VideoStream>();
                streams.add(mDepthStream);
                mDepthStream.start();
                while (!exit) {
                    try {
                        OpenNI.waitForAnyStream(streams, 2000);
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }
                    synchronized (sync) {
                        if (mDepthStream != null) {
                            mDepthGLView.update(mDepthStream);
                            VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                            ByteBuffer depthByteBuf = videoFrameRef.getData();
                            if (depthByteBuf != null) {
                                int depthLen = depthByteBuf.remaining();
                                byte[] depthByte = new byte[depthLen];
                                depthByteBuf.get(depthByte);
                                dealDepth(depthByte);
                            }
                            videoFrameRef.release();
                        }
                    }

                }
            }
        };
        thread.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.image_register_back) {    // ??????
            finish();
        } else if (id == R.id.btn_collect_confirm) {   // ???????????????
            String userName = mEditName.getText().toString();
//                if (TextUtils.isEmpty(userName)) {
//                    ToastUtils.toast(getApplicationContext(), "?????????????????????");
//                    return;
//                }
//                if (userName.length() > 10) {
//                    ToastUtils.toast(getApplicationContext(), "???????????????????????????10???");
//                    return;
//                }
            // ????????????
            String nameResult = FaceApi.getInstance().isValidName(userName);
            if (!"0".equals(nameResult)) {
                ToastUtils.toast(getApplicationContext(), nameResult);
                return;
            }
            String imageName = userName + ".jpg";
            // ??????????????????
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(null,
                    userName, imageName, null, mFeatures);
            if (isSuccess) {
                // ??????????????????
                File faceDir = FileUtils.getBatchImportSuccessDirectory();
                File file = new File(faceDir, imageName);
                FileUtils.saveBitmap(file, mCropBitmap);
                // ???????????????????????????
//                FaceSDKManager.getInstance().initDatabases();
                // ??????UI
                mRelativeCollectSuccess.setVisibility(View.GONE);
                mRelativeRegisterSuccess.setVisibility(View.VISIBLE);
                mCircleRegSucHead.setImageBitmap(mCropBitmap);
            } else {
                ToastUtils.toast(getApplicationContext(), "????????????????????????" +
                        "?????????????????????????????????");
            }
        } else if (id == R.id.btn_continue_reg) {      // ????????????
            if (mRelativeRegisterSuccess.getVisibility() == View.VISIBLE) {
                mRelativeRegisterSuccess.setVisibility(View.GONE);
            }
            mRelativePreview.setVisibility(View.VISIBLE);
            mFaceRoundProView.setTipText("");
            mCollectSuccess = false;
            mEditName.setText("");
        } else if (id == R.id.btn_return_home) {       // ????????????
            // ???????????????
            CameraPreviewManager.getInstance().stopPreview();
            finish();
        } else if (id == R.id.image_input_delete) {   // ????????????
            mEditName.setText("");
            mTextError.setVisibility(View.INVISIBLE);
        }
    }
}
