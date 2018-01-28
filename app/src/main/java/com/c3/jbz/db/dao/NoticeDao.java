package com.c3.jbz.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.c3.jbz.vo.Notice;

import java.util.List;

/**
 * @author hedong
 * @date 2018/1/27
 */
@Dao
public interface NoticeDao {
    @Query("SELECT * FROM Notice order by receiveTime desc")
    LiveData<List<Notice>> loadAllNotice();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotice(Notice notice);

    @Delete
    void deleteNotice(List<Notice> notices);
}
