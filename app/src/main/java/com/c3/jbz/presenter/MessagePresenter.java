package com.c3.jbz.presenter;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.activity.MessagesActivity;
import com.c3.jbz.db.AppDatabase;
import com.c3.jbz.db.DateConverter;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.fragment.LogisticsFragment;
import com.c3.jbz.fragment.MessageFragment;
import com.c3.jbz.fragment.NoticeFragment;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.MessageInfo;
import com.c3.jbz.vo.Notice;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import cn.jpush.android.api.JPushInterface;

/**
 * @author hedong
 * @date 2018/1/26
 */

public final class MessagePresenter {
    private MessagesActivity messagesActivity;
    private AppDatabase appDatabase;
    public static final String KEY_SHOW_REDDOT_FORMAT="KEY_SHOW_REDDOT_%d";

    public MessagePresenter(MessagesActivity messagesActivity) {
        this.messagesActivity = messagesActivity;
        appDatabase = Room.databaseBuilder(messagesActivity.getApplicationContext(), AppDatabase.class, "cface.db").build();
    }

    /**
     * 解析从push消息推送来的数据
     *
     * @param bundle
     */
    public void parseBunlde(final Bundle bundle) {
        if (bundle != null) {
            AppExecutors.as().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                    String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
                    String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                    if (!TextUtils.isEmpty(extra)) {
                        try {
                            JSONObject jsonObject = new JSONObject(extra);
                            final int type = jsonObject.getInt(BuildConfig.KEY_MSG_TYPE);

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
                                    MessageInfo messageInfo = new MessageInfo(msgId,title, body,head,foot, DateConverter.toDate(date),
                                            clickLink, notificationId, LocalDateTime.now());
                                    appDatabase.messageInfoDao().insertMessageInfo(messageInfo);
                                    break;
                                }
                                case BuildConfig.MSG_TYPE_NOTICE: {
                                    String title= jsonObject.has("title")?jsonObject.getString("title"):null;
                                    long date = jsonObject.has("date")?jsonObject.getLong("date"):0;
                                    if(date==0){
                                        date=System.currentTimeMillis();
                                    }
                                    String clickLink = jsonObject.has("clickLink")?jsonObject.getString("clickLink"):null;
                                    Notice notice=new Notice(msgId,title,DateConverter.toDate(date),clickLink,notificationId,LocalDateTime.now());
                                    appDatabase.noticeDao().insertNotice(notice);
                                    break;
                                }
                                case BuildConfig.MSG_TYPE_LOGISTICS: {
                                    break;
                                }
                            }

                            AppExecutors.as().mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    updateRedDotState(type,true);
                                    messagesActivity.selectTab(type, notificationId);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void updateRedDotState(int position,boolean show){
        ShareDataLocal.as().setBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,position),true);
        messagesActivity.updateRedDotState(position,show);
    }

    public boolean isRedDotNeedShow(int position){
        return ShareDataLocal.as().getBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,position));
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public MessagesActivity getMessagesActivity() {
        return messagesActivity;
    }
}
