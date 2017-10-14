package com.c3.jbz.view;

import com.hannesdorfmann.mosby3.mvp.MvpView;

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
}
