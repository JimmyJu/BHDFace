package com.baidu.idl.face.main.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.facesdkdemo.R;

/**
 * 其它设置 照片上传、检测、继电器时间、
 */
public class OtherSettingActivity extends BaseActivity implements View.OnClickListener {
    private ImageView qcSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_setting);

        initView();
    }

    private void initView() {
        //photo_detection
        LinearLayout photo_detection = findViewById(R.id.photo_detection);
        photo_detection.setOnClickListener(this);

        LinearLayout photo_upload = findViewById(R.id.photo_upload);
        photo_upload.setOnClickListener(this);

        LinearLayout ll_relayTime = findViewById(R.id.ll_relayTime); //设置继电器时间值
        ll_relayTime.setOnClickListener(this);

        qcSave = findViewById(R.id.qc_save);
        qcSave.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.photo_detection) {

            startActivity(new Intent(OtherSettingActivity.this, SDCardFileExplorerTestActivity.class));

        } else if (id == R.id.qc_save) {
            finish();
        } else if (id == R.id.photo_upload) {
            startActivity(new Intent(OtherSettingActivity.this, SDCardFileExplorerActivity.class));
        } else if (id == R.id.ll_relayTime) {
            startActivity(new Intent(OtherSettingActivity.this, RelayActivity.class));
        }

    }
}