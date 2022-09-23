package com.baidu.idl.face.main.setting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.facesdkdemo.R;

public class RelayActivity extends BaseActivity {
    private EditText rmtEtThreshold;
    private int initValue;
    private int zero = 1;
    private static final int hundered = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        initValue = SingleBaseConfig.getBaseConfig().getRelayTime();
        init();
    }

    private void init() {
        Button rmtDecrease = findViewById(R.id.rmt_Decrease);
        Button rmtIncrease = findViewById(R.id.rmt_Increase);
        rmtEtThreshold = findViewById(R.id.rmt_etthreshold);
        Button rmtSave = findViewById(R.id.rmt_save);

        rmtEtThreshold.setText(SingleBaseConfig.getBaseConfig().getRelayTime() + "");

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
            SingleBaseConfig.getBaseConfig().setRelayTime(Integer.valueOf(rmtEtThreshold.getText().toString()));
            GateConfigUtils.modityJson();
            finish();
        });
    }
}