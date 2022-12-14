package com.baidu.idl.face.main.activity.start;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.UserManagerActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewRgbNirDepthActivity;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private Handler mHandler = new Handler();
    private PopupWindow popupWindow;
    private View view1;
    private RelativeLayout layout_home;
    private RelativeLayout home_personRl;
    private int mLiveType;
    private PopupWindow mPopupMenu;
    private PopupWindow mPopupMenuFirst;
    private ImageView home_menuImg;
    private boolean isCheck = false;
    private TextView home_dataTv;

    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;
    private TextureView checkRBGTexture;
    private TextureView checkNIRTexture;
    private ProgressBar progressBar;
    private TextView progressText;
    private View progressGroup;
    private boolean isDBLoad;
    private Future future;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mContext = this;
        initView();

        initRGBCheck();
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            mHandler.postDelayed(mRunnable, 500);
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }
        initUserManagePopupWindow();
        initDBApi();
        //??????????????????
//        startActivity(new Intent(HomeActivity.this, FaceRGBGateActivity.class));
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
                                ToastUtils.toast(HomeActivity.this,
                                        "?????????????????????,???" + successCount + "?????????, ?????????" + finishCount + "?????????");
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

    private void initFirstPopupWindowTip() {
        home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu_first);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_home_first, null);
        mPopupMenuFirst = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenuFirst.setFocusable(true);
        mPopupMenuFirst.setOutsideTouchable(true);
        mPopupMenuFirst.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round_frist));

        mPopupMenuFirst.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isCheck = false;
                home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu);
            }
        });
        mPopupMenuFirst.setContentView(contentView);

        if (mPopupMenuFirst != null && home_menuImg != null) {
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);
            mPopupMenuFirst.showAtLocation(home_menuImg, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private Runnable mRunnable = new Runnable() {
        public void run() {
            // ??????PopupWindow???????????????
            initPopupWindow();
            initFirstPopupWindowTip();
        }
    };

    private void initPopupWindow() {

        view1 = View.inflate(mContext, R.layout.layout_popup, null);
        popupWindow = new PopupWindow(view1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // ????????????????????????popupwindow??????
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(layout_home, Gravity.CENTER, 0, 0);
        initHandler();
    }

    /**
     * ?????????????????????PopupWindow
     */
    private void initUserManagePopupWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_home, null);
        mPopupMenu = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round));

        RelativeLayout relativeRegister = contentView.findViewById(R.id.relative_register);
        RelativeLayout mPopRelativeManager = contentView.findViewById(R.id.relative_manager);
        relativeRegister.setOnClickListener(this);
        mPopRelativeManager.setOnClickListener(this);
        mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isCheck = false;
                home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu);
            }
        });
        mPopupMenu.setContentView(contentView);
    }

    private void showPopupWindow(ImageView imageView) {
        if (mPopupMenu != null && imageView != null) {
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);
            mPopupMenu.showAtLocation(imageView, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    private void dismissPopupWindow() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
    }

    private void initHandler() {
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                // ??????????????????
                popupWindow.dismiss();
                return false;
            }
        }).sendEmptyMessageDelayed(0, 3000);
    }

    private void initView() {
        //????????????
        RelativeLayout home_register = findViewById(R.id.home_register);
        home_register.setOnClickListener(this);

        RelativeLayout home_staticRl = findViewById(R.id.home_staticRl);
        home_staticRl.setOnClickListener(this);

        layout_home = findViewById(R.id.layout_home);
        ImageView home_settingImg = findViewById(R.id.home_settingImg);
        home_settingImg.setOnClickListener(this);
        home_menuImg = findViewById(R.id.home_menuImg);
        home_menuImg.setOnClickListener(this);
        RelativeLayout home_gateRl = findViewById(R.id.home_gateRl);
        home_gateRl.setOnClickListener(this);
        RelativeLayout home_checkRl = findViewById(R.id.home_checkRl);
        home_checkRl.setOnClickListener(this);
        RelativeLayout home_payRl = findViewById(R.id.home_payRl);
        home_payRl.setOnClickListener(this);
        RelativeLayout home_livenessRl = findViewById(R.id.home_livenessRl);
        home_livenessRl.setOnClickListener(this);
        RelativeLayout home_attributeRl = findViewById(R.id.home_attributeRl);
        home_attributeRl.setOnClickListener(this);
        home_personRl = findViewById(R.id.home_personRl);
        home_personRl.setOnClickListener(this);
        RelativeLayout home_driveRl = findViewById(R.id.home_driveRl);
        home_driveRl.setOnClickListener(this);
        RelativeLayout home_attentionRl = findViewById(R.id.home_attentionRl);
        home_attentionRl.setOnClickListener(this);
        ImageView home_faceTv = findViewById(R.id.home_faceTv);
        home_faceTv.setOnClickListener(this);
        ImageView home_faceLibraryTv = findViewById(R.id.home_faceLibraryTv);
        home_faceLibraryTv.setOnClickListener(this);
        home_dataTv = findViewById(R.id.home_dataTv);
        home_dataTv.setText("????????????" + FaceSDKManager.getInstance().getLicenseData(this));
        checkRBGTexture = findViewById(R.id.check_rgb_texture);
        checkNIRTexture = findViewById(R.id.check_nir_texture);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressGroup = findViewById(R.id.progress_group);
    }

    @Override
    public void onClick(View view) {
        if (!isDBLoad) {
            return;
        }
        switch (view.getId()) {
            case R.id.home_menuImg:
                if (!isCheck) {
                    isCheck = true;
                    home_menuImg.setImageResource(R.mipmap.icon_titlebar_menu_hl);
                    showPopupWindow(home_menuImg);
                } else {
                    dismissPopupWindow();
                }
                break;
            case R.id.relative_register:
                // ????????????
                dismissPopupWindow();
                SharedPreferences sharedPreferences = this.getSharedPreferences("type", MODE_PRIVATE);
                mLiveType = sharedPreferences.getInt("type", 0);
                com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().setActiveLog();
                judgeLiveType(mLiveType, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                        FaceRegisterNewDepthActivity.class, FaceRegisterNewRgbNirDepthActivity.class);
                break;
            case R.id.relative_manager:
                // ???????????????
                dismissPopupWindow();
                startActivity(new Intent(HomeActivity.this, UserManagerActivity.class));
                break;
            case R.id.home_gateRl:
                mLiveType = SingleBaseConfig.getBaseConfig().getType();
                // ????????????
//                judgeLiveType(mLiveType,
//                        FaceRGBGateActivity.class,
//                        FaceNIRGateActivriy.class,
//                        FaceDepthGateActivity.class,
//                        FaceRgbNirDepthGataActivity.class);
                break;

            case R.id.home_register:
                //????????????
                startActivity(new Intent(HomeActivity.this, SDCardFileImportOffline.class));
                break;

            case R.id.home_staticRl:
                //????????????
                startActivity(new Intent(HomeActivity.this, SDCardFileImportOfflineCompare.class));
                break;
        }
    }


    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls) {
        switch (type) {
            case 0: { // ???????????????
                startActivity(new Intent(HomeActivity.this, rgbCls));
                break;
            }

            case 1: { // RGB??????
                startActivity(new Intent(HomeActivity.this, rgbCls));
                break;
            }

//            case 2: { // NIR??????
//                startActivity(new Intent(HomeActivity.this, nirCls));
//                break;
//            }

//            case 3: { // ????????????
//                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
//                judgeCameraType(cameraType, depthCls);
//                break;
//            }
//
//            case 4: { // rgb+nir+depth??????
//                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
//                judgeCameraType(cameraType, rndCls);
//            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

//            default:
//                startActivity(new Intent(HomeActivity.this, depthCls));
//                break;
        }
    }

}
