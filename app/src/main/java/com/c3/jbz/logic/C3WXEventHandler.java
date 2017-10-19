package com.c3.jbz.logic;

import com.c3.jbz.presenter.MainPresenter;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/**
 * Created by hedong on 2017/10/19.
 */

public class C3WXEventHandler implements IWXAPIEventHandler {
    private static C3WXEventHandler instance;
    private C3WXEventHandler(){

    }
    public static final C3WXEventHandler as(){
        if(instance==null)
            instance=new C3WXEventHandler();
        return instance;
    }

    private MainPresenter presenter;
    public void init(MainPresenter presenter){
        this.presenter=presenter;
    }
    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq baseReq) {

    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        if(presenter!=null)
            presenter.handleWXRespEvent(resp);
    }
}
