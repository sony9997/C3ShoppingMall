package com.c3.jbz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.comp.C3WebChromeClient;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.presenter.MainPresenter;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.PayResult;
import com.c3.jbz.util.ToolsUtil;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.c3.jbz.R.id.bottom;
import static com.c3.jbz.R.id.pb_main;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends MvpActivity<MainView, MainPresenter> implements MainView {
    @BindView(R.id.wv_main)
    WebView webView;

    @BindView(pb_main)
    ProgressBar pbMain;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.ll_empty)
    View ll_empty;
    @BindView(R.id.ll_header)
    View ll_header;
    @BindView(R.id.iv_share)
    ImageButton iv_share;

    private ProgressDialog pd;

    private C3WebChromeClient c3WebChromeClient;
    private Toast toast;
    private boolean loadError;
    @BindView(R.id.rl_goto_msg)
    View rl_goto_msg;

    @BindView(R.id.iv_hadmsg)
    View iv_hadmsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ToolsUtil.setStatusBarColor(this);
        // android 7.0系统解决拍照的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        ButterKnife.bind(this);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        c3WebChromeClient = new C3WebChromeClient(this, pbMain, tv_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ToolsUtil.verifyStoragePermissions(this);

        Intent intent = getIntent();
        String otherUrl = null;
        if (intent != null) {
            otherUrl = intent.getStringExtra(BuildConfig.KEY_OTHER_URL);
        }
        loadMainPage(otherUrl);
        LocalBroadcastManager.getInstance(this).registerReceiver(msgBroadcastReceiver,new IntentFilter(BuildConfig.KEY_HAVE_MSG));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String otherUrl = null;
        if (intent != null) {
            otherUrl = intent.getStringExtra(BuildConfig.KEY_OTHER_URL);
            if (otherUrl != null)
                loadMainPage(otherUrl);
        }
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void onLoading(int type) {
        if (pd == null) {
            pd = new ProgressDialog(this);
        }

        pd.setCancelable(false);
        pd.setMessage(getResources().getStringArray(R.array.pd_titles)[type]);
        pd.show();
    }

    @Override
    public void toast(int msgId) {
        if (toast != null) {
            toast.setText(msgId);
            toast.show();
        }
    }

    @Override
    public void loadMainPage(String url) {
        getPresenter().loadMainPage(url);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void initMainPage(String url, Object jsObject) {
        rl_goto_msg.setVisibility(getPresenter().isLogin() ? View.VISIBLE : View.INVISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("onPageStarted", url);
                super.onPageStarted(view, url, favicon);
                loadError = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
                if (tv_title != null)
                    tv_title.setText(view.getTitle());
                if (loadError) {
                    webView.setVisibility(View.GONE);
                    ll_empty.setVisibility(View.VISIBLE);
                } else {
                    webView.setVisibility(View.VISIBLE);
                    ll_empty.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            /**
             * 页面加载错误时执行的方法，但是在6.0以下，有时候会不执行这个方法
             * @param view
             * @param errorCode
             * @param description
             * @param failingUrl
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                loadError = true;
            }

        });

        webView.setWebChromeClient(c3WebChromeClient);

        // 设置WebView属性，能够执行Javascript脚本
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setSavePassword(false);
        setPageCacheCapacity(webViewSettings);
        // 设置允许JS弹窗
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 修改ua使得web端正确判断
        String ua = webViewSettings.getUserAgentString();
        webViewSettings.setUserAgentString(ua + BuildConfig.SUFFIX_USER_AGENT);
        webView.setDownloadListener(downloadListener);
        webView.addJavascriptInterface(jsObject, "androidInvoker");
        webView.loadUrl(url);
    }

    protected void setPageCacheCapacity(WebSettings webSettings) {
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);//设置缓冲大小，我设的是8M
// 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
//开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
//设置数据库缓存路径
        webSettings.setDatabasePath(appCacheDir);
//设置  Application Caches 缓存目录
        webSettings.setAppCachePath(appCacheDir);
//开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
//设置可以访问文件
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setBlockNetworkImage(false);//解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }

    /**
     * 下载监听，当页面有可以下载的链接时触发
     */
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };

    @Override
    public void hideLoading() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = null;
    }

    private ValueCallback<String> valueCallback = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String s) {
            if ((s != null && s.trim().toLowerCase().equals("true")) || !webView.canGoBack()) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle(R.string.alert_title);
                b.setMessage(R.string.exit_msg);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                b.setCancelable(false);
                b.create().show();
            } else {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        }
    };

    @Override
    public void checkTopLevelPage() {
        webView.evaluateJavascript(BuildConfig.WEB_JS_NAME_ISTOP, valueCallback);
    }

    @Override
    public void handleWXRespEvent(BaseResp resp) {
        String errStr = resp.errStr;
        errStr = errStr != null && errStr.trim().length() > 0 ? errStr : "";
        webView.evaluateJavascript(String.format(BuildConfig.WEB_JS_NAME_handleWXRespEvent, resp.getType(), resp.errCode, errStr), null);
        switch (resp.getType()) {
            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX: {
                if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                    toast.setText(R.string.title_share_success);
                } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                    toast.setText(R.string.title_share_cancel);
                } else {
                    errStr = errStr.length() > 0 ? errStr : getString(R.string.title_share_faild);
                    toast.setText(errStr);
                }
                toast.show();
                break;
            }
            case ConstantsAPI.COMMAND_PAY_BY_WX: {
                if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                    toast.setText(R.string.title_pay_success);
                } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                    toast.setText(R.string.title_pay_cancel);
                } else {
                    errStr = errStr.length() > 0 ? errStr : getString(R.string.title_pay_faild);
                    toast.setText(errStr);
                }
                toast.show();
                break;
            }
        }
    }

    @Override
    public void setShowShareButton(boolean isShow) {
        if (iv_share != null)
            iv_share.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setShowHeader(boolean isShow) {
        if (ll_header != null)
            ll_header.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void handleAliRespEvent(Map<String, String> result) {
        PayResult payResult = new PayResult((Map<String, String>) result);
        /**
         对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
         */
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
        String resultStatus = payResult.getResultStatus();
        webView.evaluateJavascript(String.format(BuildConfig.WEB_JS_NAME_handleALIRespEvent, resultStatus), null);
        // 判断resultStatus 为9000则代表支付成功
        if (TextUtils.equals(resultStatus, "9000")) {
            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            toast.setText(R.string.title_pay_success);
        } else if (TextUtils.equals(resultStatus, "6001")) {
            toast.setText(R.string.title_pay_cancel);
        } else {
            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
            toast.setText(getString(R.string.title_pay_faild) + "errorCode:" + resultStatus);
        }
        toast.show();
    }

    @Override
    public void login() {
        rl_goto_msg.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        checkTopLevelPage();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (webView != null) {
                webView.reload();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        c3WebChromeClient.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.iv_back)
    public void goPre(View view) {
        checkTopLevelPage();
    }

    @OnClick(R.id.iv_share)
    public void goShare(View view) {
        webView.evaluateJavascript(BuildConfig.WEB_JS_NAME_goShare, null);
    }

    public void setLoadError(boolean loadError) {
        this.loadError = loadError;
    }

    @OnClick(R.id.tv_set)
    public void go2SettingNetwork(View view) {
        Intent intent = null;
        //判断手机系统的版本  即API大于10 就是3.0或以上版本
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        startActivity(intent);
    }

    @OnClick(R.id.tv_fresh)
    public void urlReload(View view) {
        if (webView != null) {
            webView.reload();
        }
    }

    @OnClick(R.id.rl_goto_msg)
    public void go2MessageCenter(View view) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkHadMsg();
    }

    private void checkHadMsg() {
        if (rl_goto_msg.getVisibility() == View.VISIBLE) {
            if(!presenter.isLogin()){
                rl_goto_msg.setVisibility(View.INVISIBLE);
                return;
            }
            int show = View.INVISIBLE;
            Map<String, ?> all = ShareDataLocal.as().getSharedPreferences().getAll();
            if (all != null) {
                Iterator<String> iterator = all.keySet().iterator();
                String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
                String condition=String.format(MessagePresenter.KEY_SHOW_REDDOT_FORMAT_PRE,userId);
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (key.startsWith(condition) && ShareDataLocal.as().getBooleanValue(key)) {
                        show = View.VISIBLE;
                        break;
                    }
                }
            }
            iv_hadmsg.setVisibility(show);
        }
    }

    private BroadcastReceiver msgBroadcastReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            iv_hadmsg.setVisibility(View.VISIBLE);
        }
    };
}
