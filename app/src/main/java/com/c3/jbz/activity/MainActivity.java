package com.c3.jbz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.presenter.MainPresenter;
import com.c3.jbz.view.MainView;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;

import java.lang.reflect.Method;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends MvpActivity<MainView, MainPresenter> implements MainView {
    @BindView(R.id.wv_main)
    WebView webView;

    @BindView(R.id.pb_main)
    ProgressBar pbMain;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ButterKnife.bind(this);
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
                Log.d("onPageStarted",url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
            }

        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if(newProgress==100){
                    pbMain.setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    pbMain.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pbMain.setProgress(newProgress);//设置进度值
                }

            }
        });

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
    }

    /**
     * 下载监听，当页面有可以下载的链接时触发
     */
    private DownloadListener downloadListener=new DownloadListener() {
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
            if(s==null)
                return;
            if (Boolean.valueOf(s)) {
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
            }
        }
    };

    @Override
    public void checkTopLevelPage() {
        webView.evaluateJavascript("javascript:isTopLevelPage()", valueCallback);
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
}
