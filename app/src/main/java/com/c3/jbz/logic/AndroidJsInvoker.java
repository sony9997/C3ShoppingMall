package com.c3.jbz.logic;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.presenter.MainPresenter;
import com.c3.jbz.util.Constant;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.util.ToolsUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hedong on 2017/10/3.
 */

public class AndroidJsInvoker {
    private Handler handler;
    public static final String tag = "AndroidJsInvoker";
    private IWXAPI iwxapi;
    private static final int THUMB_SIZE = 150;
    private static final int thumbDataMaxLen='耀';//缩略图数据最大长度，微信要求

    public AndroidJsInvoker(Handler handler, IWXAPI iwxapi) {
        this.handler = handler;
        this.iwxapi = iwxapi;
    }

    /**
     * 登陆成功
     */
    @JavascriptInterface
    public void loginOk(String userId) {
        Log.d(tag, "loginOK:" + userId);
        ShareDataLocal.as().setStringValue(Constant.KEY_USERID,userId);
    }

    /**
     * 登出成功
     */
    @JavascriptInterface
    public void logoutOk() {
        Log.d(tag, "logoutOk:" +  ShareDataLocal.as().getStringValue(Constant.KEY_USERID,null));
        ShareDataLocal.as().removeValue(Constant.KEY_USERID);
        handler.sendEmptyMessage(MainPresenter.MSG_LOGOUT);
    }

    /**
     * 分享商品详情页到微信好友
     *
     * @param url
     */
    @JavascriptInterface
    public void shareSessionUrl(String url, String title, String text, String imgurl) {
        Log.d(tag, String.format("shareSessionUrl:%s|%s|%s|%s",url,title,text,imgurl));
        if(!checkWXStatus())
            return;
        shareUrl(url,title,text,imgurl,false);
    }

    /**
     * 分享商品详情页到微信朋友圈
     *
     * @param url
     */
    @JavascriptInterface
    public void shareTimeLineUrl(String url, String title, String text, String imgurl) {
        Log.d(tag, String.format("shareTimeLineUrl:%s|%s|%s|%s",url,title,text,imgurl));
        if(!checkWXStatus())
            return;
        shareUrl(url,title,text,imgurl,true);
    }

