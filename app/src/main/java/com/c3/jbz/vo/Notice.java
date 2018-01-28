package com.c3.jbz.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;

/**
 * @author hedong
 * @date 2018/1/27
 */
@Entity(primaryKeys = "msgId")
public class Notice {
    @NonNull
    public String msgId;

    public String title;
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

    @Ignore
    public boolean isChecked;

    public Notice(@NonNull String msgId, String title, LocalDateTime date, String clickLink, int notificationId, LocalDateTime receiveTime) {
        this.msgId = msgId;
        this.title = title;
        this.date = date;
        this.clickLink = clickLink;
        this.notificationId = notificationId;
        this.receiveTime = receiveTime;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "msgId='" + msgId + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", clickLink='" + clickLink + '\'' +
                ", notificationId=" + notificationId +
                ", receiveTime=" + receiveTime +
                ", isChecked=" + isChecked +
                '}';
    }
}
