package com.baidu.idl.face.main.setting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.facesdkdemo.R;

/**
 * ibeacon信号强度值
 */
public class IbeaconSignalValueActivity extends BaseActivity {

    private EditText rmtEtThreshold;
    private int initValue;
    private int zero = 10;
    private static final int hundered = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ibeacon_signal_value);

        //初始值
        initValue = SingleBaseConfig.getBaseConfig().getSignalStrength();
        init();
    }

    private void init() {
        Button rmtDecrease = findViewById(R.id.rmt_Decrease);
        Button rmtIncrease = findViewById(R.id.rmt_Increase);
        rmtEtThreshold = findViewById(R.id.rmt_etthreshold);
        Button rmtSave = findViewById(R.id.rmt_save);

        rmtEtThreshold.setText(SingleBaseConfig.getBaseConfig().getSignalStrength() + "");

        rmtDecrease.setOnClickListener(v -> {
            if (initValue > zero && initValue <= hundered) {
                initValue = initValue - 1;
                rmtEtThreshold.setText(initValue + "");
            }
        });

        rmtIncrease.setOnClickListener(v -> {
            if (initValue >= zero && initValue < hundered) {
                initValue = initValue + 1;
                rmtEtThreshold.setText(initValue + "");
            }
        });

        rmtSave.setOnClickListener(v -> {
            SingleBaseConfig.getBaseConfig().setSignalStrength(Integer.valueOf(rmtEtThreshold.getText().toString()));
            GateConfigUtils.modityJson();
            finish();
        });
    }
}