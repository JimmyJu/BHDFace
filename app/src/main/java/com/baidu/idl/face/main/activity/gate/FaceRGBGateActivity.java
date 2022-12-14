package com.baidu.idl.face.main.activity.gate;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.api.Wiegand;
import com.baidu.idl.face.main.callback.AnalyzeQRCodeCallback;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.gatecamera.AutoTexturePreviewView;
import com.baidu.idl.face.main.gatecamera.CameraPreviewManager;
import com.baidu.idl.face.main.listener.DoubleClickListener;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.setting.GateSettingActivity;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.ByteUtils;
import com.baidu.idl.face.main.utils.DateUtil;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.IBeaconAccept;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.LogUtilsDynamic;
import com.baidu.idl.face.main.utils.NavigationBarUtil;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.ScanUtils;
import com.baidu.idl.face.main.utils.SoundPoolUtil;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.UdpMessageTool;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.user.callback.RemoveStaffCallback;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.ImportFeatureResult;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.model.QR;
import com.example.datalibrary.model.User;
import com.example.yfaceapi.GPIOManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


public class FaceRGBGateActivity extends BaseActivity {

    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private Context mContext;

    private TextureView mDrawDetectFaceView;

    private RectF rectF;
    private Paint paint;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;

    private GPIOManager mGPIOManager;
    private Wiegand mWiegand;
    //???????????????
    private int RedLight_Status = 0;
    private int GreenLight_Status = 0;
    //???????????????
    private String timeFlag;
    private String timeFlag1;

    //???????????????
    private String startTime;
    private String endTime;
    private int beginHour, beginMin, endHour, endMin;
    //???????????????????????????
    private boolean timeFlagBool = true;

    //???????????????
    private boolean relayFlag = false;

    // //??????mp3
    private SoundPool mSoundPool = null;
    private final HashMap<Integer, Integer> soundID = new HashMap<>();
    private float volume;
    private SoundPool.Builder spBuilder;

    private Hashtable<String, Long> faceTime = new Hashtable<>();

    // ????????????????????????????????????x?????????y????????????width
    private RelativeLayout textHuanying;
    private ImageView nameImage;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private Paint paintBg;
    //    private View view;
//    private TextView logoText;
    private User mUser;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private DoubleClickListener doubleClickListener;

    private SoundPoolUtil mSoundPoolUtil;

    //??????????????????
    private boolean cancelFlag = true;

    private boolean qrFlag = false;
    private boolean blFlag = false;

    //??????????????????
    private String settingPassword = "123456";

    /**
     * ibeacon???????????????:
     * ????????????ibeacon???????????????????????????????????????????????????????????????????????????
     */
//    private static final int SIGNAL_STRENGTH = 70;

    /**
     * ????????????
     */
    private BluetoothLeScanner mBLEScanner;
    /**
     * ??????Adapter
     */
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothManager mBluetoothManager;

