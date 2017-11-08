package com.c3.jbz.view;

import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.util.Map;

/**
 * Created by hedong on 2017/10/3.
 */

public interface MainView extends MvpView {
    final int LOADINGTYPE_SHARE=1;
    final int LOADINGTYPE_PAY=2;
    final int LOADINGTYPE_LOADIMAGE=3;


    /**
     * 显示提示框
     * @param type
     */
    public void onLoading(int type);
    /**
     * 显示提示
     * @param msgId
     */
    public void toast(int msgId);

    /**
     * 加载主页
     */
    public void loadMainPage();

    /**
     * 初始化主页参数
     * @param url 主页url
     * @param jsObject js对象
     */
    void initMainPage(String url,Object jsObject);

    /**
     * 隐藏提示框
     */
    public void hideLoading();

    /**
     * 是否在顶级页面
     */
    public void checkTopLevelPage();

    /**
     * 处理微信返回
     * @param resp
     */
    public void handleWXRespEvent(BaseResp resp);

    /**
     * 设置是否显示分享按钮
     * @param isShow
     */
    public void setShowShareButton(boolean isShow);
    /**
     * 设置是否显示页眉
     * @param isShow
     */
    public void setShowHeader(boolean isShow);

    /**
     * 支付宝支付返回结果处理
     * @param result
     */
    public void handleAliRespEvent(Map<String, String> result);

}
