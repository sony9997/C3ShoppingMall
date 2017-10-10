package com.c3.jbz.logic;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.c3.jbz.util.Constant;
import com.c3.jbz.db.ShareDataLocal;

/**
 * Created by hedong on 2017/10/3.
 */

public class AndroidJsInvoker {
    private Handler handler;
    private static final String tag="AndroidJsInvoker";
    public AndroidJsInvoker(Handler handler){
        this.handler=handler;
    }

    /**
     * 登陆成功
     */
    @JavascriptInterface
    public void loginOk(){
        Log.d(tag,"loginOK:"+ShareDataLocal.as().getBooleanValue(Constant.KEY_LOGIN_TAG));
        ShareDataLocal.as().setBooleanValue(Constant.KEY_LOGIN_TAG,true);
    }

    /**
     * 登出成功
     */
    @JavascriptInterface
    public void logoutOk(){
        Log.d(tag,"logoutOk:"+ShareDataLocal.as().getBooleanValue(Constant.KEY_LOGIN_TAG));
        ShareDataLocal.as().setBooleanValue(Constant.KEY_LOGIN_TAG,false);
    }

    /**
     * 分享商品详情页到微信
     * @param url
     */
    @JavascriptInterface
    public void shareSessionUrl(String url){
        Log.d(tag,"shareSessionUrl:"+url);
    }

    /**
     * 分享商品图片到微信
     * @param imgurl
     */
    @JavascriptInterface
    public void shareSessionImage(String imgurl){
        Log.d(tag,"shareSessionImage:"+imgurl);
    }

    /**
     * 分享商品信息到微信朋友圈
     * @param text
     * @param imgs
     */
    @JavascriptInterface
    public void shareTimeline (String text,String imgs){
        Log.d(tag,"shareTimeline:"+text+"|"+imgs);
    }

    /**
     * 发起支付
     * @param prepayId 预付订单id
     */
    @JavascriptInterface
    public void  payment(String prepayId){
        Log.d(tag,"payment:"+prepayId);
    }
}