    //-------------------------------?????????????????????start--------------------------------------------
    /**
     * ??????
     */
    private byte[] cardNumberByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**
     * ???????????? 20?????????
     */
    private byte[] personnelNameByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * ???????????????
     */
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    /**
     * ???????????????
     */
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};
    /**
     * ?????????????????????
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private Bitmap mRBmp = null;
    /**
     * ????????????????????????
     */
    private boolean faceFlag = false;

    /**
     * ?????????????????? handler
     */
    private Handler handler = new Handler();

    /**
     * ????????????View ??????????????????????????????????????????????????????????????????????????????
     */
    private TextView mLiveTextView, mAdoptTextView, mErrorTextView, mSendTextView, mServerStateTextView;
    private int mLiveNum = 1, mAdoptNum = 1, mErrorNum = 1;

    //????????????????????? false: 21       true:22(???????????????)
    private Boolean switchPortNum = false;
    int flag = 0;

    //-------------------------------------?????????????????????end----------------------------------------

    //-------------------------------------??????udp??????start----------------------------------------

    //sendByte1 + ??????ID???2???
    private byte[] sendByte1 = new byte[]{(byte) 0x81, (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x80, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    //sendByte2 + ???????????????16???
    private byte[] sendByte2 = new byte[]{(byte) 0x14, (byte) 0x00, (byte) 0x01};

    //???????????????1??? +sendByte3
    private byte[] sendByte3 = new byte[]{(byte) 0x00};

    //???????????? + sendByte4
    private byte[] sendByte4 = new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0xFF};

    //?????????????????????
    private final byte[] faceAddress = new byte[]{(byte) 0x00, (byte) 0x45};

    //????????????????????????
    private final byte[] qrCodeAddress = new byte[]{(byte) 0x00, (byte) 0x46};

    //??????????????????
    private final byte[] deleteAddress = new byte[]{(byte) 0x00, (byte) 0x47};

    //?????????????????????
    private final byte[] heartbeatAddress = new byte[]{(byte) 0x00, (byte) 0x48};

    //????????????????????????
    private final byte[] deleteSingleAddress = new byte[]{(byte) 0x00, (byte) 0x49};

    //???????????????4???
    private final byte[] dataLength = new byte[]{0x00, 0x00, 0x00, 0x00};

    //???????????????
    private final byte[] response = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, 0x55, 0x00, 0x45};
    //??????????????????
    private final byte[] qrCodeResponse = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, 0x55, 0x00, 0x46};
    //?????????????????????
    private final byte[] deleteResponse = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, 0x55, 0x00, 0x47, 0x00, 0x00, 0x00, 0x00};
    //?????????
    private final byte[] heartbeatResponse = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, 0x55, 0x00, 0x48, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    //???????????????????????????
    private final byte[] deleteSingleResponse = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, 0x55, 0x00, 0x49};

    private final byte[] ok = new byte[]{0x4F, 0x4B, 0x00, 0x00};//????????????
    private final byte[] er = new byte[]{0x45, 0x52, 0x00, 0x00};//????????????
    private final byte[] face_er = new byte[]{0x41, 0x51, 0x00, 0x00};//??????????????????????????????
    private final byte[] qr_er = new byte[]{0x42, 0x52, 0x00, 0x00};//?????????????????????
    private final byte[] delete_er = new byte[]{0x43, 0x53, 0x00, 0x00};//??????????????????
    private final byte[] packet_loss_er = new byte[]{0x44, 0x54, 0x00, 0x00};//????????????

    private int terminalId = 0;

    private int floorId = 0;

    private UdpMessageTool mUdpMessageTool = null;
    // ??????ip
    private String HOST = "192.168.0.81";
    // ???????????????
    private int PORT = 8899;
    //???????????????
    private int RECEIVE_PORT = 8980;
    //???????????????????????????
    private boolean isRunning = true;

    private DatagramSocket receiveSocket;
    private InetAddress serverAddr;
    //    private byte[] receiveInfo;     //??????????????????
    private DatagramSocket sendSocket = null;

    private UdpReceiveThread mUdpReceiveThread;

    //-------------------------------------??????udp??????end------------------------------------------


    //???????????????
    private int faceCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mGPIOManager = GPIOManager.getInstance(mContext);
        mWiegand = Wiegand.getInstance();

        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_gate);
        initView();
        //???????????????
        initSP();

        //????????????
        liveDataBus();

        //???????????????
        initData();

        //????????????
        if (SingleBaseConfig.getBaseConfig().getLightSwitch() == 1) {
            monitorTimestamp();
        } else if (SingleBaseConfig.getBaseConfig().getLightSwitch() == 0) {
            mGPIOManager.pullDownWhiteLight();
        }

        //??????BLE??????
        if (SingleBaseConfig.getBaseConfig().getBluetoothSwitch() == 1) {
            scanLeDevice();
        }

        //??????Activity???????????????????????????
        if (SingleBaseConfig.getBaseConfig().getVisitorSwitch() == 1) {
            mUdpReceiveThread = new UdpReceiveThread();
            mUdpReceiveThread.start();
        }

        // ????????????
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // ????????????
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // ?????????????????????????????????
        if (displayHeight < displayWidth) {
            // ?????????
            int height = displayHeight;
            // ?????????
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // ????????????????????????
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // ??????????????????
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        int rbgCameraId = SingleBaseConfig.getBaseConfig().getRBGCameraId();
        Log.e("TAG", "rbgCameraId---------------: " + rbgCameraId);
    }

    /**
     * ??????????????? ?????????????????????
     */
    private void monitorTimestamp() {
        new Thread(() -> {
            while (timeFlagBool) {
                try {
                    Thread.sleep(1000);
                    if (DateUtil.atTheCurrentTime(beginHour, beginMin, endHour, endMin)) {
                        //true???????????????   ??????false
                        mGPIOManager.pullUpWhiteLight();
                    } else {
                        mGPIOManager.pullDownWhiteLight();
                    }
                   /* if (DateUtil.getTimeShort().equals(startTime)) {
//                        Log.d("TAG", "????????????: " + DateUtil.getTimeShort());
                        manager.pullUpWhiteLight();
                    }

                    if (DateUtil.getTimeShort().equals(endTime)) {
//                        Log.d("TAG", "????????????: " + DateUtil.getTimeShort());
                        manager.pullDownWhiteLight();
                    }*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initData() {
        //????????????????????????
        mSoundPoolUtil = new SoundPoolUtil();
        mSoundPoolUtil.loadDefault(this);

        //?????????????????????????????????
        String light_start = (String) SPUtils.get(this, "light_start", "");
        String light_end = (String) SPUtils.get(this, "light_end", "");

        if (light_start != null && light_end != null) {
            startTime = light_start;
            endTime = light_end;
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                beginHour = Integer.parseInt(startTime.substring(0, startTime.indexOf(":")));
                beginMin = Integer.parseInt(startTime.substring(startTime.lastIndexOf(":") + 1));
                endHour = Integer.parseInt(endTime.substring(0, endTime.indexOf(":")));
                endMin = Integer.parseInt(endTime.substring(endTime.lastIndexOf(":") + 1));
                Log.e("TAG", "????????????: " + beginHour + ":" + beginMin + "----" + endHour + ":" + endMin);
            }
        }

        //????????????????????????
        String ed_settingPassword = (String) SPUtils.get(this, "setting_psw", "");
        if (!ed_settingPassword.isEmpty()) {
            settingPassword = ed_settingPassword;
        }
        SPUtils.put(this, "setting_psw", settingPassword);
        Log.i("TAG", "settingPws" + settingPassword);
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
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
                    FaceSDKManager.initModelSuccess = true;
                    ToastUtils.toast(mContext, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(mContext, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    /**
     * View
     */
    private void initView() {
        // ??????????????????
        relativeLayout = findViewById(R.id.all_relative);
        // ????????????
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            mDrawDetectFaceView.setRotationY(180);
        }
        // ???????????????RGB ????????????
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);

        //??????logo
//        logoText = findViewById(R.id.logo_text);
//        logoText.setVisibility(View.VISIBLE);

        // ??????logo
        ImageView switchPort = findViewById(R.id.switchPort);
        switchPort.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                switch (flag) {
                    case 0:
                        switchPortNum = true;
                        flag = 1;
                        showExitDialog("????????????");
                        break;
                    case 1:
                        switchPortNum = false;
                        flag = 0;
                        showExitDialog("????????????");
                        break;
                }
            }
        });


        // ??????
        Button mBtSetting = findViewById(R.id.menu_btn);
        doubleClickListener = new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                mBtSetting.setAlpha(1);
                mBtSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBtSetting.getAlpha() == 1) {
                            mBtSetting.setAlpha(0);
                            //????????????
                            setUpDialog();
                        }
                        mBtSetting.setOnClickListener(doubleClickListener);
                    }
                });
            }
        };

        mBtSetting.setOnClickListener(doubleClickListener);


        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        Log.e("TAG", "????????????: " + mLiveType);
        // ***************????????????*************
        textHuanying = findViewById(R.id.huanying_relative);
        userNameLayout = findViewById(R.id.user_name_layout);
        nameImage = findViewById(R.id.name_image);
        nameText = findViewById(R.id.name_text);
