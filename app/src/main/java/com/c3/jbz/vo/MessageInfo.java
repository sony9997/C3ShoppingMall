package com.c3.jbz.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;

/**
 * @author hedong
 * @date 2018/1/26
 */
@Entity(primaryKeys = "msgId")
public class MessageInfo{
    @NonNull
    public String msgId;

    public String title;
    /**
     * 正文
     */
    public String body;
    /**
     * 头部
     */
    public String head;
    /**
     * 尾部
     */
    public String foot;
    /**
     * 业务服务器后台　推送时间
     */
    public org.threeten.bp.LocalDateTime date;
    /**
     * 跳转链接
     */
    public String clickLink;

    public int notificationId;

    /**
     * 接收到极光推送的消息的时间
     */
    public org.threeten.bp.LocalDateTime receiveTime;

    public String userId;

    @Ignore
    public boolean isChecked;

    public MessageInfo(@NonNull String msgId, String title, String body, String head, String foot, LocalDateTime date, String clickLink, int notificationId, LocalDateTime receiveTime, String userId) {
        this.msgId = msgId;
        this.title = title;
        this.body = body;
        this.head = head;
        this.foot = foot;
        this.date = date;
        this.clickLink = clickLink;
        this.notificationId = notificationId;
        this.receiveTime = receiveTime;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "msgId='" + msgId + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", head='" + head + '\'' +
                ", foot='" + foot + '\'' +
                ", date=" + date +
                ", clickLink='" + clickLink + '\'' +
                ", notificationId=" + notificationId +
                ", receiveTime=" + receiveTime +
                ", userId='" + userId + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
