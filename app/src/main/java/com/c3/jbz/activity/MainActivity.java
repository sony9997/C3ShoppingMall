package com.c3.jbz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.comp.C3WebChromeClient;
import com.c3.jbz.presenter.MainPresenter;
import com.c3.jbz.util.ToolsUtil;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    private ProgressDialog pd;

    private C3WebChromeClient c3WebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ButterKnife.bind(this);
        c3WebChromeClient=new C3WebChromeClient(this,pbMain,tv_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ToolsUtil.verifyStoragePermissions(this);
        loadMainPage();
    }


    @NonNull
    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getApplicationContext());
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
        Toast.makeText(this, msgId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadMainPage() {
        getPresenter().loadMainPage();
    }

    @Override
    public void initMainPage(String url, Object jsObject) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("onPageStarted", url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
                if(tv_title!=null)
                    tv_title.setText(view.getTitle());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view,url);
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
        String errStr=resp.errStr;
        errStr=errStr!=null&&errStr.trim().length()>0?errStr:"";
        webView.evaluateJavascript(String.format(BuildConfig.WEB_JS_NAME_handleWXRespEvent,resp.getType(),resp.errCode,errStr), null);
        switch (resp.getType()){
            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:{
                if(resp.errCode==BaseResp.ErrCode.ERR_OK){
                    Toast.makeText(this,R.string.title_share_success,Toast.LENGTH_LONG).show();
                }else if(resp.errCode==BaseResp.ErrCode.ERR_USER_CANCEL){
                    Toast.makeText(this,R.string.title_share_cancel,Toast.LENGTH_LONG).show();
                }else {
                    errStr=errStr.length()>0?errStr:getString(R.string.title_share_faild);
                    Toast.makeText(this,errStr,Toast.LENGTH_LONG).show();
                }
                break;
            }
            case ConstantsAPI.COMMAND_PAY_BY_WX:{
                if(resp.errCode==BaseResp.ErrCode.ERR_OK){
                    Toast.makeText(this,R.string.title_pay_success,Toast.LENGTH_LONG).show();
                }else if(resp.errCode==BaseResp.ErrCode.ERR_USER_CANCEL){
                    Toast.makeText(this,R.string.title_pay_cancel,Toast.LENGTH_LONG).show();
                }else {
                    errStr=errStr.length()>0?errStr:getString(R.string.title_pay_faild);
                    Toast.makeText(this,errStr,Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        super.onDestroy();
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
        c3WebChromeClient.onActivityResult(requestCode,resultCode,data);
    }

    @OnClick(R.id.iv_back)
    public void goPre(View view){
        checkTopLevelPage();
    }
    @OnClick(R.id.iv_share)
    public void goShare(View view){
        webView.evaluateJavascript(BuildConfig.WEB_JS_NAME_goShare, null);
    }
}
