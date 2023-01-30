package com.baidu.idl.face.main.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.start.SDCardFileImportOffline;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.UserManagerActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;

public class OfflinePerationSettingActivity extends BaseActivity implements View.OnClickListener {
    private ImageView qcSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_peration_setting);

        initView();
    }

    private void initView() {
        //离线资源导入
        LinearLayout offline_resource_import = findViewById(R.id.offline_resource_import);
        offline_resource_import.setOnClickListener(this);

        //人员库管理
        LinearLayout personnel_database_management = findViewById(R.id.personnel_database_management);
        personnel_database_management.setOnClickListener(this);

        //脱机人脸录入
        LinearLayout offline_face_entry = findViewById(R.id.offline_face_entry);
        offline_face_entry.setOnClickListener(this);

        qcSave = findViewById(R.id.qc_save);
        qcSave.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.offline_resource_import) {
            startActivity(new Intent(OfflinePerationSettingActivity.this, SDCardFileImportOffline.class));
        } else if (id == R.id.personnel_database_management) {
            startActivity(new Intent(OfflinePerationSettingActivity.this, UserManagerActivity.class));
        } else if (id == R.id.qc_save) {
            finish();
        } else if (id == R.id.offline_face_entry) {
            startActivity(new Intent(OfflinePerationSettingActivity.this, FaceRegisterNewActivity.class));
        }

    }
}