//        view = findViewById(R.id.mongolia_view);
//        view.setAlpha(0.85f);
//        view.setBackgroundColor(Color.parseColor("#ffffff"));

        //?????? view
        mLiveTextView = findViewById(R.id.live);
        mAdoptTextView = findViewById(R.id.adopt);
        mErrorTextView = findViewById(R.id.error);
        mSendTextView = findViewById(R.id.send);
        mServerStateTextView = findViewById(R.id.server);

    }


    private void liveDataBus() {
        LiveDataBus.get().with("heart", Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean heart) {
                if (heart) {
                    mServerStateTextView.setText("??????");
                } else {
                    mServerStateTextView.setText("??????");
                }
            }
        });

        LiveDataBus.get().with("sendNum", Integer.class).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer num) {
                mSendTextView.setText("" + num);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
        //??????????????????
        timeFlag = DateUtil.timeStamp();

        //QRcode????????????
        FaceSDKManager.getInstance().setAnalyzeQRCodeCallback(mAnalyzeQRCodeCallback);
        timeFlag1 = DateUtil.timeStamp();

        // TODO ??? ????????????
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // ???????????????????????????????????????
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
                                    @Override
                                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                                        // ????????????
//                                        Log.e("TAG", "?????????: " + (Long.parseLong(DateUtil.timeStamp()) - Long.parseLong(timeFlag)));
                                        if (Long.parseLong(DateUtil.timeStamp()) - Long.parseLong(timeFlag) > 1) {
                                            timeFlag = DateUtil.timeStamp();
                                            checkCloseDebugResult(livenessModel, width, height);
                                        }

                                    }

                                    @Override
                                    public void onTip(int code, String msg) {
                                    }

                                    @Override
                                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                        // ???????????????
                                        showFrame(livenessModel);
                                    }
                                });
                    }
                });
    }

    // ***************????????????????????????*************
    private void checkCloseDebugResult(final LivenessModel livenessModel, int width, int height) {
        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //|| livenessModel.getFaceInfo() == null || !faceSizeFilter(livenessModel.getFaceInfo(), width, height)
                if (livenessModel == null) {

                    //???????????????
                    try {
                        delayClose_Red_GedreenLight();
                        RedLight_Status = 0;
                        GreenLight_Status = 0;
                    } catch (Exception e) {
                        Log.e("loge", "run:----- delayClose_Red_GedreenLight----??????" + e.toString());
                    }


                   /* // ???????????????????????????????????????????????????"alpha",?????????????????????
                    // ???????????????,?????????????????????view?????????
                    if (isTime) {
                        isTime = false;
                        startTime = System.currentTimeMillis();
                    }
                    detectCount = true;
                    long endTime = System.currentTimeMillis() - startTime;

                    if (endTime < 10000) {
                        textHuanying.setVisibility(View.VISIBLE);
                        userNameLayout.setVisibility(View.GONE);
                        return;
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }

                    textHuanying.setVisibility(View.VISIBLE);
                    userNameLayout.setVisibility(View.GONE);
                    return;*/

//                    textHuanying.setVisibility(View.VISIBLE);
//                    userNameLayout.setVisibility(View.GONE);
                }
                /*isTime = true;
                if (detectCount) {
                    detectCount = false;
                    objectAnimator();
                } else {
                    view.setVisibility(View.GONE);
                }*/

                else {

                    //????????????
                    try {
                        mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                    } catch (OutOfMemoryError e) {
                        ToastUtils.toast(FaceRGBGateActivity.this, "" + e.toString());
                    }

                    //????????????
                    mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
                    byteArrayOutputStream.reset();

                    if (mRBmp != null && !mRBmp.isRecycled()) {
                        // ??????????????????null
                        mRBmp.recycle();
                        mRBmp = null;
                    }

                    User user = livenessModel.getUser();
//                float score = user.getScore();
                    if (user == null) {
                        //??????????????? ??????
                        try {
                            if (GreenLight_Status == 1) {
                                mGPIOManager.pullDownGreenLight();
                                GreenLight_Status = 0;
                            }
                            delayRedLight();
                        } catch (Exception e) {
                            Log.e("loge", "run:----- pullDownGreenLight-pullUpRedLight----??????" + e.toString());
                        }


                        mUser = null;
                        if (livenessModel.isMultiFrame()) {
                            textHuanying.setVisibility(View.GONE);
                            userNameLayout.setVisibility(View.VISIBLE);
                            nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
                            nameText.setTextColor(Color.parseColor("#fec133"));
                            nameText.setText("?????? ???????????????");

                            //??????3????????????????????????????????????
                            delaySendData(livenessModel, imageData, null);

                            //??????????????????
                            sendSerialPortData(null);
                        } else {
//                            textHuanying.setVisibility(View.VISIBLE);
//                            userNameLayout.setVisibility(View.GONE);
                        }
                    } else {
                        //???????????????  ??????
                        try {
                            if (RedLight_Status == 1) {
                                mGPIOManager.pullDownRedLight();
                                RedLight_Status = 0;
                            }
                            delayGreenLight();
                        } catch (Exception e) {
                            Log.e("loge", "run:----- pullDownRedLight-pullUpGreenLight----??????" + e.toString());
                        }
                        String userName = user.getUserName();
                        String userName_data = (String) SPUtils.get(FaceRGBGateActivity.this, "userName", "");
                        flag_same = userName.equals(userName_data);

                        if (!flag_same) {
                            flag_same = true;
                            SPUtils.put(FaceRGBGateActivity.this, "userName", userName);
                            LogUtilsDynamic.i("---->" + user.getUserName().trim(), "????????????" + faceCount++ +
                                    "  ????????????: " + livenessModel.getFeatureScore());

                        }
                        mUser = user;
                        textHuanying.setVisibility(View.GONE);
                        userNameLayout.setVisibility(View.VISIBLE);
                        nameImage.setImageResource(R.mipmap.ic_tips_gate_success);
                        nameText.setTextColor(Color.parseColor("#0dc6ff"));
                        nameText.setText(FileUtils.spotString(user.getUserName()) + " ?????????");

                        //??????3???????????????????????????????????????????????????
                        delaySendData(livenessModel, imageData, user);

                        //??????????????????
                        sendSerialPortData(user);
                    }

                }
            }
        });
    }

    //?????????????????????
    private boolean flag_same = false;


    //???????????????
    private void hintView(String text) {
        textHuanying.setVisibility(View.GONE);
        userNameLayout.setVisibility(View.VISIBLE);
        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
        nameText.setTextColor(Color.parseColor("#fec133"));
        nameText.setText(text);
    }

    /**
     * ???????????????
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));

                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                // ???????????????
                FaceOnDrawTexturViewUtil.drawFaceColor(mUser, paint, paintBg, model);
                // ???????????????
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    // ????????????
    /*private void objectAnimator() {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 0.85f);
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator.cancel();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        });
        animator.start();
    }*/

    /**
     * ????????????
     */
    private void initSP() {
        if (null != mSoundPool) {
            Log.e("TAG", "initSP:--------??????-----");
            mSoundPool.release();
        }
        if (null == mSoundPool) {
            if (Build.VERSION.SDK_INT > 21) {
                if (null == spBuilder) {
                    Log.e("TAG", "onStart: ???????????????");
                    spBuilder = new SoundPool.Builder();
                    //??????????????????
                    spBuilder.setMaxStreams(1);
                    //AudioAttributes??????????????????????????????????????????
                    AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
                    //?????????????????????????????????
                    attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//STREAM_MUSIC
                    //????????????AudioAttributes
                    spBuilder.setAudioAttributes(attrBuilder.build());
                }
                mSoundPool = spBuilder.build();

            } else {
                mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
            }
//            if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
            soundID.put(1, mSoundPool.load(this, R.raw.unregistered, 1));
            soundID.put(2, mSoundPool.load(this, R.raw.success, 1));
            soundID.put(3, mSoundPool.load(this, R.raw.info, 1));//??????????????????
//                soundID.put(4, mSoundPool.load(this, R.raw.error_info, 1));//??????--?????????
//                soundID.put(5, mSoundPool.load(this, R.raw.face_verify, 1));//??????--????????????
//                soundID.put(6, mSoundPool.load(this, R.raw.qrcode_verify, 1));//??????--???????????????
//                soundID.put(7, mSoundPool.load(this, R.raw.bluetooth_verify, 1));//??????--????????????
//                soundID.put(8, mSoundPool.load(this, R.raw.success_verify, 1));//??????--??????????????????
//            }
            AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volume = streamVolumeCurrent / streamVolumeMax;
            // ????????????????????????
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    if (mSoundPool != null) {
                        Log.e("TAG", "??????????????????");
                    }
                }
            });
        }
    }


    /**
     * ?????????????????? ?????? /rs485
     */
    private void sendSerialPortData(User user) {
        if (user == null) {
//            Log.e("TAG", "????????????: " + switchPortNum);
            if (faceTime.containsKey("fail")) {
                Long oldTime = faceTime.get("fail");
                Long newTime = System.currentTimeMillis();
                if (newTime - oldTime > 3000) {
                    faceTime.put("fail", newTime);
                    mErrorTextView.setText("" + mErrorNum++);
                    mLiveTextView.setText("" + mLiveNum++);
//                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //????????????
                    if (null != mSoundPool) {
                        mSoundPool.play(soundID.get(1), volume, volume, 0, 0, 1);
                    }
//                    }
                }
            } else {
                Long time = System.currentTimeMillis();
                faceTime.put("fail", time);
                mErrorTextView.setText("" + mErrorNum++);
                mLiveTextView.setText("" + mLiveNum++);
//                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                //????????????
                if (null != mSoundPool) {
                    mSoundPool.play(soundID.get(1), volume, volume, 0, 0, 1);
                }
//                }
            }
        } else {
            if (faceTime.containsKey(user.getUserName())) {
                Long oldTime = faceTime.get(user.getUserName());
                Long newTime = System.currentTimeMillis();
                if (newTime - oldTime > 3000) {
                    faceTime.put(user.getUserName(), newTime);
                    //????????????
                    wiegandOutput34(Utils.addZero(user.getUserInfo()), user.getImageName());
//                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //????????????
                    if (null != mSoundPool) {
                        mSoundPool.play(soundID.get(2), volume, volume, 0, 0, 1);
                    }
//                    }
                    mLiveTextView.setText("" + mLiveNum++);
                    mAdoptTextView.setText("" + mAdoptNum++);
                }
            } else {
                Long time = System.currentTimeMillis();
                faceTime.put(user.getUserName(), time);
                //????????????
                wiegandOutput34(Utils.addZero(user.getUserInfo()), user.getImageName());
//                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                //????????????
                if (null != mSoundPool) {
                    mSoundPool.play(soundID.get(2), volume, volume, 1, 0, 1);
                }
//                }
                mAdoptTextView.setText("" + mAdoptNum++);
                mLiveTextView.setText("" + mLiveNum++);
            }
        }
    }


    /**
     * ??????3???????????????????????????????????????????????????
     *
     * @param livenessModel ??????
     * @param imageData     ????????????
     * @param user          user = null ???????????? , user != null ????????????
     */
    private void delaySendData(LivenessModel livenessModel, byte[] imageData, User user) {
        if (!faceFlag) {

            faceFlag = true;

            handler.postDelayed(() -> {

                if (livenessModel.getFeature() != null) {
                    if (user == null) {
                        LiveDataBus.get().with("switchPort").postValue(switchPortNum);
                        //?????????????????????????????????
                        byte[] registerData = Utils.concat(
                                //??????????????????????????????
                                Utils.concat(livenessModel.getFeature(), cardNumberByte),
                                //???????????????????????????????????????
                                Utils.concat(personnelNameByte, imageData)
                        );
                        LiveDataBus.get().with("registerData").postValue(registerData);
                    } else {
                        //?????? ??????????????????????????????????????????
                        try {
                            byte[] registerData = Utils.addBytes(
                                    //?????? ???????????????
                                    Utils.concat(livenessModel.getFeature(), Utils.hexString2Bytes(user.getUserInfo())),
                                    //??????
                                    Utils.addZero3(user.getUserName()).getBytes("GB2312"),
                                    //??????
                                    imageData
                            );
                            LiveDataBus.get().with("registerData").postValue(registerData);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                faceFlag = false;

            }, 3000);
        }
    }

    //??????????????????
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {
        float ratio = (float) faceInfo.width / (float) bitMapHeight;
        /*Log.e("TAG", " faceInfo.width-->" + faceInfo.width
                + ", bitMapWidth-->" + bitMapWidth + ", bitMapHeight-->" + bitMapHeight
                + ", faceSize??????-->" + ratio
                + ", faceInfo.centerX-->" + faceInfo.centerX
                + ", faceInfo.centerY-->" + faceInfo.centerY);*/
        if (ratio < 0.2) {
            //?????????????????????
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }

//            hintView("?????????????????????");
            Log.e("TAG", "?????????????????????: ");
            return false;
        }

        if (faceInfo.centerX > bitMapHeight * 3 / 4) {
            //???????????????????????????  640-->480  ,480-->360
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }

//            hintView("???????????????????????????");
            Log.e("TAG", "???????????????????????????: ");
            return false;
        } else if (faceInfo.centerX < bitMapHeight / 4) {
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }
            //???????????????????????????  640-->160  ,480-->120
//            hintView("???????????????????????????");
            Log.e("TAG", "???????????????????????????: ");
            return false;
        }
        return true;
    }

    //??????
    public void setUpDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        final EditText etUsername = view.findViewById(R.id.username);
        final EditText etPassword = view.findViewById(R.id.password);
        builder.setView(view).setTitle("???????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etUsername.getText().toString().length() == 0 || etPassword.getText().toString().length() == 0) {
                            ToastUtils.toast(getApplicationContext(), "???????????????????????????!");
                        } else if (etUsername.getText().toString().equals("Admin")
                                && etPassword.getText().toString().equals(settingPassword)) {

                            if (!FaceSDKManager.initModelSuccess) {
                                Toast.makeText(mContext, "SDK????????????????????????????????????",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            //????????????
                            startActivity(new Intent(mContext, GateSettingActivity.class));
                            finish();

                        } else {
                            ToastUtils.toast(getApplicationContext(), "????????????????????????!");
                        }
                    }
                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        NavigationBarUtil.dialogShow(dialog);
    }

    /**
     * ????????????34??? /rs485 /?????????
     */
    private void wiegandOutput34(String id, String floorNumber) {
        try {
            //?????????
            if (!floorNumber.isEmpty()) {
           /* //sendByte1 + ??????ID(2)
            byte[] concat1 = Utils.concat(sendByte1, Utils.terminalBytes(terminalId));
            //sendByte2 + ????????????(16)
            byte[] concat2 = Utils.concat(sendByte2, Utils.cardBytes(Utils.addZero(id)));
            //???????????????1) + sendByte3
            byte[] concat3 = Utils.concat(Utils.floorBytes(floorId), sendByte3);
            //????????????(1) + sendByte4
            byte[] concat4 = Utils.concat(Utils.hexString2Bytes(floorNumber), sendByte4);
            byte[] concat5 = Utils.addBytes(concat1, concat2, concat3);
            byte[] concat = Utils.concat(concat5, concat4);*/
                //????????????
//                    sendDataByUDP1(concat);

                //????????????
             /*   //?????????????????????
                handler.removeCallbacks(relayTimeRunnable);
                relayFlag = false;

                //???????????????
                mGPIOManager.pullUpRelay();

                //??????
                if (!relayFlag) {
                    relayFlag = true;
                    handler.postDelayed(relayTimeRunnable, SingleBaseConfig.getBaseConfig().getRelayTime() * 1000L);
                }*/

            }


            //??????
            BigInteger data = new BigInteger(id, 16);
            int result = mWiegand.output34(data.longValue());
            Log.i("TAG", "Wiegand34 output result:" + result + " card: " + data.longValue() + "  card-16:" + id);

            //485??????
            byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(id)));
            LiveDataBus.get().with("SerialData").setValue(crcUuid);

            //?????????????????????
            handler.removeCallbacks(relayTimeRunnable);
            relayFlag = false;

            //???????????????
            mGPIOManager.pullUpRelay();

            //??????
            if (!relayFlag) {
                relayFlag = true;
                handler.postDelayed(relayTimeRunnable, SingleBaseConfig.getBaseConfig().getRelayTime() * 1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Runnable relayTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mGPIOManager.pullDownRelay();
            relayFlag = false;
        }
    };


    //?????????------start---------------
    private void delayGreenLight() {
        mGPIOManager.pullUpGreenLight();
        GreenLight_Status = 1;
    }

    private void delayRedLight() {
        mGPIOManager.pullUpRedLight();
        RedLight_Status = 1;
    }

    private void delayClose_Red_GedreenLight() {
        mGPIOManager.pullDownGreenLight();
        mGPIOManager.pullDownRedLight();
    }
    //#------end--------------------

    private void showExitDialog(String text) {
        new AlertDialog.Builder(this)
                .setTitle("??????")
                .setMessage(text)
                .setPositiveButton("??????", null)
                .setCancelable(false)
                .show();
    }

    /**
     * ???????????????
     * lib-zxing -- CodeUtils
     */
    AnalyzeQRCodeCallback mAnalyzeQRCodeCallback = new AnalyzeQRCodeCallback() {
        @Override
        public void onAnalyzeSuccess(String result) {
            if (Long.parseLong(DateUtil.timeStamp()) - Long.parseLong(timeFlag1) > 1) {
                timeFlag1 = DateUtil.timeStamp();
                mSoundPoolUtil.play();
            }

            runOnUiThread(() -> {
//                Log.e("TAG", "???????????????: " + result);
//                Log.e("TAG", "????????????????????????????????????????????????: " + result + "?????????" + result.length());
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????*???+?????????
                String reg = "^[\\da-zA-Z]*$";
                if (result.matches(reg)) {
                    if (result.length() == 40) {
                        String substring = result.substring(0, 32);
                        String key = substring.substring(0, 16);
                        String encodeStr = substring.substring(16);

                        Date orderDateStart = null;
                        try {
                            //???????????? 5??????????????????
                            orderDateStart = new SimpleDateFormat("yyyyMMddHHmmssSS").parse(key);
                            if (Utils.time(orderDateStart)) {

                                byte[] keys = ScanUtils.merge2BytesTo1Byte(key);
                                byte[] encodeStrs = ScanUtils.hexStringToBytes(encodeStr);
                                try {
                                    byte[] decrypt = ScanUtils.decrypt(encodeStrs, keys);
                                    String datas = ScanUtils.byte2hex(decrypt).substring(2, 10);

                                    //??????????????????????????????
                                   /* if (qrCodeID != null) {
                                        qrCodeID.clear();
                                    }
                                    qrCodeID.put("qrCodeID", datas);*/

                            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("????????????")
                                    .setMessage("???????????????" + datas)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                    }).create().show();*/

                                    //??????QrCard??????QR??????
                                    String qrName = "";
                                    List<QR> qrInfo = FaceApi.getInstance().getQrInfoByQrCard(datas);
                                    if (qrInfo != null && qrInfo.size() > 0) {
                                        qrName = qrInfo.get(0).getQrName();
                                        //????????????
                                        validView("??????:" + qrName);
                                    } else {
                                        //????????????
                                        validView("????????????:" + datas);
                                    }
//                                    Log.d("TAG", "??????????????????: " + qrName);

//                                    validView(datas);
//                                Log.d("TAG", "???????????????" + datas );
                                    delaySendWiegand(datas);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                invalidView("?????????????????????!");
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        invalidView("??????????????????!");
                    }
                } else {
                    invalidView("??????????????????!");
                }
            });
        }

        @Override
        public void onAnalyzeFailed() {
            if (cancelFlag) {
                if (userNameLayout.getVisibility() != View.GONE) {
                    cancelFlag = false;
                    handler.postDelayed(() -> {
//                        Log.e("TAG", "???????????????");
                        textHuanying.setVisibility(View.VISIBLE);
                        userNameLayout.setVisibility(View.GONE);
                        cancelFlag = true;

                    }, 2000);
                }
            }
        }
    };


    /**
     * ?????????????????????
     *
     * @param datas ??????
     */
    private void delaySendWiegand(String datas) {
        if (!qrFlag) {
            qrFlag = true;

            handler.postDelayed(() -> {

                //?????????
                String qrFloorNumber;
                List<QR> qrInfo = FaceApi.getInstance().getQrInfoByQrCard(datas);
                if (qrInfo != null && qrInfo.size() > 0) {
                    qrFloorNumber = qrInfo.get(0).getQrfloor();
                } else {
                    qrFloorNumber = "";
                }
                Log.d("TAG", "?????????????????????: " + qrFloorNumber);


                //??????34 /rs485
                wiegandOutput34(datas, qrFloorNumber);
                qrFlag = false;
            }, 1000);

        }
    }


    /**
     * ??????BLE??????
     */
    private void scanLeDevice() {
        //?????????BluetoothManager???BluetoothAdapter
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);
        }
        if (mBluetoothManager != null && mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (mBLEScanner == null) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        Log.d("TAG", "searchBLE: ----------> startScan");
        //SDK < 21??????bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback())
        mBLEScanner.startScan(scanCallback);

        //??????????????????
        handler.postDelayed(bleRunnable, 10 * 3000); //30?????????????????????????????????5???
    }

    private Runnable bleRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("TAG", "searchBLE: --------->  stopScan");
            mBLEScanner.stopScan(scanCallback);
            scanLeDevice();
        }
    };

    /**
     * BL????????????
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
//            Log.e("TAG", "onBatchScanResults: " + results.toString());
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            if (result.getScanRecord() != null) {
//                Log.e("TAG", "onScanResult: " + result.toString() + "\n" + Utils.byteToHex(result.getScanRecord().getBytes()));
//            }
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanBytes = scanRecord.getBytes();
            String bytesToHex = ByteUtils.bytesToHex(scanBytes);

            if (bytesToHex.contains("4c000215")) {
//                Log.e("TAG", "ScanResult-----> :" + bytesToHex + "\n" + result.getRssi());
                IBeaconAccept iBeaconAccept = new IBeaconAccept();

                SparseArray<byte[]> specificData = scanRecord.getManufacturerSpecificData();

                if (specificData == null) {
                    return;
                }

                for (int i = 0; i < specificData.size(); i++) {
                    byte[] bytes = specificData.valueAt(i);
                    if (bytes == null) {
                        continue;
                    }
                    IBeaconAccept.IBeaconInfo iBeaconInfo = iBeaconAccept.getIBeaconInfo(scanBytes, result.getRssi());
                    String uuid = iBeaconInfo.uuid;
                    if (!TextUtils.isEmpty(uuid)) {
                        //??????????????????????????? ????????????
                        if (Math.abs(iBeaconInfo.rssi) < SingleBaseConfig.getBaseConfig().getSignalStrength()) {
                            String replace = uuid.replace("-", "");
                            Log.d("TAG", "uuid: " + uuid + ", ?????? " + Math.abs(iBeaconInfo.rssi) +
                                    " ???????????????" + SingleBaseConfig.getBaseConfig().getSignalStrength());
//                            Log.d("TAG", "onScanResult: " + replace);
                            String key = replace.substring(0, 16);
                            String encodeStr = replace.substring(16);
                            byte[] keys = ScanUtils.merge2BytesTo1Byte(key);
                            byte[] encodeStrs = ScanUtils.hexStringToBytes(encodeStr);
                            try {
                                byte[] decrypt = ScanUtils.decrypt(encodeStrs, keys);
                                String datas = ScanUtils.byte2hex(decrypt).substring(2, 10);

                                //???????????????????????????
                               /* if (bluetoothID != null) {
                                    bluetoothID.clear();
                                }
                                bluetoothID.put("bluetoothID", datas);*/

                                //????????????
                                delaySendBLWiegand(datas);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
