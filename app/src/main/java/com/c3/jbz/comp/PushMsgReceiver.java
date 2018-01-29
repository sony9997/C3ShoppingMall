package com.c3.jbz.comp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.activity.MainActivity;
import com.c3.jbz.activity.MessagesActivity;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.ToolsUtil;

import cn.jpush.android.api.JPushInterface;

public class PushMsgReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        /**
         * 用户点击了通知。 一般情况下，用户不需要配置此 receiver action。
         如果开发者在 AndroidManifest.xml 里未配置此 receiver action，那么，SDK 会默认打开应用程序的主 Activity，相当于用户点击桌面图标的效果。
         如果开发者在 AndroidManifest.xml 里配置了此 receiver action，那么，当用户点击通知时，SDK 不会做动作。开发者应该在自己写的 BroadcastReceiver 类里处理，比如打开某 Activity
         */
        if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) {
            sendBundle2MessageActivity(context, intent);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)) {
            String className = ToolsUtil.getForegroundActivity(context);
            if (MessagesActivity.class.getName().equals(className)) {
                sendBundle2MessageActivity(context, intent);
            } else if (MainActivity.class.getName().equals(className)) {
                sendHaveMsg2MainActivity(context,intent);
            }
        }
    }

    public static final void sendBundle2MessageActivity(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Intent si = new Intent(context, MessagesActivity.class);
            si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            si.putExtras(bundle);
            context.startActivity(si);
        }
    }

    private void sendHaveMsg2MainActivity(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BuildConfig.KEY_HAVE_MSG));
        MessagePresenter messagePresenter=new MessagePresenter(null);
        messagePresenter.parseBunlde(intent.getExtras());
        messagePresenter=null;
    }
}
