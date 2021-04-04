package com.rtx.combobettingtips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends BaseActivity {

    private WebView webView;
    SwipeRefreshLayout refreshLayout;
    ViewGroup progressLayout;
    View noInternetLayout;
    Button reloadBtn;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        Sprite doubleBounce = new DoubleBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);

        progressLayout = findViewById(R.id.progress_layout);
        refreshLayout = findViewById(R.id.swipe);
        progressLayout.setVisibility(View.VISIBLE);
        refreshLayout.setVisibility(View.GONE);

        webView = findViewById(R.id.webview);
        webView.requestFocus();
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
        //webView.getSettings().setDatabasePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/databases");// deprecated

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return true; // then it is not handled by default action
            }

            public void onProgressChanged(android.webkit.WebView view, int progress) {
                hideTop();
                if (progress < 100) {
                } else { // Progress >= 100
                    progressLayout.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.VISIBLE);
                }
            }

            public void onPageFinished(WebView view, String weburl) {
                hideTop();
                loadAds();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(android.webkit.WebView view, int progress) {
                hideTop();
                if (progress < 100) {
                } else { // Progress >= 100
                    progressLayout.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.VISIBLE);
                }
            }
        });


        webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {

            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                loadUrl();
                refreshLayout.setRefreshing(false);
            }
        });

        noInternetLayout = findViewById(R.id.no_internet_layout);
        reloadBtn = noInternetLayout.findViewById(R.id.reload_btn);
        reloadBtn.setOnClickListener(view -> {
            loadUrl();
        });

        loadUrl();
    }

    private void hideTop() {
        webView.loadUrl("javascript:(function f() { " +
                "var images = document.getElementsByTagName('p'); " +
                "for(var i = 0; i < images.length; i++){" +
                "images[i].style.display = 'none';" +
                "}" +
                "} )()");
        webView.loadUrl("javascript:(function f() { " +
                "var alerts = document.getElementsByClassName('alert'); " +
                "for(var i = 0; i < alerts.length; i++){" +
                "alerts[i].style.display = 'none';" +
                "}" +
                "} )()");
        webView.loadUrl("javascript:(function f() { " +
                "var buttons = document.getElementsByTagName('button'); " +
                "for(var i = 0; i < buttons.length; i++){" +
                "buttons[i].style.display = 'none';" +
                "}" +
                "} )()");
        //       webView.loadUrl("javascript:(function f() { document.getElementsByTagName('img')[0].style.display = 'none'; } )()");
        //       webView.loadUrl("javascript:(function f() { document.getElementsByClassName('alert')[1].style.display = 'none'; } )()");
        //       webView.loadUrl("javascript:(function f() { document.getElementsByClassName('alert')[2].style.display = 'none'; } )()");
        //       webView.loadUrl("javascript:(function f() { document.getElementsByClassName('alert')[3].style.display = 'none'; } )()");
    }

    private void loadUrl() {
        if (isInternetConnected(getApplicationContext())) {
            refreshLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            webView.loadUrl(AppConstants.URL);
        } else {
            progressLayout.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            logMessage("Turn on Wifi or Mobile Data");
        }
        hideTop();
    }

    // Check Connection
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

    private void loadAds() {

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();

                            String adType = mFirebaseRemoteConfig.getString("ad_type");

                            switch (adType) {
                                case "admob":
                                    testAdmob();
                                    break;
                                case "facebook":
                                    testFacebook();
                                    break;
                                case "adcolony":
                                    testAdColony();
                                    break;
                                case "unity":
                                    testUnity();
                                    break;
                                default:
                                    //DO NOTHING
                                    break;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void testAdColony() {
        adController.setAdConfig(AppConstants.ADCOLONY());
        adController.loadBannerAd((ViewGroup) findViewById(R.id.ad_view));
        adController.loadInterstitialAd(true);
    }

    private void testFacebook() {
        adController.setAdConfig(AppConstants.FACEBOOK());
        adController.loadBannerAd((ViewGroup) findViewById(R.id.ad_view));
        adController.loadInterstitialAd(true);
    }

    private void testAdmob() {
        adController.setAdConfig(AppConstants.ADMOB());
        adController.loadBannerAd((ViewGroup) findViewById(R.id.ad_view));
        adController.loadInterstitialAd(true);
    }

    private void testUnity() {
        adController.setAdConfig(AppConstants.UNITY());
        adController.loadBannerAd((ViewGroup) findViewById(R.id.ad_view));
        adController.loadInterstitialAd(true);
    }

    // functions for the action bar buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_hma, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rate_usbtnHM:
                openStoreRatePage();
                return true;

            case R.id.sharebtnHM:
                openShareMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
