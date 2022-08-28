package com.baidu.idl.face.main.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baidu.idl.face.main.activity.start.StartActivity;


public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent thisIntent = new Intent(context, StartActivity.class);//设置要启动的app
            thisIntent.setAction("android.intent.action.MAIN");
            thisIntent.addCategory("android.intent.category.LAUNCHER");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(thisIntent);
        }
    }
}
