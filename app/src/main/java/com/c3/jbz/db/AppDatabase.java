package com.c3.jbz.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.c3.jbz.db.dao.LogisticsDao;
import com.c3.jbz.db.dao.MessageInfoDao;
import com.c3.jbz.db.dao.NoticeDao;
import com.c3.jbz.vo.Logistics;
import com.c3.jbz.vo.MessageInfo;
import com.c3.jbz.vo.Notice;

/**
 * Created by hedong on 2017/11/28.
 */
@Database(entities = {MessageInfo.class, Notice.class, Logistics.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageInfoDao messageInfoDao();

    public abstract NoticeDao noticeDao();

    public abstract LogisticsDao logisticsDao();
}