//            Log.e("TAG", "onScanFailed: " + errorCode);
        }
    };

    /**
     * ??????????????????
     *
     * @param datas
     */
    private void delaySendBLWiegand(String datas) {
        if (!blFlag) {
            blFlag = true;
            handler.postDelayed(() -> {
                runOnUiThread(() -> showBlHint(datas));
                //????????????
                if (null != mSoundPool) {
                    mSoundPool.play(soundID.get(3), volume, volume, 0, 0, 1);
                }
                //??????34 / rs485
                wiegandOutput34(datas, "");
                Log.e("TAG", "datas: " + datas);
                blFlag = false;
            }, 1000);
        }

    }

    List<byte[]> listBytes = new ArrayList<>();
    private boolean face_udp_flag = false;
    private boolean length_flag = false;
    int srcLength = 0;
    int dataHeartbeatLength = 0;
    byte[] serialNumber_head = new byte[1];

    /**
     * --------------------------------????????????--------------------------------------------------
     * UDP??????????????????
     */
    public class UdpReceiveThread extends Thread {
        public void run() {
            try {
                Log.e("TAG", "UdpReceiveThread??????.... ");
                receiveSocket = new DatagramSocket(RECEIVE_PORT);
                serverAddr = InetAddress.getByName(HOST);
                while (isRunning) {
                    byte[] inBuf = new byte[1024 * 1024];
                    DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
                    receiveSocket.receive(inPacket);

                    byte[] receiveInfo_des = inPacket.getData();
                    byte[] data = new byte[inPacket.getLength()];
                    System.arraycopy(receiveInfo_des, 0, data, 0, inPacket.getLength());

                    Log.e("TAG", "?????????: " + Arrays.toString(data));
                    Log.e("TAG", "???????????????>bytesToHex: " + ByteUtils.bytesToHex(data));
                    //DES??????
                    byte[] receiveInfo = Utils.decryptDES(data, "Lookyxyx");
                    //?????????????????????
                    byte[] buffer = new byte[receiveInfo.length];
                    System.arraycopy(receiveInfo, 0, buffer, 0, receiveInfo.length);


                    //--------------------------------start-----------------------------------------------

                    int receiveLength = buffer.length;
                    Log.e("TAG", "?????????-->receive_UDP_Length----> " + receiveLength);
                    Log.e("TAG", "?????????-->bytesToHex: " + ByteUtils.bytesToHex(buffer));

                    //14???????????????????????????????????????
                    if (receiveLength == 14) {
                        //?????????????????????
                        byte[] category = new byte[2];
                        System.arraycopy(buffer, 6, category, 0, 2);

                        byte[] lengthInBytes1 = new byte[4];
                        System.arraycopy(buffer, 8, lengthInBytes1, 0, 4);
                        String length1 = Utils.byteToHex(lengthInBytes1);
                        dataHeartbeatLength = new BigInteger(length1, 16).intValue();
                        Log.i("TAG", "????????????(??????or??????)??????: " + Arrays.toString(lengthInBytes1) + "------>" + dataHeartbeatLength);


                        //????????????
                        if (Arrays.equals(category, deleteAddress)) {
                            FaceApi.getInstance().visitorDelete();
                            boolean bRet = FaceApi.getInstance().deleteQr();
                            if (bRet) {
                                // ???????????????????????????
                                FaceSDKManager.getInstance().initDataBases(getApplicationContext());
                                //??????
                                byte[] sendBytesDes = Utils.concat(deleteResponse, ok);
                                byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                receiveSocket.send(dp);
                                Log.i("TAG", "???????????? " + bRet);
                            } else {
                                //??????
                                byte[] sendBytesDes = Utils.concat(deleteResponse, delete_er);
                                byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                receiveSocket.send(dp);
                            }
                            //???????????????
                        } else if (Arrays.equals(category, heartbeatAddress)) {
                            byte[] sendBytes = Utils.encryptDES(heartbeatResponse, "Lookyxyx");
                            DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                            receiveSocket.send(dp);
                        }

                    } else if (receiveLength == 18) {
                        //????????????????????????
                        //?????????????????????
                        byte[] category = new byte[2];
                        System.arraycopy(buffer, 6, category, 0, 2);

                        byte[] lengthInBytes = new byte[4];
                        System.arraycopy(buffer, 12, lengthInBytes, 0, 4);
                        String length1 = Utils.byteToHex(lengthInBytes);
                        int deleteSingleLength = new BigInteger(length1, 16).intValue();
                        Log.i("TAG", "????????????(??????or??????)??????: " + Arrays.toString(lengthInBytes) + "------>" + deleteSingleLength);
                        //fefefefeaa55004911111233000000120000
                        if (Arrays.equals(category, deleteSingleAddress)) {
                            byte[] cardByte = new byte[4];
                            System.arraycopy(buffer, 8, cardByte, 0, 4);

                            //??????
                            String card = Utils.byteToHex(cardByte);
                            Log.e("TAG", "card-----------: " + card);

                            //????????????????????????
                            UserInfoManager.getInstance().deleteUserInfo(card);
                            UserInfoManager.getInstance().setRemoveStaffCallback(new RemoveStaffCallback() {
                                @Override
                                public void removeStaffSuccess() {
                                    Log.e("TAG", "????????????????????????: ??????");
                                    // ???????????????????????????
                                    FaceSDKManager.getInstance().initDataBases(getApplicationContext());
                                    byte[] sendBytes1 = Utils.addBytes(deleteSingleResponse, cardByte, dataLength);
                                    byte[] sendBytesDes = Utils.concat(sendBytes1, ok);
                                    byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                    DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                    try {
                                        receiveSocket.send(dp);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void removeStaffFailure() {
                                    Log.e("TAG", "????????????????????????: ??????");
                                    byte[] sendBytes1 = Utils.addBytes(deleteSingleResponse, cardByte, dataLength);
                                    byte[] sendBytesDes = Utils.concat(sendBytes1, er);
                                    byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                    DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                    try {
                                        receiveSocket.send(dp);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }

                    } else if (receiveLength > 18) {
                        if (!length_flag) {
                            length_flag = true;
                            //???????????????
                            byte[] lengthInBytes = new byte[4];
                            System.arraycopy(buffer, 9, lengthInBytes, 0, 4);

                            //??????
                            serialNumber_head = new byte[1];
                            System.arraycopy(buffer, 8, serialNumber_head, 0, 1);

                            String length = Utils.byteToHex(lengthInBytes);
                            srcLength = new BigInteger(length, 16).intValue();
                            Log.i("TAG", "????????????????????????: " + Arrays.toString(lengthInBytes) + "??????" + srcLength);
                        }


                        //?????????????????????
                        byte[] category = new byte[2];
                        System.arraycopy(receiveInfo, 6, category, 0, 2);

                        //??????????????????
                        byte[] checkByte = new byte[2];
                        System.arraycopy(buffer, buffer.length - 2, checkByte, 0, 2);

                        if (!Utils.byteToHex(checkByte).equals("0000")) {
                            //??????????????????
                            listBytes.add(buffer);
                            face_udp_flag = true;

                        } else {
                            if (!face_udp_flag) {
                                //?????????
                                if (Arrays.equals(category, qrCodeAddress)) {
                                    byte[] floorByte = new byte[1];
                                    byte[] nameByte = new byte[20];
                                    byte[] cardByte = new byte[4];

                                    //??????
                                    byte[] serialNumber = new byte[1];
                                    System.arraycopy(receiveInfo, 8, serialNumber, 0, 1);

                                    //??????ID
                                    System.arraycopy(receiveInfo, 13, floorByte, 0, 1);
                                    //?????????
                                    System.arraycopy(receiveInfo, 14, nameByte, 0, 20);
                                    //??????
                                    System.arraycopy(receiveInfo, 34, cardByte, 0, 4);

                                    //??????ID
                                    String floor = Utils.byteToHex(floorByte);
                                    //??????
                                    String card = Utils.byteToHex(cardByte);
                                    //??????
                                    String username = null;
                                    try {
                                        username = new String(nameByte, "GB2312");
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    boolean ret = FaceApi.getInstance().registerQrIntoDBmanager(username.trim(), card, floor);
                                    if (ret) {
                                        //??????
                                        byte[] sendBytes1 = Utils.addBytes(qrCodeResponse, serialNumber, dataLength);
                                        byte[] sendBytesDes = Utils.concat(sendBytes1, ok);
                                        byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                        DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                        receiveSocket.send(dp);
                                        Log.i("TAG", "?????? QR?????? " + username.trim() + " ?????? " + card + " ??????ID " + floor);
                                    } else {
                                        //??????
                                        byte[] sendBytes1 = Utils.addBytes(qrCodeResponse, serialNumber, dataLength);
                                        byte[] sendBytesDes = Utils.concat(sendBytes1, qr_er);
                                        byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                        DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                        receiveSocket.send(dp);
                                    }
                                    length_flag = false;
                                }
                            } else {
                                byte[] concatByte = new byte[1024 * 1024 * 10];
//                            byte[] concatByte = new byte[srcLength];
                                listBytes.add(buffer);
                                //??????????????????
                                for (int i = 0; i < listBytes.size() - 1; i++) {
                                    if (i == 0) {
                                        concatByte = Utils.concat(listBytes.get(i), listBytes.get(i + 1));
                                    } else {
                                        concatByte = Utils.concat(concatByte, listBytes.get(i + 1));
                                    }
                                }

//                            byte[] dataLength = new byte[4];
//                            //????????????
//                            System.arraycopy(concatByte, 8, dataLength, 0, 4);
                                Log.e("TAG", "?????????????????????: " + concatByte.length);
                                Log.e("TAG", "???????????????: " + Arrays.toString(concatByte));
                                String s = ByteUtils.bytesToHex(concatByte);
                                Log.e("TAG", "???????????????: " + s);
//                            if (length == concatByte.length) {
                                //?????????????????????????????????????????????
                                if (concatByte.length == srcLength) {
                                    //????????????????????????
                                    byte[] spliceAddress = new byte[2];
                                    System.arraycopy(concatByte, 6, spliceAddress, 0, 2);
                                    //????????????????????????
                                    if (Arrays.equals(spliceAddress, faceAddress)) {
                                        Bitmap imgBitmap = null;
                                        int imgLength = concatByte.length - 39;
                                        byte[] floorByte = new byte[1];
                                        byte[] nameByte = new byte[20];
                                        byte[] cardByte = new byte[4];
                                        byte[] imgByte = new byte[imgLength];

                                        //??????
                                        byte[] serialNumber = new byte[1];
                                        System.arraycopy(concatByte, 8, serialNumber, 0, 1);

                                        //??????ID
                                        System.arraycopy(concatByte, 13, floorByte, 0, 1);
                                        //?????????
                                        System.arraycopy(concatByte, 14, nameByte, 0, 20);
                                        //??????
                                        System.arraycopy(concatByte, 34, cardByte, 0, 4);
                                        //img
                                        System.arraycopy(concatByte, 38, imgByte, 0, imgLength);

                                        //??????ID
                                        String floor = Utils.byteToHex(floorByte);
                                        //??????
                                        String card = Utils.byteToHex(cardByte);
                                        //??????
                                        String username = null;
                                        try {
                                            username = new String(nameByte, "GB2312");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            imgBitmap = Bytes2Bimap(imgByte);
                                            Log.e("TAG", "floor " + floor);
                                            Log.e("TAG", "card " + card);
                                            Log.e("TAG", "username " + username.trim());
                                            String s1 = ByteUtils.bytesToHex(imgByte);
                                            Log.e("TAG", username.trim() + "->img " + s1);

                                            byte[] bytes = new byte[512];

                                            //???????????????
                                            ImportFeatureResult result;
                                            // ???????????????????????????128????????????-1??????????????????-2????????????????????????
                                            result = getFeature(imgBitmap, bytes,
                                                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
                                            Log.i("TAG", "live_photo = " + result.getResult());

                                            if (result.getResult() == 128) {
                                                //?????????????????????
                                                String dbCard = FaceApi.getInstance().getUserCardByUserCard(card);
                                                Log.i("TAG", "?????????db_Card: " + dbCard);
                                                if (!dbCard.equals(card)) {
                                                    FaceApi.getInstance().registerUserIntoDBmanager(null, username.trim(), floor, card, bytes);
                                                    // ???????????????????????????
                                                    FaceSDKManager.getInstance().initDataBases(getApplicationContext());
                                                    Log.i("TAG", "?????? ?????? " + username.trim() + " ?????? " + card + " ??????ID " + floor);
                                                }
                                                byte[] sendBytes1 = Utils.addBytes(response, serialNumber, dataLength);
                                                byte[] sendBytesDes = Utils.concat(sendBytes1, ok);
                                                byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                                DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                                receiveSocket.send(dp);
                                                listBytes.clear();
                                            } else {
                                                //?????????????????????
                                                byte[] sendBytes1 = Utils.addBytes(response, serialNumber, dataLength);
                                                byte[] sendBytesDes = Utils.concat(sendBytes1, face_er);
                                                byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                                DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                                receiveSocket.send(dp);
                                                listBytes.clear();
                                            }

                                            face_udp_flag = false;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            if (imgBitmap != null && !imgBitmap.isRecycled()) {
                                                imgBitmap.recycle();
                                            }
                                        }
                                    }
                                } else {
                                    //????????????
                                    byte[] sendBytes1 = Utils.addBytes(response, serialNumber_head, dataLength);
                                    byte[] sendBytesDes = Utils.concat(sendBytes1, packet_loss_er);
                                    byte[] sendBytes = Utils.encryptDES(sendBytesDes, "Lookyxyx");
                                    DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, inPacket.getAddress(), inPacket.getPort());
                                    receiveSocket.send(dp);
                                }
                                length_flag = false;
                                face_udp_flag = false;

                                srcLength = 0;
                                listBytes.clear();
                            }
                        }


                    }


                    //------------------------------------end-------------------------------------------

                    /*String info = new String(receiveInfo, 0, inPacket.getLength());
                    Log.e("TAG", "????????????????????? " + Arrays.toString(category));
                    Log.e("TAG", "receiveIP: " + inPacket.getAddress() + ": " + inPacket.getPort());
                    Log.e("TAG", "bytesToHex: " + ByteUtils.bytesToHex(buffer));
                    Log.e("TAG", "receiveInfo: " + Arrays.toString(buffer));
                    Log.e("TAG", "receive-length: " + inPacket.getLength());
                    Log.e("TAG", "info-length: " + info.length());
                    Log.e("TAG", "buffer-length: " + buffer.length);
                    Log.e("TAG", "checkByte: " + Utils.byteToHex(checkByte));*/


                    //????????????
                    /*SPUtils.put(mContext, "remoteIp", inPacket.getAddress().getHostAddress());
                    SPUtils.put(mContext, "remotePort", String.valueOf(inPacket.getPort()));*/

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    //QR??????????????????
    private void validView(String card) {
        textHuanying.setVisibility(View.GONE);
        userNameLayout.setVisibility(View.VISIBLE);
        nameImage.setImageResource(R.mipmap.ic_tips_gate_success);
        nameText.setTextColor(Color.parseColor("#0dc6ff"));
        nameText.setText(card);
    }

    //QR??????????????????
    private void invalidView(String text) {
        textHuanying.setVisibility(View.GONE);
        userNameLayout.setVisibility(View.VISIBLE);
        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
        nameText.setTextColor(Color.parseColor("#fec133"));
        nameText.setText(text);
    }

    //BL??????
    private void showBlHint(String card) {
        textHuanying.setVisibility(View.GONE);
        userNameLayout.setVisibility(View.VISIBLE);
        nameImage.setImageResource(R.mipmap.ic_tips_gate_success);
        nameText.setTextColor(Color.parseColor("#0dc6ff"));
        nameText.setText("????????????:" + card);
    }


    /**
     * ???????????????
     */
    public ImportFeatureResult getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return new ImportFeatureResult(2, null);
        }

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // ???????????????????????????????????????
        FaceInfo[] faceInfos = com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        if (faceInfos == null || faceInfos.length == 0) {
            imageInstance.destory();
            // ????????????
            Bitmap broadBitmap = com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils.broadImage(bitmap);
            imageInstance = new BDFaceImageInstance(broadBitmap);
            // ???????????????????????????????????????
            faceInfos = com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().getFaceDetect()
                    .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
            // ?????????????????????????????????????????????????????????
            if (faceInfos == null || faceInfos.length == 0) {
                return new ImportFeatureResult(/*rotationDetection(broadBitmap , 90)*/8, null);
            }
        }
        // ???????????????
        if (faceInfos.length > 1) {
            imageInstance.destory();
            return new ImportFeatureResult(9, null);
        }
        FaceInfo faceInfo = faceInfos[0];
        // ????????????
        int quality = onQualityCheck(faceInfo);
        if (quality != 0) {
            return new ImportFeatureResult(quality, null);
        }
        // ????????????????????????????????????
        float ret = com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().getFaceFeature().feature(
                featureType, imageInstance,
                faceInfo.landmarks, feature);
        // ????????????
        /*BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                .cropFaceByLandmark(imageInstance, faceInfo.landmarks,
                        2.0f, true, new AtomicInteger());
        if (cropInstance == null) {
            imageInstance.destory();
            return new ImportFeatureResult(10, null);
        }

        Bitmap cropBmp = BitmapUtils.getInstaceBmp(cropInstance);
        cropInstance.destory();*/
        imageInstance.destory();
        return new ImportFeatureResult(ret, null);
    }


    /**
     * ??????????????????????????????????????????????????????
     * ???????????? SingleBaseConfig.getBaseConfig().setQualityControl(true);?????????true???
     * ?????????  FaceSDKManager.getInstance().initConfig() ???????????????????????????
     *
     * @return
     */
    public int onQualityCheck(FaceInfo faceInfo) {

        if (!com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return 0;
        }

        if (faceInfo != null) {

            // ????????????
            if (Math.abs(faceInfo.yaw) > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getYaw()) {
                return 4;
            } else if (Math.abs(faceInfo.roll) > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getRoll()) {
                return 4;
            } else if (Math.abs(faceInfo.pitch) > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getPitch()) {
                return 4;
            }

            // ??????????????????
            float blur = faceInfo.bluriness;
            if (blur > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getBlur()) {
                return 5;
            }

            // ??????????????????
            float illum = faceInfo.illum;
            if (illum < com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getIllumination()) {
                return 7;
            }


            // ??????????????????
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;

                if (occlusion.leftEye > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.rightEye > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.nose > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getNose()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.mouth > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getMouth()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.leftCheek > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.rightCheek > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // ?????????????????????
                    return 6;
                } else if (occlusion.chin > com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // ?????????????????????
                    return 6;
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }


    /**
     * ????????????
     */
    private void releaseSoundPool() {
        if (mSoundPool != null) {
            Log.e("TAG", "releaseSoundPool:++++++++++++++");
            mSoundPool.autoPause();
            mSoundPool.unload(soundID.get(1));
            mSoundPool.unload(soundID.get(2));
            mSoundPool.unload(soundID.get(3));
//            mSoundPool.unload(soundID.get(4));
//            mSoundPool.unload(soundID.get(5));
//            mSoundPool.unload(soundID.get(6));
//            mSoundPool.unload(soundID.get(7));
//            mSoundPool.unload(soundID.get(8));
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        delayClose_Red_GedreenLight();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        //?????????????????????
        mSoundPoolUtil.release();

        //??????????????????
        isRunning = false;

        //??????????????????
        timeFlagBool = false;

        mWiegand.release();

        if (mRBmp != null && !mRBmp.isRecycled()) {
            // ??????????????????null
            mRBmp.recycle();
            mRBmp = null;
        }

        //?????????????????????
        delayClose_Red_GedreenLight();

        releaseSoundPool();

        //????????????
        if (mBLEScanner != null) {
            Log.e("TAG", "BLEScanner_release");
            mBLEScanner.stopScan(scanCallback);
            handler.removeCallbacks(bleRunnable);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
