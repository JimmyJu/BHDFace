package com.baidu.idl.face.main.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.facesdkdemo.R;

public class ServiceSettingActivity extends BaseActivity implements View.OnClickListener {
    private ImageView qcSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_setting);

        initView();
    }

    private void initView() {
        //门禁服务配置
        LinearLayout access_control_service = findViewById(R.id.access_control_service);
        access_control_service.setOnClickListener(this);

        qcSave = findViewById(R.id.qc_save);
        qcSave.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.access_control_service) {

            startActivity(new Intent(ServiceSettingActivity.this, IpAddressActivity.class));

        } else if (id == R.id.qc_save) {
            finish();
        }

    }
}