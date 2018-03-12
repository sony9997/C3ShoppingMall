package com.c3.jbz.comp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.service.PushService;

/**
 * @author hedong
 * @date 2018/2/27
 */

public class PushServiceStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent pushintent=new Intent(context,PushService.class);//启动极光推送的服务
//        context.startService(pushintent);
        // 初始化 JPush
//        JPushInterface.init(context);
    }
}
