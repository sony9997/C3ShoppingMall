package com.c3.jbz.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.activity.MainActivity;
import com.c3.jbz.activity.MessagesActivity;
import com.c3.jbz.app.C3App;
import com.c3.jbz.db.AppDatabase;
import com.c3.jbz.db.DateConverter;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.MessageInfo;
import com.c3.jbz.vo.Notice;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import cn.jpush.android.api.JPushInterface;

/**
 * @author hedong
 * @date 2018/1/26
 */

public final class MessagePresenter {
    private static final String tag="message";
    private MessagesActivity messagesActivity;
    private AppDatabase appDatabase;
    public static final String KEY_SHOW_REDDOT_FORMAT="KEY_SHOW_REDDOT_%d";

    public MessagePresenter(MessagesActivity messagesActivity) {
        this.messagesActivity = messagesActivity;
        appDatabase = C3App.app.getAppDatabase();
    }

    /**
     * 解析从push消息推送来的数据
     *
     * @param bundle
     */
    public void parseBunlde(final Bundle bundle) {
        Log.d(tag,"parseBunlde:"+bundle);
        if (bundle != null) {
            final int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
            String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (!TextUtils.isEmpty(extra)) {
                try {
                    JSONObject jsonObject = new JSONObject(extra);
                    final int type = jsonObject.getInt(BuildConfig.KEY_MSG_TYPE);
                    Object addData=null;
                    switch (type) {
                        case BuildConfig.MSG_TYPE_NORMAL: {
                            String title= jsonObject.has("title")?jsonObject.getString("title"):null;
                            String body = jsonObject.has("body")?jsonObject.getString("body"):null;
                            String head = jsonObject.has("head")?jsonObject.getString("head"):null;
                            String foot = jsonObject.has("foot")?jsonObject.getString("foot"):null;
                            String clickLink = jsonObject.has("clickLink")?jsonObject.getString("clickLink"):null;
                            long date = jsonObject.has("date")?jsonObject.getLong("date"):0;
                            if(date==0){
                                date=System.currentTimeMillis();
                            }
                            final MessageInfo messageInfo = new MessageInfo(msgId,title, body,head,foot, DateConverter.toDate(date),
                                    clickLink, notificationId, LocalDateTime.now());
                            AppExecutors.as().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.messageInfoDao().insertMessageInfo(messageInfo);
                                }
                            });
                            addData=messageInfo;
                            break;
                        }
                        case BuildConfig.MSG_TYPE_NOTICE: {
                            String title= jsonObject.has("title")?jsonObject.getString("title"):null;
                            long date = jsonObject.has("date")?jsonObject.getLong("date"):0;
                            if(date==0){
                                date=System.currentTimeMillis();
                            }
                            String clickLink = jsonObject.has("clickLink")?jsonObject.getString("clickLink"):null;
                            final Notice notice=new Notice(msgId,title,DateConverter.toDate(date),clickLink,notificationId,LocalDateTime.now());
                            AppExecutors.as().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.noticeDao().insertNotice(notice);
                                }
                            });
                            addData=notice;
                            break;
                        }
                        case BuildConfig.MSG_TYPE_LOGISTICS: {
                            break;
                        }
                        default:
                            Log.w(tag,"unknow type:"+type);
                            return;
                    }
                    updateRedDotState(type,true);
                    messagesActivity.selectTab(type, notificationId);
                    if(addData!=null){
                        messagesActivity.addData2SubFragment(addData,type);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateRedDotState(int position,boolean show){
        ShareDataLocal.as().setBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,position),show);
        messagesActivity.updateRedDotState(position,show);
    }

    public static final boolean isRedDotNeedShow(int position){
        return ShareDataLocal.as().getBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,position));
    }

    public static final boolean isRedDotNeedShow(){
        int max=10;
        for(int i=0;i<max;i++){
            String key=String.format(KEY_SHOW_REDDOT_FORMAT,i);
            if(!ShareDataLocal.as().containsKey(key)){
                continue;
            }
            if(ShareDataLocal.as().getBooleanValue(key)){
                return true;
            }
        }
        return false;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public MessagesActivity getMessagesActivity() {
        return messagesActivity;
    }

    public void openMainActivity(String url){
        Intent intent=new Intent(messagesActivity, MainActivity.class);
        intent.putExtra(BuildConfig.KEY_OTHER_URL,url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        messagesActivity.startActivity(intent);
    }
}
