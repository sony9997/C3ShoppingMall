package com.c3.jbz.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.c3.jbz.vo.MessageInfo;

import java.util.List;

/**
 * @author hedong
 * @date 2018/1/26
 */
@Dao
public interface MessageInfoDao {
    @Query("SELECT * FROM MessageInfo where userId=:userId order by receiveTime desc")
    LiveData<List<MessageInfo>> loadAllMessageInfo(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessageInfo(MessageInfo messageInfo);

    @Delete
    void deleteMessageInfo(List<MessageInfo> messageInfo);
}
