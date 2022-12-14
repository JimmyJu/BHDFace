package com.baidu.idl.face.main.setting;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.GateConfigUtils;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;

import java.util.Calendar;

/**
 * 白色补光灯控制开关
 */
public class LightActivity extends BaseActivity implements View.OnClickListener {

    private Switch mSwitchFillLight;
    private Button mButton_light_save;
    private TextView mTv_light_start, mTv_light_end;


    private int zero = 0;
    private int one = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        initView();
        initdeta();
    }


    private void initView() {
        mSwitchFillLight = findViewById(R.id.switch_fillLight);
        mButton_light_save = findViewById(R.id.button_light_save);
        mTv_light_start = findViewById(R.id.tv_light_start);
        mTv_light_end = findViewById(R.id.tv_light_end);

        mButton_light_save.setOnClickListener(this);
        mTv_light_start.setOnClickListener(this);
        mTv_light_end.setOnClickListener(this);
    }

    private void initdeta() {
        if (SingleBaseConfig.getBaseConfig().getLightSwitch() == zero) {  //关闭白色补光灯
            mSwitchFillLight.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getLightSwitch() == one) {    //开启白色补光灯
            mSwitchFillLight.setChecked(true);
        }


        String light_start_str = (String) SPUtils.get(this, "light_start_str", "");
        String light_end_str = (String) SPUtils.get(this, "light_end_str", "");

        if (light_start_str != null && light_end_str != null) {
            mTv_light_start.setText(light_start_str);
            mTv_light_end.setText(light_end_str);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_light_save) {
            if (mSwitchFillLight.isChecked()) {
                SingleBaseConfig.getBaseConfig().setLightSwitch(one);
            } else {
                SingleBaseConfig.getBaseConfig().setLightSwitch(zero);
            }

            GateConfigUtils.modityJson();

            finish();
        } else if (v.getId() == R.id.tv_light_start) {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {

                String desc = String.format("您选择的开始时间是%s时%s分",
                        Utils.addTimeZero(String.valueOf(hourOfDay)), Utils.addTimeZero(String.valueOf(minute)));
                mTv_light_start.setText(desc);
                SPUtils.put(LightActivity.this, "light_start_str", desc);
                SPUtils.put(LightActivity.this, "light_start", Utils.addTimeZero(String.valueOf(hourOfDay)) + ":" + Utils.addTimeZero(String.valueOf(minute)));

            },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);//true表示使用二十四小时制
            dialog.show();
        } else if (v.getId() == R.id.tv_light_end) {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String desc = String.format("您选择的关闭时间是%s时%s分",
                        Utils.addTimeZero(String.valueOf(hourOfDay)), Utils.addTimeZero(String.valueOf(minute)));
                mTv_light_end.setText(desc);
                SPUtils.put(LightActivity.this, "light_end_str", desc);
                SPUtils.put(LightActivity.this, "light_end", Utils.addTimeZero(String.valueOf(hourOfDay)) + ":" + Utils.addTimeZero(String.valueOf(minute)));
            },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);//true表示使用二十四小时制
            dialog.show();
        }
    }
}