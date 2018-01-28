package com.c3.jbz.comp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.c3.jbz.activity.MessagesActivity;
import com.c3.jbz.util.ToolsUtil;

import cn.jpush.android.api.JPushInterface;

public class PushMsgReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        /**
         * 用户点击了通知。 一般情况下，用户不需要配置此 receiver action。
         如果开发者在 AndroidManifest.xml 里未配置此 receiver action，那么，SDK 会默认打开应用程序的主 Activity，相当于用户点击桌面图标的效果。
         如果开发者在 AndroidManifest.xml 里配置了此 receiver action，那么，当用户点击通知时，SDK 不会做动作。开发者应该在自己写的 BroadcastReceiver 类里处理，比如打开某 Activity
         */
        if(JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)){
            sendMsg(context,intent);
        }else if(JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)){
            if(ToolsUtil.isForeground(context,MessagesActivity.class.getName())){
                sendMsg(context,intent);
            }
        }
    }

    private void sendMsg(Context context,Intent intent){
        Bundle bundle=intent.getExtras();
        Log.d("message push receiver:","sendMsg:"+bundle);
        if(bundle!=null){
            Intent si=new Intent(context,MessagesActivity.class);
            si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            si.putExtras(bundle);
            context.startActivity(si);
        }
    }
}
