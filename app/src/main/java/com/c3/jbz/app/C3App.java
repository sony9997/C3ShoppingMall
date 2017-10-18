package com.c3.jbz.app;

import android.app.Application;

import com.c3.jbz.db.ShareDataLocal;

/**
 * Created by hedong on 2017/10/18.
 */

public class C3App extends Application {
    public static C3App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        ShareDataLocal.as().init(this);
    }
}
