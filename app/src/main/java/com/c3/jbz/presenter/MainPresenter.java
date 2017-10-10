package com.c3.jbz.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.logic.AndroidJsInvoker;
import com.c3.jbz.util.Constant;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

/**
 * Created by hedong on 2017/10/3.
 */

public class MainPresenter extends MvpBasePresenter<MainView> implements Handler.Callback {
    private Handler handler;
    /**
     * 加载主页
     */
    public void loadMainPage(){
        if(handler==null){
            handler=new Handler(Looper.getMainLooper(),this);
        }
        getView().initMainPage(isLogin()?BuildConfig.mainUrl:BuildConfig.loginUrl,new AndroidJsInvoker(handler));
    }

    /**
     * 是否登陆
     * @return
     */
    private boolean isLogin(){
        return ShareDataLocal.as().getBooleanValue(Constant.KEY_LOGIN_TAG,false);
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}
