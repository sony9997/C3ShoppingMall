package com.c3.jbz.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
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
import com.c3.jbz.vo.Logistics;
import com.c3.jbz.vo.MessageInfo;
import com.c3.jbz.vo.Notice;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.jpush.android.api.JPushInterface;

/**
 * @author hedong
 * @date 2018/1/26
 */

public final class MessagePresenter {
    private static final String tag = "message";
    private MessagesActivity messagesActivity;
    private AppDatabase appDatabase;
    public static final String KEY_SHOW_REDDOT_FORMAT_PRE = "KEY_SHOW_REDDOT_%s";
    public static final String KEY_SHOW_REDDOT_FORMAT = KEY_SHOW_REDDOT_FORMAT_PRE + "_%d";

    public MessagePresenter(MessagesActivity messagesActivity) {
        this.messagesActivity = messagesActivity;
        appDatabase = C3App.app.getAppDatabase();
    }

    /**
     * 解析从push消息推送来的数据
     *
     * @param bundle
     */
    @MainThread
    public void parseBunlde(final Bundle bundle) {
        Log.d(tag, "parseBunlde:" + bundle);
        if (bundle != null) {
            final int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
            String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (!TextUtils.isEmpty(extra)) {
                try {
                    String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
                    JSONObject jsonObject = new JSONObject(extra);
                    String mc = jsonObject.getString(BuildConfig.KEY_MSG_CONTENT);
                    if (TextUtils.isEmpty(mc)) {
                        return;
                    }

                    JSONObject messageContent = new JSONObject(mc);
                    final int type = jsonObject.getInt(BuildConfig.KEY_MSG_TYPE);
                    Object addData = null;
                    switch (type) {
                        case BuildConfig.MSG_TYPE_NORMAL: {
                            String title = messageContent.has("title") ? messageContent.getString("title") : null;
                            String body = messageContent.has("body") ? messageContent.getString("body") : null;
                            String head = messageContent.has("head") ? messageContent.getString("head") : null;
                            String foot = messageContent.has("foot") ? messageContent.getString("foot") : null;
                            String clickLink = messageContent.has("clickLink") ? URLDecoder.decode(messageContent.getString("clickLink"), "UTF-8") : null;

                            long date = messageContent.has("date") ? messageContent.getLong("date") : 0;
                            if (date == 0) {
                                date = System.currentTimeMillis();
                            }
                            final MessageInfo messageInfo = new MessageInfo(msgId, title, body, head, foot, DateConverter.toDate(date),
                                    clickLink, notificationId, LocalDateTime.now(),userId);
                            AppExecutors.as().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.messageInfoDao().insertMessageInfo(messageInfo);
                                }
                            });
                            addData = messageInfo;
                            break;
                        }
                        case BuildConfig.MSG_TYPE_NOTICE: {
                            String title = messageContent.has("title") ? messageContent.getString("title") : null;
                            long date = messageContent.has("date") ? messageContent.getLong("date") : 0;
                            if (date == 0) {
                                date = System.currentTimeMillis();
                            }
                            String clickLink = messageContent.has("clickLink") ? URLDecoder.decode(messageContent.getString("clickLink"), "UTF-8") : null;
                            final Notice notice = new Notice(msgId, title, DateConverter.toDate(date), clickLink, notificationId, LocalDateTime.now(),userId);
                            AppExecutors.as().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.noticeDao().insertNotice(notice);
                                }
                            });
                            addData = notice;
                            break;
                        }
                        case BuildConfig.MSG_TYPE_LOGISTICS: {
                            String title = messageContent.has("title") ? messageContent.getString("title") : null;
                            long date = messageContent.has("date") ? messageContent.getLong("date") : 0;
                            String status = messageContent.has("status") ? messageContent.getString("status") : null;
                            if (date == 0) {
                                date = System.currentTimeMillis();
                            }
                            String clickLink = messageContent.has("clickLink") ? URLDecoder.decode(messageContent.getString("clickLink"), "UTF-8") : null;
                            String goodsPic = messageContent.has("goodsPic") ? messageContent.getString("goodsPic") : null;
                            String expressNo = messageContent.has("expressNo") ? messageContent.getString("expressNo") : null;
                            final Logistics logistics = new Logistics(msgId, title, DateConverter.toDate(date), clickLink, status, goodsPic, expressNo, notificationId, LocalDateTime.now(),userId);
                            AppExecutors.as().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.logisticsDao().insertLogistics(logistics);
                                }
                            });
                            addData = logistics;
                            break;
                        }
                        default:
                            Log.w(tag, "unknow type:" + type);
                            return;
                    }
                    updateRedDotState(type, true);
                    if (messagesActivity != null)
                        messagesActivity.selectTab(type, notificationId);
                    if (addData != null && messagesActivity != null) {
                        messagesActivity.addData2SubFragment(addData, type);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @MainThread
    public void updateRedDotState(int position, boolean show) {
        String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
        ShareDataLocal.as().setBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,userId, position), show);
        if (messagesActivity != null)
            messagesActivity.updateRedDotState(position, show);
    }

    public void updateRedDotStateInCurrentTab(boolean show){
        if (messagesActivity != null){
            updateRedDotState(messagesActivity.getCurrentTabPosition(),show);
        }
    }

    public static final boolean isRedDotNeedShow(int position) {
        String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
        return ShareDataLocal.as().getBooleanValue(String.format(KEY_SHOW_REDDOT_FORMAT,userId, position));
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public MessagesActivity getMessagesActivity() {
        return messagesActivity;
    }

    public void openMainActivity(String url) {
        updateRedDotStateInCurrentTab(false);
        Intent intent = new Intent(messagesActivity, MainActivity.class);
        intent.putExtra(BuildConfig.KEY_OTHER_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        messagesActivity.startActivity(intent);
    }
}
