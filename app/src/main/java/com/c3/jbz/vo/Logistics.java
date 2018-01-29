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
public class Logistics {
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

    /**
     * 物流状态
     */
    public String status;

    /**
     * 商品截图
     */
    public String goodsPic;
    /**
     * 货运单号
     */
    public String expressNo;

    public int notificationId;

    /**
     * 接收到极光推送的消息的时间
     */

    public org.threeten.bp.LocalDateTime receiveTime;

    @Ignore
    public boolean isChecked;

    public Logistics(@NonNull String msgId, String title, LocalDateTime date, String clickLink, String status, String goodsPic, String expressNo, int notificationId, LocalDateTime receiveTime) {
        this.msgId = msgId;
        this.title = title;
        this.date = date;
        this.clickLink = clickLink;
        this.status = status;
        this.goodsPic = goodsPic;
        this.expressNo = expressNo;
        this.notificationId = notificationId;
        this.receiveTime = receiveTime;
    }

    @Override
    public String toString() {
        return "Logistics{" +
                "msgId='" + msgId + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", clickLink='" + clickLink + '\'' +
                ", status='" + status + '\'' +
                ", goodsPic='" + goodsPic + '\'' +
                ", expressNo='" + expressNo + '\'' +
                ", notificationId=" + notificationId +
                ", receiveTime=" + receiveTime +
                ", isChecked=" + isChecked +
                '}';
    }
}
