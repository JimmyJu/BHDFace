package com.baidu.idl.face.main.activity.start;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceSDKManager;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.service.TcpService;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.RegisterConfigUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;
import com.example.yfaceapi.GPIOManager;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.opencv.OpenCV;

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
    private Intent mIntent;

    //蓝牙适配器
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;


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

        //设置低功耗蓝牙
        setupBLE();

        //激活许可证
//        initLicense();
    }


    private void initView() {
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressGroup = findViewById(R.id.progress_group);

        //接收激活后的状态
        LiveDataBus.get().with("ActiveState", Boolean.class).observe(this, (Observer<Boolean>) flag -> {
            if (flag) {
                //开启服务
                Log.e("TAG", "开启服务: ");
                openService();
            }
        });
    }

    private void openService() {
        //db初始化
        initDBApi();
        //注册库初始化
        initListener();
        //初始化 OpenCV 和 WeChatQRCodeDetector
        initWeChatQR();

        //开启socket以及串口，先判断防止重复开启服务
        if (!Utils.isServiceRunning(mContext, "com.baidu.idl.face.main.service.TcpService")) {
            mIntent = new Intent(StartActivity.this, TcpService.class);
            startService(mIntent);
        }

        //加载FaceRGBGateActivity
        mHandler.postDelayed(() -> {
//            startActivity(new Intent(StartActivity.this, FaceRGBGateActivity.class));
            startActivity(new Intent(StartActivity.this, TranslucentActivity.class));

        }, 8 * 1000);
    }

    /**
     * 初始化许可证
     */
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


    //
    private void initListener() {
        if (com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.initStatus != com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().initModel(this, new com.baidu.idl.main.facesdk.registerlibrary.user.listener.SdkInitListener() {
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
                    com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.initModelSuccess = true;
                    com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils.toast(mContext, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils.toast(mContext, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    private void initWeChatQR() {
        //初始化OpenCV
        OpenCV.initAsync(mContext);

        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(mContext);
    }

    /**
     * 设置低功耗蓝牙
     */
    private void setupBLE() {
        //检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "本机不支持低功耗蓝牙!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BluetoothManager manager = getManager(this);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }
        if ((bluetoothAdapter == null) || (!bluetoothAdapter.isEnabled())) {
            //调用系统对话框启动本地蓝牙
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        } else {
            initLicense();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothManager getManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //蓝牙已打开
                initLicense();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        FaceApi.getInstance().cleanRecords();

        GPIOManager.getInstance(this).pullDownRedLight();
        GPIOManager.getInstance(this).pullDownGreenLight();
        GPIOManager.getInstance(this).pullDownWhiteLight();

        stopService(mIntent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
