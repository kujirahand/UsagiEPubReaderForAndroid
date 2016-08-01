package com.kujirahand.usagireader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.kujirahand.utils.DialogHelper;

/**
 * Created by kujira on 2016/04/15.
 */
public class WebActivity extends Activity {

    final public String LOG_TAG = "Usagi.Web";

    protected boolean useFullScreen = false;
    protected boolean useBuiltInZoomControls = true;
    protected LinearLayout root;
    protected WebView webview;
    protected Uri viewUri;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSetWindowFlags(getWindow());
        buildMainView();
        setContentView(root);

        // ---
        DialogHelper.parent = this;

        // get parameter
        Intent intent = getIntent();
        String action = intent.getAction();
        if (!Intent.ACTION_VIEW.equals(action)) {
            Log.d("Usagi", "Not set action VIEW.");
            return;
        }
        viewUri = intent.getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewUri == null) {
            this.finish();
            return;
        }
        Log.d("Usagi", "UsagiPath=" + viewUri.toString());
        webview.loadUrl(viewUri.toString());
        webview.requestFocus();
    }

    @Override
    protected void onDestroy() {
        setVisible(false);
        super.onDestroy();
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    public void onSetWindowFlags(Window w) {
        // title
        w.requestFeature(Window.FEATURE_NO_TITLE);

        // full screen
        if (useFullScreen) {
            w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            w.setFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        // keep screen (do not sleep)
        // w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void buildMainView() {
        // Parameters from Layout
        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0.0F);
        LinearLayout.LayoutParams webviewParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1.0F);
        // Add root to Layout
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(containerParams);
        //
        webview = new WebView(this);
        webview.setLayoutParams(webviewParams);
        setWebViewParams();
        root.addView(webview);

        // for AdMob
        /*
        if (waffle_flags.useAdMob) {
            adView = new AdView(getApplicationContext());
            adView.setAdUnitId(waffle_flags.idAdMob);
            adView.setAdSize(AdSize.BANNER);

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            adView.loadAd(adRequest);
            root.addView(adView);
            flagAdViewVislbe = true;
        }
        */
    }
    class jsWaffleWebViewClient extends WebViewClient {
        private Context appContext = null;

        public jsWaffleWebViewClient(Context con) {
            super();
            appContext = con;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // HTTP or HTTPS
            if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url);
                return false;
            }
            Log.d(LOG_TAG, "loadUrl=" + url);
            /*
            boolean b = IntentHelper.run(appContext, url);
            if (!b) {
                log("loadUrl=" + url);
                view.loadUrl(url); // browse url in waffle browser
                return false;
            }
            */
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        @Override
        public void onPageFinished(WebView view, String url) {
        }


    }

    long downloadId = 0;

    protected void setWebViewParams() {
        webview.setWebChromeClient(new jsWaffleChromeClient(this));
        webview.setWebViewClient(new jsWaffleWebViewClient(this));

        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                /*
                // よく分からないけどダウンロードできない
                DownloadManager downLoadManager_ = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                String fname = uri.getLastPathSegment().replace("/", "_");
                Log.d("download", "url="+url);
                Log.d("download", "fname="+fname);
                Log.d("download", "contentDisposition="+contentDisposition);
                request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, "/"+fname);
                request.setTitle("Download EPUB: " + fname);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setMimeType(mimetype);
                downloadId = downLoadManager_.enqueue(request);
                */
                // ブラウザからダウンロードする場合
                Log.d("download", mimetype);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType(mimetype);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // webview.setInitialScale(waffle_flags.initialScale);
        // scroll bar
        //webview.setVerticalScrollBarEnabled(waffle_flags.useVerticalScrollBar);
        //webview.setHorizontalScrollBarEnabled(waffle_flags.useHorizontalScrollBar);

        WebSettings setting = webview.getSettings();

        setting.setDefaultTextEncodingName("utf-8");
        setting.setJavaScriptEnabled(true);
        setting.setDomStorageEnabled(true);

        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        setting.setSupportZoom(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(LOG_TAG, "SDK >= KITKAT");
            setting.setUseWideViewPort(true);
            setting.setLoadWithOverviewMode(true);
            setting.setBuiltInZoomControls(useBuiltInZoomControls);
            // webview.setInitialScale(1);
        } else {
            Log.d(LOG_TAG, "SDK < KITKAT");
            setting.setBuiltInZoomControls(useBuiltInZoomControls);
        }
    }

    class jsWaffleChromeClient extends WebChromeClient {

        protected Context appContext = null;

        public jsWaffleChromeClient(Context con) {
            super();
            this.appContext = con;
        }

        // for Android 2.x
        public void addMessageToConsole(String message, int lineNumber, String sourceID) {
            Log.e(LOG_TAG,
                    sourceID + ": Line " + Integer.toString(lineNumber) + " : " +
                            message);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            boolean r = false;
            try {
                r = DialogHelper.alert("Information", message, result);
            } catch (Exception e) {
                Log.e(LOG_TAG, "[DialogError]" + e.getMessage());
                result.cancel();
            }
            return r;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            boolean r = false;
            try {
                r = DialogHelper.confirm("Confirm", message, result);
            } catch (Exception e) {
                Log.e(LOG_TAG, "[DialogError]" + e.getMessage());
                result.cancel();
            }
            return r;
        }


        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            // show Dialog extends "prompt()"
            boolean r = true;
            try {
                switch (DialogHelper.dialogType) {
                    case DialogHelper.DIALOG_TYPE_DEFAULT:
                        r = DialogHelper.inputDialog("Prompt", message, defaultValue, result);
                        break;
                    case DialogHelper.DIALOG_TYPE_CHECKBOX_LIST:
                        r = DialogHelper.checkboxList(DialogHelper.dialogTitle, message, defaultValue, result);
                        break;
                    case DialogHelper.DIALOG_TYPE_DATE:
                        r = DialogHelper.datePickerDialog(DialogHelper.dialogTitle, message, defaultValue, result);
                        break;
                    case DialogHelper.DIALOG_TYPE_TIME:
                        r = DialogHelper.timePickerDialog(DialogHelper.dialogTitle, message, defaultValue, result);
                        break;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "[DialogError]" + e.getMessage());
                result.cancel();
            }
            return r;
        }

        //Android 1.6 not supported
        @Override
        public boolean onJsBeforeUnload(android.webkit.WebView view, java.lang.String url, java.lang.String message, android.webkit.JsResult result) {
            Log.d(LOG_TAG, "[onJsBeforeUnload]");
            return false;
        }
    }

}
