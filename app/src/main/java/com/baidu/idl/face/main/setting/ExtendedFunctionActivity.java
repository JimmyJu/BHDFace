package com.baidu.idl.face.main.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * 拓展 蓝牙、二维码
 */
public class ExtendedFunctionActivity extends BaseActivity implements View.OnClickListener {
    private Switch mSwitchBluetooth, mSwitchVisitor;
    private Button mButtonSave;
    private int zero = 0;
    private int one = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_function);
        initView();
        initData();
    }

    private void initView() {
        mSwitchBluetooth = findViewById(R.id.switch_bluetooth);
        mSwitchVisitor = findViewById(R.id.switch_visitor);
        mButtonSave = findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(this);
    }

    private void initData() {
        if (SingleBaseConfig.getBaseConfig().getBluetoothSwitch() == zero) {//关闭蓝牙扫描功能
            mSwitchBluetooth.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getBluetoothSwitch() == one) {//打开蓝牙扫描功能
            mSwitchBluetooth.setChecked(true);
        }

        if (SingleBaseConfig.getBaseConfig().getVisitorSwitch() == zero) {//访客关闭
            mSwitchVisitor.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getVisitorSwitch() == one) {//访客开启
            mSwitchVisitor.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_save) {
            if (mSwitchBluetooth.isChecked()) {
                SingleBaseConfig.getBaseConfig().setBluetoothSwitch(one);
            } else {
                SingleBaseConfig.getBaseConfig().setBluetoothSwitch(zero);
            }
            if (mSwitchVisitor.isChecked()) {
                SingleBaseConfig.getBaseConfig().setVisitorSwitch(one);
            } else {
                SingleBaseConfig.getBaseConfig().setVisitorSwitch(zero);
            }

            GateConfigUtils.modityJson();
            finish();
        }
    }
}
