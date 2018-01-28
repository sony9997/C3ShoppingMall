package com.c3.jbz.app;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.c3.jbz.db.AppDatabase;
import com.c3.jbz.db.ShareDataLocal;
import com.jakewharton.threetenabp.AndroidThreeTen;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by hedong on 2017/10/18.
 */

public class C3App extends Application {
    public static C3App app;
    private AppDatabase appDatabase;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        ShareDataLocal.as().init(this);
        //类似java8 中的时间日期库
        AndroidThreeTen.init(this);
        // 设置开启日志,发布时请关闭日志
        JPushInterface.setDebugMode(true);
        // 初始化 JPush
        JPushInterface.init(this);

        appDatabase= Room.databaseBuilder(this, AppDatabase.class, "xs.db").build();
    }

    public AppDatabase getAppDatabase(){
        return appDatabase;
    }
}