    private void shareUrl(String url, String title, String text, String imgurl,final boolean isTimeLine){
        if (url != null) {
            final String transaction="webpage";
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = url;
            final WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = title;
            msg.description = text;
            if (imgurl != null && imgurl.trim().length() > 0) {
                ToolsUtil.getBitmap(imgurl, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        handler.sendEmptyMessage(MainPresenter.MSG_LOADING_IMG);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (failReason != null) {
                            Log.d(tag, failReason.toString());
                            if (failReason.getCause() != null)
                                failReason.getCause().printStackTrace();
                        }
                        sendWXMediaMessage2Session(msg,transaction,isTimeLine);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        msg.thumbData=getThumbData(loadedImage);
                        sendWXMediaMessage2Session(msg,transaction,isTimeLine);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        sendWXMediaMessage2Session(msg,transaction,isTimeLine);
                    }
                });

            } else {
                sendWXMediaMessage2Session(msg,transaction,isTimeLine);
            }
        }
    }

    /**
     * 获得缩略图数据
     * @param loadedImage
     * @return
     */
    private byte[] getThumbData(Bitmap loadedImage){
        Bitmap thumbBmp = Bitmap.createScaledBitmap(loadedImage, THUMB_SIZE, THUMB_SIZE, true);
        loadedImage.recycle();
        byte[] thumbData = ToolsUtil.bmpToByteArray(thumbBmp, false);
        int curSize=THUMB_SIZE;
        while(thumbData.length>thumbDataMaxLen){
            thumbData=null;
            curSize-=10;
            Bitmap tmp=Bitmap.createScaledBitmap(thumbBmp, curSize, curSize, true);
            thumbData = ToolsUtil.bmpToByteArray(tmp, true);
        }
        thumbBmp.recycle();
        return thumbData;
    }

    private void sendWXMediaMessage2Session(WXMediaMessage mediaMessage,String transaction,boolean isTimeLine) {
        if(mediaMessage!=null) {
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction(transaction);
            req.message = mediaMessage;
            req.scene = isTimeLine?SendMessageToWX.Req.WXSceneTimeline:SendMessageToWX.Req.WXSceneSession;
            iwxapi.sendReq(req);
        }
        handler.sendEmptyMessage(MainPresenter.MSG_LOADED_IMG);
    }

    /**
     * 分享商品图片到微信
     *
     * @param imgurl
     */
    @JavascriptInterface
    public void shareSessionImage(String imgurl) {
        Log.d(tag, "shareSessionImage:" + imgurl);
        if (imgurl != null) {
            shareImages(imgurl,null,false);
        }
    }

    private AtomicBoolean inShareImgs=new AtomicBoolean(false);
    /**
     * 分享商品信息到微信朋友圈,分享多张图片+文字
     *
     * @param text
     * @param imgs
     */
    @JavascriptInterface
    public synchronized void shareTimeline(final String text, String imgs) {
        Log.d(tag, "shareTimeline:" + text + "|" + imgs);

        if(imgs!=null&&imgs.trim().length()>0){
            shareImages(imgs,text,true);
        }
    }

    /**
     * 分享图片到朋友或者朋友圈
     * @param imgurl
     * @param text
     * @param isTimeLine
     */
    private void shareImages(String imgurl,final String text,final boolean isTimeLine){
        if(!checkWXStatus())
            return;
        final String[] imgUrls=imgurl.split(",");
        Log.d(tag,"imgUrls len:"+imgUrls.length);
        if(imgUrls.length<=0){
            Log.e(tag,"share images len is 0!");
            return;
        }
        if(imgUrls.length==1){//单张
            ToolsUtil.getBitmap(imgurl, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    handler.sendEmptyMessage(MainPresenter.MSG_LOADING_IMG);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    if (failReason != null) {
                        Log.d(tag, failReason.toString());
                        if (failReason.getCause() != null)
                            failReason.getCause().printStackTrace();
                    }
                    sendWXMediaMessage2Session(null,null,false);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    WXImageObject wxImageObject=new WXImageObject(loadedImage);
                    WXMediaMessage wxMediaMessage=new WXMediaMessage();
                    wxMediaMessage.mediaObject=wxImageObject;
                    wxMediaMessage.thumbData = getThumbData(loadedImage);
                    final String transaction="img";
                    sendWXMediaMessage2Session(wxMediaMessage,transaction,false);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    sendWXMediaMessage2Session(null,null,false);
                }
            });
        }else{//多张
            if(inShareImgs.get()){
                return;
            }
            if(imgUrls!=null) {
                inShareImgs.set(true);
                handler.sendEmptyMessage(MainPresenter.MSG_LOADING_IMG);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Uri> imageUris = new ArrayList<Uri>();
                        int max=9;//微信朋友圈限制了最多传9个图片
                        for (String imgUrl:imgUrls){
                            File file=ToolsUtil.getBitmapFileSync(imgUrl,true);
                            imageUris.add(Uri.fromFile(file));
                            max--;
                            if(max==0)
                                break;
                        }
                        Intent intent = getShareImgIntent(text,imageUris,isTimeLine);
                        Message message=handler.obtainMessage(MainPresenter.MSG_SHARE_IMGS_TIMELINE);
                        message.obj=intent;
                        message.arg1=1;
                        handler.sendMessage(message);
                        inShareImgs.set(false);
                    }
                }).start();
            }
        }
    }

    private static final String COMP_CLS_NAME_TIMELINE="com.tencent.mm.ui.tools.ShareToTimeLineUI";
    private static final String COMP_CLS_NAME_SESSION="com.tencent.mm.ui.tools.ShareImgUI";
    private static final Intent getShareImgIntent(String text, ArrayList<Uri> imageUris, boolean isTimeLine){
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm",
                isTimeLine?COMP_CLS_NAME_TIMELINE:COMP_CLS_NAME_SESSION);
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putExtra(isTimeLine?"Kdescription":Intent.EXTRA_TEXT, text);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        return intent;
    }

    /**
     * 发起支付
     *
     * @param prepayId 预付订单id
     */
    @JavascriptInterface
    public void payment(String prepayId) {
        Log.d(tag, "payment:" + prepayId);
        if(!checkWXStatus())
            return;
        PayReq request = new PayReq();
        request.appId = BuildConfig.wxAppId;
        request.partnerId = BuildConfig.wxPartnerId;
        request.prepayId= prepayId;
        request.packageValue = "Sign=WXPay";
        request.nonceStr= ToolsUtil.createRandom(false,32);
        request.timeStamp= String.valueOf(System.currentTimeMillis()/1000);
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", request.appId);
        parameters.put("noncestr", request.nonceStr);
        parameters.put("package", request.packageValue);
        parameters.put("partnerid", request.partnerId);
        parameters.put("prepayid", request.prepayId);
        parameters.put("timestamp", request.timeStamp);
        request.sign= ToolsUtil.createWXSign(parameters);
        Log.d(tag, "request.sign:" + request.sign);
        boolean result=iwxapi.sendReq(request);
        Log.d(tag, "sendReq:" + result);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 检测微信api状态
     * @return
     */
    private boolean checkWXStatus(){
        if(!this.iwxapi.isWXAppInstalled()){
            handler.sendEmptyMessage(MainPresenter.MSG_ERR_NOT_INSTALL_WX);
            return false;
        }
        if(!iwxapi.isWXAppSupportAPI()){
            handler.sendEmptyMessage(MainPresenter.MSG_ERR_NOT_SUPPORT_WX);
            return false;
        }
        return true;
    }
}
