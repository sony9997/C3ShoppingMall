package com.c3.jbz.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.logic.AndroidJsInvoker;
import com.c3.jbz.logic.C3WXEventHandler;
import com.c3.jbz.util.ToolsUtil;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hedong on 2017/10/3.
 */

public class MainPresenter extends MvpBasePresenter<MainView> implements Handler.Callback {
    private Handler handler;
    private Context context;
    public static final int MSG_LOADING_IMG=0;
    public static final int MSG_LOADED_IMG=1;
    public static final int MSG_SHARE_IMGS_TIMELINE=2;//分享图片到朋友圈
    public static final int MSG_LOGOUT=3;//登出
    public static final int MSG_SHOWSHARE=4;//设置分享按钮是否显示
    public static final int MSG_SHOWHEADER=5;//设置页眉是否显示
    public static final int MSG_ALIPAY_RESULT=6;//支付宝支付返回结果
    public static final int MSG_ERR_NOT_INSTALL_WX=-1;//未安装微信
    public static final int MSG_ERR_NOT_SUPPORT_WX=-2;//不支持的微信api

    private AndroidJsInvoker androidJsInvoker=null;
    public MainPresenter(Context context){
        this.context=context;
    }
    /**
     * 加载主页
     */
    public void loadMainPage(){
        if(handler==null){
            handler=new Handler(Looper.getMainLooper(),this);
        }

        //初始化图片加载工具
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory().cacheOnDisc().build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
        C3WXEventHandler.as().init(this);
        androidJsInvoker=new AndroidJsInvoker(handler,context);

        go2MainPage();
    }

    private void go2MainPage(){
        //加载主页面
        String url=String.format(BuildConfig.mainUrl, ToolsUtil.getUniqueId(context));
        if(isLogin()){
            url+="&userId="+ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
        }
        getView().initMainPage(url,androidJsInvoker);
    }

    /**
     * 是否登陆
     * @return
     */
    private boolean isLogin(){
        return ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null)!=null;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what){
            case MSG_LOADING_IMG:{
                getView().onLoading(MainView.LOADINGTYPE_LOADIMAGE);
                break;
            }
            case MSG_LOADED_IMG:{
                getView().hideLoading();
                break;
            }
            case MSG_SHARE_IMGS_TIMELINE:{
                Intent intent= (Intent) message.obj;
                if(message.arg1==1){
                    ArrayList<Uri> imageUris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if(imageUris!=null) {
                        for(Uri uri:imageUris)
                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    }
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                getView().hideLoading();
                break;
            }
            case MSG_ERR_NOT_INSTALL_WX:{
                getView().toast(R.string.toast_not_install_wx);
                break;
            }
            case MSG_ERR_NOT_SUPPORT_WX:{
                getView().toast(R.string.toast_not_support_wxapi);
                break;
            }
            case MSG_LOGOUT:{
                go2MainPage();
                break;
            }
            case MSG_SHOWSHARE:{
                boolean isShow=message.arg1==1;
                getView().setShowShareButton(isShow);
                break;
            }
            case MSG_SHOWHEADER:{
                boolean isShow=message.arg1==1;
                getView().setShowHeader(isShow);
                break;
            }
            case MSG_ALIPAY_RESULT:{
                getView().handleAliRespEvent((Map<String, String>) message.obj);
                break;
            }
        }
        return false;
    }

    /**
     * 处理微信回调
     * @param resp
     */
    public void handleWXRespEvent(BaseResp resp){
        getView().handleWXRespEvent(resp);
    }
}
