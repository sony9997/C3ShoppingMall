package com.c3.jbz.vo;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import org.threeten.bp.LocalDateTime;

/**
 * @author hedong
 * @date 2018/1/26
 */
@Entity(primaryKeys = "msgId")
public class MessageInfo {
    public String title;
    public String content;
    public int notificationId;
    @NonNull
    public String msgId;
    /**
     * 备注
     */
    public String comment;
    public String detailUrl;
    /**
     * 业务服务器后台　推送时间
     */
    public org.threeten.bp.LocalDateTime pushTime;
    /**
     * 接收到消息的时间
     */
    public org.threeten.bp.LocalDateTime receiveTime;

    public MessageInfo(String title, String content, int notificationId, @NonNull String msgId, String comment, String detailUrl, LocalDateTime pushTime, LocalDateTime receiveTime) {
        this.title = title;
        this.content = content;
        this.notificationId = notificationId;
        this.msgId = msgId;
        this.comment = comment;
        this.detailUrl = detailUrl;
        this.pushTime = pushTime;
        this.receiveTime = receiveTime;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", notificationId=" + notificationId +
                ", msgId='" + msgId + '\'' +
                ", comment='" + comment + '\'' +
                ", detailUrl='" + detailUrl + '\'' +
                ", pushTime=" + pushTime +
                ", receiveTime=" + receiveTime +
                '}';
    }
}
