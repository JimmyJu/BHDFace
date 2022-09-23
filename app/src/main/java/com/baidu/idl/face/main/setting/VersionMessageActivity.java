package com.baidu.idl.face.main.setting;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.utils.NetWorkUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.license.BDFaceLicenseAuthInfo;

import java.text.SimpleDateFormat;
import java.util.Date;


public class VersionMessageActivity extends BaseActivity {
    private TextView sdkVersion;
    private TextView systemVersion;
    private TextView activateStatus;
    private TextView activateType;
    private TextView activateData;
    private ImageView buttonVersionSave;
    private FaceAuth faceAuth;

    private TextView current_device_ip, current_license_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versionmessage);

        init();
    }

    public void init() {
        faceAuth = new FaceAuth();
        buttonVersionSave = findViewById(R.id.button_version_save);
        sdkVersion = findViewById(R.id.sdkversion);
        systemVersion = findViewById(R.id.systemversion);
        activateStatus = findViewById(R.id.activatestatus);
        activateType = findViewById(R.id.activatetype);
        activateData = findViewById(R.id.activatedata);

        current_device_ip = findViewById(R.id.current_device_ip);
        current_license_key = findViewById(R.id.current_license_key);

        sdkVersion.setText(Utils.getVersionName(this));
        systemVersion.setText(android.os.Build.VERSION.RELEASE);
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            activateStatus.setText("未激活");
        } else {
            activateStatus.setText("已激活");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        BDFaceLicenseAuthInfo bdFaceLicenseAuthInfo = faceAuth.getAuthInfo(this);
        Date dateLong = new Date(bdFaceLicenseAuthInfo.expireTime * 1000L);
        String dateTime = simpleDateFormat.format(dateLong);
        activateData.setText(dateTime);

        //当前设备IP
        String substringDeviceID = NetWorkUtils.getLocalIpAddress();
        if (substringDeviceID != null) {
            String newString = substringDeviceID.replace(".", "");
            String newSubstringDeviceID = newString.substring(newString.length() - 6);

//            current_device_ip.setText(substringDeviceID + getString(R.string.ip_info) + newSubstringDeviceID);
            current_device_ip.setText(String.format(getString(R.string.ip_info),substringDeviceID,newSubstringDeviceID));
        }

        //当前许可证
        current_license_key.setText(bdFaceLicenseAuthInfo.licenseKey);

        buttonVersionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
