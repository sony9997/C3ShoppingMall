package com.c3.jbz.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.logic.AndroidJsInvoker;
import com.c3.jbz.util.Constant;
import com.c3.jbz.util.ToolsUtil;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by hedong on 2017/10/3.
 */

public class MainPresenter extends MvpBasePresenter<MainView> implements Handler.Callback {
    private Handler handler;
    private Context context;
    public static final int MSG_LOADING_IMG=0;
    public static final int MSG_LOADED_IMG=1;
    public static final int MSG_SHARE_IMGS_TIMELINE=2;//分享图片到朋友圈
    public static final int MSG_ERR_NOT_INSTALL_WX=-1;//未安装微信
    public static final int MSG_ERR_NOT_SUPPORT_WX=-2;//不支持的微信api
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
        //初始化微信API对象
        IWXAPI iwxapi=WXAPIFactory.createWXAPI(context, BuildConfig.wxAppId,true);
        iwxapi.registerApp(BuildConfig.wxAppId);

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

        //加载主页面
        String url=String.format(BuildConfig.mainUrl, ToolsUtil.getUniqueId(context));
        if(isLogin()){
            url+="&userId="+ShareDataLocal.as().getStringValue(Constant.KEY_USERID,null);
        }
        getView().initMainPage(url,new AndroidJsInvoker(handler,iwxapi));
    }

    /**
     * 是否登陆
     * @return
     */
    private boolean isLogin(){
        return ShareDataLocal.as().getStringValue(Constant.KEY_USERID,null)!=null;
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
        }
        return false;
    }
}
