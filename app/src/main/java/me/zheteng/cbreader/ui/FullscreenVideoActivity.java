package me.zheteng.cbreader.ui;

import java.io.IOException;

import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.utils.Utils;

public class FullscreenVideoActivity extends ActionBarActivity {

    WebView mWebView;
    String mType;
    String mData;

    public static void startNewInstance(Context context, String type, String data) {
        Intent intent = new Intent(context, FullscreenVideoActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("data", data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        mWebView = ((WebView) findViewById(R.id.webview));
        mType = getIntent().getStringExtra("type");
        mData = getIntent().getStringExtra("data");

        init();

        loadVideo();

    }

    private void loadVideo() {
        String html = Utils.loadAssetTextAsString(this, "fullscreenplayer.html");
        String videoHtml = "";
        if (mType.equals("tudou")) {
            videoHtml = ReadFragment.VideoPlaceHolders.TUDOU.replace("$1", mData);
        } else if (mType.equals("youku")) {
            videoHtml = ReadFragment.VideoPlaceHolders.YOUKU.replace("$1", mData);
        } else if (mType.equals("sohu")) {
            videoHtml = ReadFragment.VideoPlaceHolders.SOHU.replace("$1", mData);
        }

        assert html != null;
        html = html.replaceAll("\\{placeholder\\}", videoHtml);
        mWebView.loadData(html, "text/html;charset=UTF-8", "utf-8");
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like "
                        + "Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                // hack 搜狐视频
                if (url.contains("http://tv.sohu.com/upload/touch/css/albumPlayerPage.min")) {
                    try {
                        WebResourceResponse resourceResponse =
                                new WebResourceResponse("text/css", "utf-8", getAssets().open("inject.css"));
                        return resourceResponse;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        mWebView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    requestImmersiveMode();
                }
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                requestImmersiveMode();
                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void requestImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onPause(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        if (!BuildConfig.DEBUG) {
            MobclickAgent.onResume(this);
        }
    }
}
