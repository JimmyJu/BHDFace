package com.baidu.idl.face.main.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.gate.FaceRGBGateActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.PreferencesManager;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.license.BDFaceLicenseAuthInfo;
import com.example.yfaceapi.GPIOManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 人脸闸机模式设置页面
 */
public class GateSettingActivity extends BaseActivity implements View.OnClickListener {

    private ImageView gateSetttingBack;
    private LinearLayout gateFaceDetection;
    private LinearLayout gateConfigQualtify;
    private LinearLayout gateHuotiDetection;
    private LinearLayout gateFaceRecognition;
    private LinearLayout gateLensSettings;
    private LinearLayout offline_peration;
    private LinearLayout service_configuration;
    private LinearLayout other;
    private View gatePictureOptimization;
    private View gateLogSettings;
    private TextView tvSettingQualtify;
    private TextView logSettingQualtify;
    private TextView tvSettingLiviness;
    private LinearLayout configVersionMessage;
    private TextView tvSettingEffectiveDate;
    private FaceAuth faceAuth;

    private Handler mHandler = new Handler();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_setting);
        mContext = this;
        init();
    }

    private void init() {
        faceAuth = new FaceAuth();
        // 返回
        gateSetttingBack = findViewById(R.id.gate_settting_back);
        gateSetttingBack.setOnClickListener(this);
        // 人脸检测
        gateFaceDetection = findViewById(R.id.gate_face_detection);
        gateFaceDetection.setOnClickListener(this);
        // 质量检测
        gateConfigQualtify = findViewById(R.id.gate_config_qualtify);
        gateConfigQualtify.setOnClickListener(this);
        // 活体检测
        gateHuotiDetection = findViewById(R.id.gate_huoti_detection);
        gateHuotiDetection.setOnClickListener(this);
        // 人脸识别
        gateFaceRecognition = findViewById(R.id.gate_face_recognition);
        gateFaceRecognition.setOnClickListener(this);
        // 镜头设置
        gateLensSettings = findViewById(R.id.gate_lens_settings);
        gateLensSettings.setOnClickListener(this);
        // 图像优化
        gatePictureOptimization = findViewById(R.id.gate_picture_optimization);
        gatePictureOptimization.setOnClickListener(this);
        //离线操作
        offline_peration = findViewById(R.id.offline_peration);
        offline_peration.setOnClickListener(this);

        //服务配置
        service_configuration = findViewById(R.id.service_configuration);
        service_configuration.setOnClickListener(this);

        //其它
        other = findViewById(R.id.other);
        other.setOnClickListener(this);


        // 日志设置
        gateLogSettings = findViewById(R.id.gate_log_settings);
        gateLogSettings.setOnClickListener(this);
        // 版本信息
        configVersionMessage = findViewById(R.id.configVersionMessage);
        configVersionMessage.setOnClickListener(this);
        tvSettingQualtify = findViewById(R.id.tvSettingQualtify);
        logSettingQualtify = findViewById(R.id.logSettingQualtify);
        tvSettingLiviness = findViewById(R.id.tvSettingLiviness);

        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //系统log
        if (SingleBaseConfig.getBaseConfig().isLog()) {
            logSettingQualtify.setText("开启");
        } else {
            logSettingQualtify.setText("关闭");
        }
        //质量控制开关
        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            tvSettingQualtify.setText("开启");
        } else {
            tvSettingQualtify.setText("关闭");
        }
        //是否开启活体检测开关
        if (SingleBaseConfig.getBaseConfig().isLivingControl()) {
            tvSettingLiviness.setText("开启");
        } else {
            tvSettingLiviness.setText("关闭");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        BDFaceLicenseAuthInfo bdFaceLicenseAuthInfo = faceAuth.getAuthInfo(this);
        Date dateLong = new Date(bdFaceLicenseAuthInfo.expireTime * 1000L);
        String dateTime = simpleDateFormat.format(dateLong);

        tvSettingEffectiveDate.setText("有效期至" + dateTime);
    }

    @Override
    protected void onStart() {
        super.onStart();
        delayCloseLight();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.gate_settting_back) {
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setType(SingleBaseConfig.getBaseConfig().getType());

            startActivity(new Intent(GateSettingActivity.this, FaceRGBGateActivity.class));
            finish();
        } else if (id == R.id.gate_face_detection) {
            Intent intent = new Intent(GateSettingActivity.this, GateMinFaceActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_config_qualtify) {
            Intent intent = new Intent(GateSettingActivity.this, GateConfigQualtifyActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_huoti_detection) {
            Intent intent = new Intent(GateSettingActivity.this, FaceLivinessTypeActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_face_recognition) {
            Intent intent = new Intent(GateSettingActivity.this, GateFaceDetectActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_lens_settings) {
            Intent intent = new Intent(GateSettingActivity.this, GateLensSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.configVersionMessage) {
            Intent intent = new Intent(GateSettingActivity.this, VersionMessageActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_picture_optimization) {
            Intent intent = new Intent(GateSettingActivity.this, PictureOptimizationActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_log_settings) {
            Intent intent = new Intent(GateSettingActivity.this, LogSettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.offline_peration) {
            startActivity(new Intent(GateSettingActivity.this, OfflinePerationSettingActivity.class));
        } else if (id == R.id.service_configuration) {
            startActivity(new Intent(GateSettingActivity.this, ServiceSettingActivity.class));
        } else if (id == R.id.other) {
            startActivity((new Intent(GateSettingActivity.this, OtherSettingActivity.class)));
        }
    }

    private void delayCloseLight() {
        mHandler.postDelayed(() -> {
            GPIOManager.getInstance(mContext).pullDownRedLight();
            GPIOManager.getInstance(mContext).pullDownGreenLight();
            GPIOManager.getInstance(mContext).pullDownWhiteLight();
        }, 2000);
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}