package com.c3.jbz.logic;

import android.os.Handler;
import android.webkit.JavascriptInterface;

/**
 * Created by hedong on 2017/10/3.
 */

public class AndroidJsInvoker {
    private Handler handler;
    public AndroidJsInvoker(Handler handler){
        this.handler=handler;
    }

    /**
     * 登陆成功
     */
    @JavascriptInterface
    public void loginOk(){

    }

    /**
     * 登出成功
     */
    @JavascriptInterface
    public void logoutOk(){

    }

    /**
     * 分享商品详情页到微信
     * @param url
     */
    @JavascriptInterface
    public void shareSessionUrl(String url){

    }

    /**
     * 分享商品图片到微信
     * @param imgurl
     */
    @JavascriptInterface
    public void shareSessionImage(String imgurl){

    }

    /**
     * 分享商品信息到微信朋友圈
     * @param text
     * @param imgs
     */
    @JavascriptInterface
    public void shareTimeline (String text,String imgs){

    }

    /**
     * 发起支付
     * @param prepayId 预付订单id
     */
    @JavascriptInterface
    public void  payment(String prepayId){

    }
}
