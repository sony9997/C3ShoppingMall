package com.c3.jbz.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.c3.jbz.db.dao.MessageInfoDao;
import com.c3.jbz.vo.MessageInfo;

/**
 * Created by hedong on 2017/11/28.
 */
@Database(entities = {MessageInfo.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageInfoDao messageInfoDao();
}
