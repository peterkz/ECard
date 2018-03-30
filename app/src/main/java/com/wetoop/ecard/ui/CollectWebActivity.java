package com.wetoop.ecard.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.ecard.R;

import cn.edots.nest.ui.BaseActivity;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/9/7.
 */
@Slug(layout = R.layout.activity_collect_web)
public class CollectWebActivity extends BaseActivity {

    @FindView(R.id.back_button)
    private RelativeLayout backButton;
    @FindView(R.id.title_text)
    private TextView titleText;
    @FindView(R.id.error)
    private TextView error;
    @FindView(R.id.myProgressBar)
    private ProgressBar progressBar;
    @FindView(R.id.webViewLogin)
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        error.setVisibility(View.GONE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
    }

    private void init() {
        webView.getSettings().setJavaScriptEnabled(true);//支持js脚本
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setGeolocationEnabled(false);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        webView.getSettings().setSupportZoom(true);//支持缩放
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.requestFocus();
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);

        Intent intent = getIntent();
        if (intent.getStringExtra("url") != null) {
            webView.loadUrl(intent.getStringExtra("url"));
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView = view;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                error.setVisibility(View.VISIBLE);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                titleText.setText(title);//a textview
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (View.INVISIBLE == progressBar.getVisibility()) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            //webView.goBack();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
