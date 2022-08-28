package com.baidu.idl.face.main.activity.start;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceSDKManager;
import com.baidu.idl.face.main.activity.gate.FaceRGBGateActivity;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.RegisterConfigUtils;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StartActivity extends BaseActivity {

    private Context mContext;
    private final Handler mHandler = new Handler();
    private Future future;
    private boolean isDBLoad;
    private ProgressBar progressBar;
    private TextView progressText;
    private View progressGroup;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;
    private TextureView checkRBGTexture;
    private TextureView checkNIRTexture;
    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mContext = this;
        boolean isConfigExit = GateConfigUtils.isConfigExit(this);
        boolean isInitConfig = GateConfigUtils.initConfig();
        boolean isRegisterConfigExit = RegisterConfigUtils.isConfigExit(this);
        boolean isRegisterInitConfig = RegisterConfigUtils.initConfig();

        if (isInitConfig && isConfigExit
                && isRegisterInitConfig && isRegisterConfigExit
        ) {
            Toast.makeText(StartActivity.this, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(StartActivity.this, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            GateConfigUtils.modityJson();
            RegisterConfigUtils.modityJson();
        }

        initView();
        initLicense();

    }


    private void initView() {
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressGroup = findViewById(R.id.progress_group);

        checkRBGTexture = findViewById(R.id.check_rgb_texture);
        checkNIRTexture = findViewById(R.id.check_nir_texture);


        //接收激活后的状态
        LiveDataBus.get().with("ActiveState", Boolean.class).observe(this, (Observer<Boolean>) flag -> {
            if (flag) {
                //开启服务
                Log.e("TAG", "开启服务: " );
                openService();
            }
        });
    }

    private void openService() {

        initRGBCheck();
        initDBApi();

        //加载FaceRGBGateActivity
        mHandler.postDelayed(() -> {
            startActivity(new Intent(StartActivity.this, FaceRGBGateActivity.class));
        }, 10 * 1000);
    }

    private void initLicense() {
        FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
            @Override
            public void initStart() {

            }

            public void initLicenseSuccess() {

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        /**
                         *要执行的操作
                         */
                        //授权成功
//                        startActivity(new Intent(mContext, HomeActivity.class));
//                        finish();
                        openService();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        /**
                         *要执行的操作
                         */
                        //跳转授权页面
                        startActivity(new Intent(mContext, ActivitionActivity.class));
//                        finish();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }

            @Override
            public void initModelSuccess() {
            }

            @Override
            public void initModelFail(int errorCode, String msg) {

            }
        });
    }

    private void initDBApi() {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        isDBLoad = false;
        future = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                FaceApi.getInstance().init(new DBLoadListener() {

                    @Override
                    public void onStart(int successCount) {
                        if (successCount < 5000 && successCount != 0) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadProgress(10);
                                }
                            });
                        }
                    }

                    @Override
                    public void onLoad(final int finishCount, final int successCount, final float progress) {
                        if (successCount > 5000 || successCount == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress((int) (progress * 100));
                                    progressText.setText(((int) (progress * 100)) + "%");
                                }
                            });
                        }
                    }

                    @Override
                    public void onComplete(final List<User> users, final int successCount) {
//                        FileUtils.saveDBList(HomeActivity.this, users);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
                                if (successCount > 5000 || successCount == 0) {
                                    progressGroup.setVisibility(View.GONE);
                                    isDBLoad = true;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(final int finishCount, final int successCount, final List<User> users) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
                                progressGroup.setVisibility(View.GONE);
                                ToastUtils.toast(StartActivity.this,
                                        "人脸库加载失败,共" + successCount + "条数据, 已加载" + finishCount + "条数据");
                                isDBLoad = true;
                            }
                        });
                    }
                }, mContext);
            }
        });
    }

    private void loadProgress(float i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress((int) ((i / 5000f) * 100));
                progressText.setText(((int) ((i / 5000f) * 100)) + "%");
                if (i < 5000) {
                    loadProgress(i + 100);
                } else {
                    progressGroup.setVisibility(View.GONE);
                    isDBLoad = true;
                }
            }
        }, 10);
    }


    private void initRGBCheck() {
        if (isSetCameraId()) {
            return;
        }
        int mCameraNum = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
        }
        if (mCameraNum > 1) {
            try {
                mCamera = new Camera[mCameraNum];
                previewTextures = new PreviewTexture[mCameraNum];
                mCamera[0] = Camera.open(0);
                previewTextures[0] = new PreviewTexture(this, checkRBGTexture);
                previewTextures[0].setCamera(mCamera[0], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(0);
                        }
                        release(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera[1] = Camera.open(1);
                previewTextures[1] = new PreviewTexture(this, checkNIRTexture);
                previewTextures[1].setCamera(mCamera[1], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1) {
                            setRgbCameraId(1);
                        }
                        release(1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setRgbCameraId(0);
        }
    }

    private void setRgbCameraId(int index) {
        SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        GateConfigUtils.modityJson();

    }

    private boolean isSetCameraId() {
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.registerlibrary.user.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1) {
            return false;
        } else {
            return true;
        }
    }

    private void release(int id) {
        if (mCamera != null && mCamera[id] != null) {
            if (mCamera[id] != null) {
                mCamera[id].setPreviewCallback(null);
                mCamera[id].stopPreview();
                previewTextures[id].release();
                mCamera[id].release();
                mCamera[id] = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release(0);
        release(1);

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        FaceApi.getInstance().cleanRecords();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
