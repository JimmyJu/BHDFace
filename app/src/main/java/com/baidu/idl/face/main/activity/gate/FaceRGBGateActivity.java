package com.baidu.idl.face.main.activity.gate;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.util.Log;
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
import com.baidu.idl.face.main.utils.DateUtil;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.LogUtilsDynamic;
import com.baidu.idl.face.main.utils.NavigationBarUtil;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.example.datalibrary.model.User;
import com.example.yfaceapi.GPIOManager;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;


public class FaceRGBGateActivity extends BaseActivity {

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
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
    //补光灯状态
    private int RedLight_Status = 0;
    private int GreenLight_Status = 0;
    //时间戳标识
    private String timeFlag;

    //继电器标示
    private boolean relayFlag = false;

    // //播报mp3
    private SoundPool mSoundPool = null;
    private final HashMap<Integer, Integer> soundID = new HashMap<>();
    private float volume;
    private SoundPool.Builder spBuilder;

    private Hashtable<String, Long> faceTime = new Hashtable<>();

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private RelativeLayout textHuanying;
    private ImageView nameImage;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private Paint paintBg;
    //    private View view;
//    private TextView logoText;
    private User mUser;
    private boolean isTime = true;
    private long startTime;
    private boolean detectCount;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private DoubleClickListener doubleClickListener;

    //-------------------------------------------------------------------------------
    /**
     * 卡号
     */
    private byte[] cardNumberByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**
     * 人员名称 20个字节
     */
    private byte[] personnelNameByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * 照片信息头
     */
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    /**
     * 照片信息尾
     */
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};
    /**
     * 字节数组输出流
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private Bitmap mRBmp = null;
    /**
     * 检测人脸间隔标识
     */
    private boolean faceFlag = false;

    /**
     * 检测人脸间隔 handler
     */
    private Handler handler = new Handler();

    /**
     * 底部状态View 活体检测、识别通过、识别未通过、发送信息、服务器状态
     */
    private TextView mLiveTextView, mAdoptTextView, mErrorTextView, mSendTextView, mServerStateTextView;
    private int mLiveNum = 1, mAdoptNum = 1, mErrorNum = 1;

    //切换寄存器地址 false: 21       true:22(需要注册的)
    private Boolean switchPortNum = false;
    int flag = 0;

    //-----------------------------------------------------------------------------
    //动态对比数
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
        //初始化音频
        initSP();

        //接收数据
        liveDataBus();

        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        int rbgCameraId = SingleBaseConfig.getBaseConfig().getRBGCameraId();
        Log.e("TAG", "rbgCameraId---------------: " + rbgCameraId);
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
                    ToastUtils.toast(mContext, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(mContext, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    /**
     * View
     */
    private void initView() {
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
            mDrawDetectFaceView.setRotationY(180);
        }
        // 单目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);

        //底部logo
//        logoText = findViewById(R.id.logo_text);
//        logoText.setVisibility(View.VISIBLE);

        // 左上logo
        ImageView switchPort = findViewById(R.id.switchPort);
        switchPort.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                switch (flag) {
                    case 0:
                        switchPortNum = true;
                        flag = 1;
                        showExitDialog("注册模式");
                        break;
                    case 1:
                        switchPortNum = false;
                        flag = 0;
                        showExitDialog("正常模式");
                        break;
                }
            }
        });


        // 设置
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
                            //进入设置
                            setUpDialog();
                        }
                        mBtSetting.setOnClickListener(doubleClickListener);
                    }
                });
            }
        };

        mBtSetting.setOnClickListener(doubleClickListener);


        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        Log.e("TAG", "活体类别: " + mLiveType);
        // ***************预览模式*************
        textHuanying = findViewById(R.id.huanying_relative);
        userNameLayout = findViewById(R.id.user_name_layout);
        nameImage = findViewById(R.id.name_image);
        nameText = findViewById(R.id.name_text);
