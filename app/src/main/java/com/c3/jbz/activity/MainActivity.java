package com.c3.jbz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
public class MainActivity extends MvpActivity<MainView,MainPresenter> implements MainView {
    @BindView(R.id.wv_main)
    WebView webView;

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
        if(pd==null){
            pd=new ProgressDialog(this);
        }

        pd.setCancelable(false);
        pd.setMessage(getResources().getStringArray(R.array.pd_titles)[type]);
        pd.show();
    }

    @Override
    public void loadMainPage() {
        getPresenter().loadMainPage();
    }

    @Override
    public void initMainPage(String url,Object jsObject) {
        onLoading(MainView.LOADINGTYPE_MAINPAGE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
            }

        });
        // 设置WebView属性，能够执行Javascript脚本
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setSavePassword(false);
        setPageCacheCapacity(webViewSettings);
        webViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置允许JS弹窗
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(jsObject,"androidInvoker");
        webView.loadUrl(url);
    }

    protected void setPageCacheCapacity(WebSettings webSettings) {
        try {
            Class<?> c = Class.forName("android.webkit.WebSettingsClassic");
            Method tt = c.getMethod("setPageCacheCapacity", new Class[] { int.class });
            tt.invoke(webSettings, 5);
        } catch (ClassNotFoundException e) {
            System.out.println("No such class: " + e);
        } catch (Exception e) {
            Log.e("ERROR:", e.getMessage());
        }
    }

    @Override
    public void hideLoading() {
        if(pd!=null&&pd.isShowing()){
            pd.dismiss();
        }
        pd=null;
    }

    private ValueCallback<String> valueCallback= new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String s) {
            if(Boolean.valueOf(s)){
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
        webView.evaluateJavascript("javascript:isTopLevelPage()",valueCallback);
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
