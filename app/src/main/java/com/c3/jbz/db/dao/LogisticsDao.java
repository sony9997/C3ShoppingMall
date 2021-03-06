package com.c3.jbz.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.c3.jbz.vo.Logistics;

import java.util.List;

/**
 * @author hedong
 * @date 2018/1/27
 */
@Dao
public interface LogisticsDao {
    @Query("SELECT * FROM logistics where userId=:userId order by receiveTime desc")
    LiveData<List<Logistics>> loadAllLogistics(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLogistics(Logistics logistics);

    @Delete
    void deleteLogistics(List<Logistics> logistics);
}
