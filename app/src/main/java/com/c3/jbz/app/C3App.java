package com.c3.jbz.app;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.IntentFilter;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.comp.PushServiceStartReceiver;
import com.c3.jbz.db.AppDatabase;
import com.c3.jbz.db.ShareDataLocal;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.tencent.bugly.crashreport.CrashReport;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by hedong on 2017/10/18.
 */

public class C3App extends Application {
    public static C3App app;
    private AppDatabase appDatabase;
    private PushServiceStartReceiver pushServiceStartReceiver=new PushServiceStartReceiver();
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        ShareDataLocal.as().init(this);
        //类似java8 中的时间日期库
        AndroidThreeTen.init(this);
        // 设置开启日志,发布时请关闭日志
        JPushInterface.setDebugMode(false);
        // 初始化 JPush
        JPushInterface.init(this);

        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel(BuildConfig.FLAVOR);
        strategy.setAppPackageName(getPackageName());
        strategy.setAppVersion(BuildConfig.VERSION_NAME);
        CrashReport.initCrashReport(getApplicationContext(), BuildConfig.BUGLY_APP_KEY, true,strategy);

        appDatabase= Room.databaseBuilder(this, AppDatabase.class, "xs.db").build();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(pushServiceStartReceiver,intentFilter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        this.unregisterReceiver(pushServiceStartReceiver);
    }

    public AppDatabase getAppDatabase(){
        return appDatabase;
    }
}