//        view = findViewById(R.id.mongolia_view);
//        view.setAlpha(0.85f);
//        view.setBackgroundColor(Color.parseColor("#ffffff"));

        //底部 view
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
                    mServerStateTextView.setText("在线");
                } else {
                    mServerStateTextView.setText("离线");
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
        //获取当前时间
        timeFlag = DateUtil.timeStamp();
        // TODO ： 临时放置
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
                        // 摄像头预览数据进行人脸检测
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
                                    @Override
                                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                                        // 预览模式
//                                        Log.e("TAG", "时间戳: " + (Long.parseLong(DateUtil.timeStamp()) - Long.parseLong(timeFlag)));
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
                                        // 绘制人脸框
                                        showFrame(livenessModel);
                                    }
                                });
                    }
                });
    }

    // ***************预览模式结果输出*************
    private void checkCloseDebugResult(final LivenessModel livenessModel, int width, int height) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //|| livenessModel.getFaceInfo() == null || !faceSizeFilter(livenessModel.getFaceInfo(), width, height)
                if (livenessModel == null) {

                    //补光灯控制
                    try {
                        delayClose_Red_GedreenLight();
                        RedLight_Status = 0;
                        GreenLight_Status = 0;
                    } catch (Exception e) {
                        Log.e("loge", "run:----- delayClose_Red_GedreenLight----异常" + e.toString());
                    }


                   /* // 对背景色颜色进行改变，操作的属性为"alpha",此处必须这样写
                    // 不能全小写,后面设置的是对view的渐变
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

                    textHuanying.setVisibility(View.VISIBLE);
                    userNameLayout.setVisibility(View.GONE);
                }
                /*isTime = true;
                if (detectCount) {
                    detectCount = false;
                    objectAnimator();
                } else {
                    view.setVisibility(View.GONE);
                }*/

                else {

                    //获取照片
                    try {
                        mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                    } catch (OutOfMemoryError e) {
                        ToastUtils.toast(FaceRGBGateActivity.this, "" + e.toString());
                    }

                    //压缩照片
                    mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
                    byteArrayOutputStream.reset();

                    if (mRBmp != null && !mRBmp.isRecycled()) {
                        // 回收并且置为null
                        mRBmp.recycle();
                        mRBmp = null;
                    }

                    User user = livenessModel.getUser();
//                float score = user.getScore();
                    if (user == null) {
                        //补光灯控制 红灯
                        try {
                            if (GreenLight_Status == 1) {
                                mGPIOManager.pullDownGreenLight();
                                GreenLight_Status = 0;
                            }
                            delayRedLight();
                        } catch (Exception e) {
                            Log.e("loge", "run:----- pullDownGreenLight-pullUpRedLight----异常" + e.toString());
                        }


                        mUser = null;
                        if (livenessModel.isMultiFrame()) {
                            textHuanying.setVisibility(View.GONE);
                            userNameLayout.setVisibility(View.VISIBLE);
                            nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
                            nameText.setTextColor(Color.parseColor("#fec133"));
                            nameText.setText("抱歉 未能认出您");

                            //延迟3秒发送给后台图片、特征值
                            delaySendData(livenessModel, imageData, null);

                            //发送串口数据
                            sendSerialPortData(null);
                        } else {
                            textHuanying.setVisibility(View.VISIBLE);
                            userNameLayout.setVisibility(View.GONE);
                        }
                    } else {
                        //补光灯控制  绿灯
                        try {
                            if (RedLight_Status == 1) {
                                mGPIOManager.pullDownRedLight();
                                RedLight_Status = 0;
                            }
                            delayGreenLight();
                        } catch (Exception e) {
                            Log.e("loge", "run:----- pullDownRedLight-pullUpGreenLight----异常" + e.toString());
                        }
                        String userName = user.getUserName();
                        String userName_data = (String) SPUtils.get(FaceRGBGateActivity.this, "userName", "");
                        flag_same = userName.equals(userName_data);

                        if (!flag_same) {
                            flag_same = true;
                            SPUtils.put(FaceRGBGateActivity.this, "userName", userName);
                            LogUtilsDynamic.i("---->" + user.getUserName().trim(), "动态识别" + faceCount++ +
                                    "  对比分值: " + livenessModel.getFeatureScore());

                        }
                        mUser = user;
                        textHuanying.setVisibility(View.GONE);
                        userNameLayout.setVisibility(View.VISIBLE);
                        nameImage.setImageResource(R.mipmap.ic_tips_gate_success);
                        nameText.setTextColor(Color.parseColor("#0dc6ff"));
                        nameText.setText(FileUtils.spotString(user.getUserName()) + " 欢迎您");

                        //延迟3秒发送给后台图片，特征，人名，卡号
                        delaySendData(livenessModel, imageData, user);

                        //发送串口数据
                        sendSerialPortData(user);
                    }

                }
            }
        });
    }

    //日志重复值标识
    private boolean flag_same = false;


    //失败对话框
    private void hintView(String text) {
        textHuanying.setVisibility(View.GONE);
        userNameLayout.setVisibility(View.VISIBLE);
        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
        nameText.setTextColor(Color.parseColor("#fec133"));
        nameText.setText(text);
    }

    /**
     * 绘制人脸框
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
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));

                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                // 人脸框颜色
                FaceOnDrawTexturViewUtil.drawFaceColor(mUser, paint, paintBg, model);
                // 绘制人脸框
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    // 蒙层动画
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
     * 声音播报
     */
    private void initSP() {
        if (null != mSoundPool) {
            Log.e("TAG", "initSP:--------重置-----");
            mSoundPool.release();
        }
        if (null == mSoundPool) {
            if (Build.VERSION.SDK_INT > 21) {
                if (null == spBuilder) {
                    Log.e("TAG", "onStart: 音频初始化");
                    spBuilder = new SoundPool.Builder();
                    //传入音频数量
                    spBuilder.setMaxStreams(1);
                    //AudioAttributes是一个封装音频各种属性的方法
                    AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
                    //设置音频流的合适的属性
                    attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//STREAM_MUSIC
                    //加载一个AudioAttributes
                    spBuilder.setAudioAttributes(attrBuilder.build());
                }
                mSoundPool = spBuilder.build();

            } else {
                mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
            }
//            if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
            soundID.put(1, mSoundPool.load(this, R.raw.unregistered, 1));
            soundID.put(2, mSoundPool.load(this, R.raw.success, 1));
//                soundID.put(3, mSoundPool.load(this, R.raw.bluetooth_signal, 1));//蓝牙信息播报
//                soundID.put(4, mSoundPool.load(this, R.raw.error_info, 1));//复验--不通过
//                soundID.put(5, mSoundPool.load(this, R.raw.face_verify, 1));//复验--人脸播报
//                soundID.put(6, mSoundPool.load(this, R.raw.qrcode_verify, 1));//复验--二维码播报
//                soundID.put(7, mSoundPool.load(this, R.raw.bluetooth_verify, 1));//复验--蓝牙播报
//                soundID.put(8, mSoundPool.load(this, R.raw.success_verify, 1));//复验--验证成功播报
//            }
            AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volume = streamVolumeCurrent / streamVolumeMax;
            // 设置加载完成监听
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    if (mSoundPool != null) {
                        Log.e("TAG", "声音加载完成");
                    }
                }
            });
        }
    }


    /**
     * 发送串口数据 韦根 /rs485
     */
    private void sendSerialPortData(User user) {
        if (user == null) {
//            Log.e("TAG", "注册状态: " + switchPortNum);
            if (faceTime.containsKey("fail")) {
                Long oldTime = faceTime.get("fail");
                Long newTime = System.currentTimeMillis();
                if (newTime - oldTime > 3000) {
                    faceTime.put("fail", newTime);
                    mErrorTextView.setText("" + mErrorNum++);
                    mLiveTextView.setText("" + mLiveNum++);
//                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //播放音频
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
                //播放音频
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
                    //韦根输出
                    wiegandOutput34(Utils.addZero(user.getUserInfo()));
//                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //播放音频
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
                //韦根输出
                wiegandOutput34(Utils.addZero(user.getUserInfo()));
//                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                //播放音频
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
     * 延迟3秒发送给后台图片，特征，人名，卡号
     *
     * @param livenessModel 模型
     * @param imageData     照片字节
     * @param user          user = null 识别失败 , user != null 识别成功
     */
    private void delaySendData(LivenessModel livenessModel, byte[] imageData, User user) {
        if (!faceFlag) {

            faceFlag = true;

            handler.postDelayed(() -> {

                if (livenessModel.getFeature() != null) {
                    if (user == null) {
                        LiveDataBus.get().with("switchPort").postValue(switchPortNum);
                        //发送给后台图片、特征值
                        byte[] registerData = Utils.concat(
                                //拼接特征值、卡号字节
                                Utils.concat(livenessModel.getFeature(), cardNumberByte),
                                //拼接人员名称、照片信息字节
                                Utils.concat(personnelNameByte, imageData)
                        );
                        LiveDataBus.get().with("registerData").postValue(registerData);
                    } else {
                        //拼接 特征、卡号、人名、照片，字节
                        try {
                            byte[] registerData = Utils.addBytes(
                                    //拼接 特征、卡号
                                    Utils.concat(livenessModel.getFeature(), Utils.hexString2Bytes(user.getUserInfo())),
                                    //人名
                                    user.getUserName().getBytes("GB2312"),
                                    //照片
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

    //人脸大小过滤
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {
        float ratio = (float) faceInfo.width / (float) bitMapHeight;
        /*Log.e("TAG", " faceInfo.width-->" + faceInfo.width
                + ", bitMapWidth-->" + bitMapWidth + ", bitMapHeight-->" + bitMapHeight
                + ", faceSize比率-->" + ratio
                + ", faceInfo.centerX-->" + faceInfo.centerX
                + ", faceInfo.centerY-->" + faceInfo.centerY);*/
        if (ratio < 0.2) {
            //人脸离屏幕太远
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }

//            hintView("人脸离屏幕太远");
            Log.e("TAG", "人脸离屏幕太远: ");
            return false;
        }

        if (faceInfo.centerX > bitMapHeight * 3 / 4) {
            //人脸在屏幕中太靠右  640-->480  ,480-->360
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }

//            hintView("人脸在屏幕中太靠右");
            Log.e("TAG", "人脸在屏幕中太靠右: ");
            return false;
        } else if (faceInfo.centerX < bitMapHeight / 4) {
//            if (rlDiscernBg.getVisibility() != View.GONE) {
//                rlDiscernBg.setVisibility(View.GONE);
//            }
            //人脸在屏幕中太靠左  640-->160  ,480-->120
//            hintView("人脸在屏幕中太靠左");
            Log.e("TAG", "人脸在屏幕中太靠左: ");
            return false;
        }
        return true;
    }

    //设置
    public void setUpDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        final EditText etUsername = view.findViewById(R.id.username);
        final EditText etPassword = view.findViewById(R.id.password);
        builder.setView(view).setTitle("请输入用户名和密码")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etUsername.getText().toString().length() == 0 || etPassword.getText().toString().length() == 0) {
                            ToastUtils.toast(getApplicationContext(), "账号或密码不能为空!");
                        } else if (etUsername.getText().toString().equals("Admin")
                                && etPassword.getText().toString().equals("123456")) {

                            if (!FaceSDKManager.initModelSuccess) {
                                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            //设置页面
                            startActivity(new Intent(mContext, GateSettingActivity.class));
                            finish();

                        } else {
                            ToastUtils.toast(getApplicationContext(), "账号或密码不正确!");
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
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
     * 输出韦根34位 /rs485 /继电器
     */
    private void wiegandOutput34(String id) {
        //韦根
        BigInteger data = new BigInteger(id, 16);
        int result = mWiegand.output34(data.longValue());
        Log.i("TAG", "Wiegand34 output result:" + result + " card: " + data.longValue() + "  card-16:" + id);

        //485输出
        byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(id)));
        LiveDataBus.get().with("SerialData").setValue(crcUuid);

        //移除继电器延时
        handler.removeCallbacks(relayTimeRunnable);
        relayFlag = false;

        //继电器拉升
        mGPIOManager.pullUpRelay();

        //延时
        if (!relayFlag) {
            relayFlag = true;
            handler.postDelayed(relayTimeRunnable, SingleBaseConfig.getBaseConfig().getRelayTime() * 1000);
        }
    }


    private Runnable relayTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mGPIOManager.pullDownRelay();
            relayFlag = false;
        }
    };


    //补光灯------start---------------
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
                .setTitle("提示")
                .setMessage(text)
                .setPositiveButton("确定", null)
                .setCancelable(false)
                .show();
    }

    /**
     * 释放资源
     */
    private void releaseSoundPool() {
        if (mSoundPool != null) {
            Log.e("TAG", "releaseSoundPool:++++++++++++++");
            mSoundPool.autoPause();
            mSoundPool.unload(soundID.get(1));
            mSoundPool.unload(soundID.get(2));
//            mSoundPool.unload(soundID.get(3));
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

        mWiegand.release();

        if (mRBmp != null && !mRBmp.isRecycled()) {
            // 回收并且置为null
            mRBmp.recycle();
            mRBmp = null;
        }

        //关闭所有补光灯
        delayClose_Red_GedreenLight();

        releaseSoundPool();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
