package com.baidu.idl.main.facesdk.registerlibrary.user.register;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.BaseActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.FaceFeatureCallBack;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.registerlibrary.user.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceTrackManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.DensityUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.KeyboardsUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.CircleImageView;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.FaceRoundProView;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.LivenessModel;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.User;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ?????????????????????
 * Created by v_liujialu01 on 2020/02/19.
 */
public class FaceRegisterNewActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = FaceRegisterNewActivity.class.getSimpleName();

    // RGB????????????????????????
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

    // ????????????????????????
    private RelativeLayout mRelativeRegisterSuccess;
    private CircleImageView mCircleRegSucHead;

    // ?????????????????????????????????x?????????y????????????width
    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mCropBitmap;
    private boolean mCollectSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_new_registerlibrary);
        initView();
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
                    ToastUtils.toast(FaceRegisterNewActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRegisterNewActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    private void initView() {
        mAutoTexturePreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoTexturePreviewView.setIsRegister(true);
        mFaceRoundProView = findViewById(R.id.round_view);
        mRelativePreview = findViewById(R.id.relative_preview);

        mRelativeCollectSuccess = findViewById(R.id.relative_collect_success);
        mCircleHead = findViewById(R.id.circle_head);
        mCircleHead.setBorderWidth(DensityUtils.dip2px(FaceRegisterNewActivity.this, 3));
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ?????????????????????
        startCameraPreview();
        Log.e(TAG, "start camera");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ???????????????
        CameraPreviewManager.getInstance().stopPreview();
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
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        }
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, mAutoTexturePreviewView,
                PREFER_WIDTH, PERFER_HEIGHT, new CameraDataCallback() {

                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        if (mCollectSuccess) {
                            return;
                        }
                        // ?????????????????????
                        faceDetect(data, width, height);
                    }
                });
    }

    /**
     * ?????????????????????
     */
    private void faceDetect(byte[] data, final int width, final int height) {
        if (mCollectSuccess) {
            return;
        }

        // ???????????????????????????????????????
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // int liveType = 2;

        if (liveType == 0) { // ???????????????
            FaceTrackManager.getInstance().setAliving(false);
        } else if (liveType == 1) { // ????????????
            FaceTrackManager.getInstance().setAliving(true);
        }

        // ???????????????????????????????????????
        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                checkFaceBound(livenessModel);
            }

            @Override
            public void onTip(final int code, final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFaceRoundProView == null) {
                            return;
                        }
                        if (code == 0){
                            mFaceRoundProView.setTipText("??????????????????????????????");
                            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
                        }else {
                            mFaceRoundProView.setTipText("????????????????????????????????????");
                            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                        }
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
                if (mCollectSuccess) {
                    return;
                }

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

//                Log.e(TAG, "faceX = " + mPointXY[0] + ", faceY = " + mPointXY[1]
//                        + ", faceW = " + mPointXY[2] + ", prw = " + previewWidth);
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

//                if ((Math.abs(AutoTexturePreviewView.circleX - mPointXY[0]) < mPointXY[2] / 2)
//                        && (Math.abs(AutoTexturePreviewView.circleY - mPointXY[1]) < mPointXY[2] / 2)
//                        && (mPointXY[2] <= previewWidth && mPointXY[3] <= previewWidth)) {
//
//                }
                mFaceRoundProView.setTipText("??????????????????????????????");
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

        // ??????????????????
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // int liveType = 2;

        if (! livenessModel.isQualityCheck()){
            mFaceRoundProView.setTipText("????????????????????????????????????");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
            return;
        } else if (liveType == 0) {         // ?????????
            getFeatures(livenessModel);
        } else if (liveType == 1) { // RGB????????????
            float rgbLivenessScore = livenessModel.getRgbLivenessScore();
            float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            // Log.e(TAG, "score = " + rgbLivenessScore);
            if (rgbLivenessScore < liveThreadHold) {
                mFaceRoundProView.setTipText("??????????????????????????????");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                // ????????????
                destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                return;
            }
            // ???????????????
            getFeatures(livenessModel);
        }
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

        // ?????????????????????????????????
        int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
        if (modelType == 1) {
            // ?????????
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(final float featureSize, final byte[] feature, long time) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCollectSuccess) {
                                        return;
                                    }
                                    displayCompareResult(featureSize, feature, model);
                                    Log.e(TAG, String.valueOf(feature.length));
                                }
                            });

                        }
                    });
        }
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
            BDFaceImageInstance imageInstance = model.getBdFaceImageInstanceCrop();
            AtomicInteger isOutoBoundary = new AtomicInteger();
            BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                    .cropFaceByLandmark(imageInstance, model.getLandmarks(),
                            2.0f, false, isOutoBoundary);
            if (cropInstance == null) {
                mFaceRoundProView.setTipText("????????????");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                // ????????????
                destroyImageInstance(model.getBdFaceImageInstanceCrop());
                return;
            }
            mCropBitmap = BitmapUtils.getInstaceBmp(cropInstance);
            // ????????????
            if (mCropBitmap != null) {
                mCollectSuccess = true;
                mCircleHead.setImageBitmap(mCropBitmap);
            }
            cropInstance.destory();
            // ????????????
            destroyImageInstance(model.getBdFaceImageInstanceCrop());

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
            mCollectSuccess = false;
            mFaceRoundProView.setTipText("");
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

    /**
     * ?????????????????????????????????
     * ??????????????????
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
