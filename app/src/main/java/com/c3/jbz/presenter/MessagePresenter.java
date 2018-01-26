package com.c3.jbz.presenter;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.activity.MessagesActivity;
import com.c3.jbz.db.AppDatabase;
import com.c3.jbz.db.DateConverter;
import com.c3.jbz.fragment.LogisticsFragment;
import com.c3.jbz.fragment.MessageFragment;
import com.c3.jbz.fragment.NoticeFragment;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.MessageInfo;

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
                    String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
                    String content = bundle.getString(JPushInterface.EXTRA_ALERT);
                    final int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                    String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
                    String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                    if (!TextUtils.isEmpty(extra)) {
                        try {
                            JSONObject jsonObject = new JSONObject(extra);
                            final int type = jsonObject.getInt(BuildConfig.KEY_MSG_TYPE);

                            switch (type) {
                                case BuildConfig.MSG_TYPE_NORMAL: {
                                    String comment = jsonObject.has("comment")?jsonObject.getString("comment"):null;
                                    String detailUrl = jsonObject.has("detailUrl")?jsonObject.getString("detailUrl"):null;
                                    long pushTime = jsonObject.has("pushTime")?jsonObject.getLong("pushTime"):0;
                                    if(pushTime==0){
                                        pushTime=System.currentTimeMillis();
                                    }
                                    MessageInfo messageInfo = new MessageInfo(title, content, notificationId, msgId, comment, detailUrl,
                                            DateConverter.toDate(pushTime), LocalDateTime.now());
                                    appDatabase.messageInfoDao().insertMessageInfo(messageInfo);
                                    break;
                                }
                                case BuildConfig.MSG_TYPE_NOTICE: {
                                    break;
                                }
                                case BuildConfig.MSG_TYPE_LOGISTICS: {
                                    break;
                                }
                            }

                            AppExecutors.as().mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
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

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public MessagesActivity getMessagesActivity() {
        return messagesActivity;
    }
}
