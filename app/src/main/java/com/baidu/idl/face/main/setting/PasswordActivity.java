package com.baidu.idl.face.main.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;

public class PasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText oldPassword, newPassword, rewritePassword;
    private Button submit;

    private ImageView qc_save;
    private String settingPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        initView();
        initdeta();
    }

    private void initView() {
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        rewritePassword = findViewById(R.id.rewritePassword);
        submit = findViewById(R.id.submit);
        qc_save = findViewById(R.id.qc_save);

        submit.setOnClickListener(this);
        qc_save.setOnClickListener(this);
    }

    private void initdeta() {
        settingPassword = (String) SPUtils.get(this, "setting_psw", "");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.submit) {
            String oldPas = oldPassword.getText().toString().trim();
            String newPas = newPassword.getText().toString().trim();
            String rewritePas = rewritePassword.getText().toString().trim();

            if (oldPas.isEmpty() || newPas.isEmpty() || rewritePas.isEmpty()) {
                ToastUtils.toast(this, "请输入密码！");
                return;
            }

            if (!oldPas.equals(settingPassword)) {
                ToastUtils.toast(this, "原密码不正确！");
                return;
            }
            if (!newPas.equals(rewritePas)) {
                ToastUtils.toast(this, "两次密码不一致！");
                return;
            }

            SPUtils.put(this, "setting_psw", newPas);
            finish();

        } else if (view.getId() == R.id.qc_save) {
            finish();
        }

    }
}