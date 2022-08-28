package com.baidu.idl.face.main.activity.gate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
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
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LogUtilsDynamic;
import com.baidu.idl.face.main.utils.NavigationBarUtil;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.example.datalibrary.model.User;


public class FaceRGBGateActivity extends BaseActivity implements View.OnClickListener {

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

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private RelativeLayout textHuanying;
    private ImageView nameImage;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private Paint paintBg;
    private View view;
    private TextView logoText;
    private User mUser;
    private boolean isTime = true;
    private long startTime;
    private boolean detectCount;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private DoubleClickListener doubleClickListener;

    //动态对比数
    private int faceCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_gate);
        initView();
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
        logoText = findViewById(R.id.logo_text);
        logoText.setVisibility(View.VISIBLE);

        // 左上logo
        ImageView mButReturn = findViewById(R.id.switchPort);
        mButReturn.setOnClickListener(this);
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
        view = findViewById(R.id.mongolia_view);
        view.setAlpha(0.85f);
        view.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
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
                                        checkCloseDebugResult(livenessModel);

                                        // 开发模式
//                                        checkOpenDebugResult(livenessModel);

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
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    // 对背景色颜色进行改变，操作的属性为"alpha",此处必须这样写
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
                    return;
                }
                isTime = true;
                if (detectCount) {
                    detectCount = false;
                    objectAnimator();
                } else {
                    view.setVisibility(View.GONE);
                }


                User user = livenessModel.getUser();
//                float score = user.getScore();
                if (user == null) {
                    mUser = null;
                    if (livenessModel.isMultiFrame()) {
                        textHuanying.setVisibility(View.GONE);
                        userNameLayout.setVisibility(View.VISIBLE);
                        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
                        nameText.setTextColor(Color.parseColor("#fec133"));
                        nameText.setText("抱歉 未能认出您");
                    } else {
                        textHuanying.setVisibility(View.VISIBLE);
                        userNameLayout.setVisibility(View.GONE);
                    }
                } else {
                    String userName = user.getUserName();
                    String userName_data = (String) SPUtils.get(FaceRGBGateActivity.this, "userName", "");
                    flag = userName.equals(userName_data);

                    if (!flag) {
                        flag = true;
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
                }

            }
        });
    }

    private boolean flag = false;


    @Override
    public void onClick(View v) {
        int id = v.getId();// logo
        if (id == R.id.switchPort) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
//            finish();
            // 设置
        }
//        else if (id == R.id.menu_btn) {
//            if (!FaceSDKManager.initModelSuccess) {
//                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
//                        Toast.LENGTH_LONG).show();
//                return;
//            }
//            startActivity(new Intent(mContext, GateSettingActivity.class));
//            finish();
//        }

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
    private void objectAnimator() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
