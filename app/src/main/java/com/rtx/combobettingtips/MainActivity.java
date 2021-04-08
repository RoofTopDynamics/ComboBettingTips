package com.rtx.combobettingtips;

import android.annotation.SuppressLint;
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
import com.github.ybq.android.spinkit.style.FoldingCube;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.rtdx.ads.base.BaseActivity;
import com.rtdx.ads.utils.AdConfig;

public class MainActivity extends BaseActivity {

    private WebView webView;
    SwipeRefreshLayout refreshLayout;
    ViewGroup progressLayout;
    Button reloadBtn;
    private String postScript = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        Sprite doubleBounce = new FoldingCube();
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
                executePostScript();
                if (progress < 100) {
                } else { // Progress >= 100
                    progressLayout.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.VISIBLE);
                }
            }

            public void onPageFinished(WebView view, String weburl) {
                executePostScript();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(android.webkit.WebView view, int progress) {
                executePostScript();
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
                fetchRemoteConfigs();
                refreshLayout.setRefreshing(false);
            }
        });

        if (isInternetConnected(this)) {
            fetchRemoteConfigs();
        } else {
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        if (root != null) {
            View noInternetLayout = getLayoutInflater().inflate(R.layout.activity_no_connection, null);
            root.addView(noInternetLayout);
            Button reloadbtn = (Button) noInternetLayout.findViewById(R.id.reloadbtn);
            reloadbtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (isInternetConnected(MainActivity.this)) {
                        root.removeView(noInternetLayout);
                        fetchRemoteConfigs();
                    }
                }
            });
        }
    }

    private void executePostScript() {
        webView.loadUrl(postScript);
    }

    private void loadUrl(String url) {
        if (isInternetConnected(getApplicationContext())) {
            refreshLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        } else {
            progressLayout.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            logMessage("Turn on Wifi or Mobile Data");
        }
        executePostScript();
    }

    private void fetchRemoteConfigs() {
        if (!isInternetConnected(this)) {
            showNoInternetDialog();
        }

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

                            String url = mFirebaseRemoteConfig.getString("url");
                            postScript = mFirebaseRemoteConfig.getString("post_script");

                            loadUrl(url);

                            //Ads Stuff
                            String adType = mFirebaseRemoteConfig.getString("ad_type").toLowerCase();
                            if (BuildConfig.BUILD_DEBUG) {
                                adType = mFirebaseRemoteConfig.getString("ad_type_test");
                            }
                            double adFilter = mFirebaseRemoteConfig.getDouble("ad_filter");
                            logMessage("ADFILTER => " + adFilter + " | " + "Version Code => " + (double) BuildConfig.VERSION_CODE);

                            if (adFilter >= (double) BuildConfig.VERSION_CODE || adType.equals("ad_type_test")) {
                                fetchCollections(adType);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                            showNoInternetDialog();
                            logMessage("Turn on Wifi or Mobile Data");
                        }
                    }
                });
    }

    private void fetchCollections(String adType) {
        FirebaseFirestore.getInstance().collection("ad_ids").document(adType).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                logMessage("DocumentSnapshot data: " + document.getData());
                                AdConfig adConfig = new AdConfig(adType);
                                adConfig.APP_ID = document.getString("APP_ID");
                                adConfig.BANNER_ID = document.getString("BANNER_ID");
                                adConfig.INTERSTITIAL_ID = document.getString("INTERSTITIAL_ID");
                                adConfig.VIDEO_ID = document.getString("VIDEO_ID");
                                adConfig.REWARDED_ID = document.getString("REWARDED_ID");
                                adConfig.setAutoTestMode();
                                logMessage(adConfig.toString());
                                loadAd(adConfig);
                            } else {
                                logMessage("No such document => " + adType);
                            }
                        } else {
                            logMessage("get failed with " + task.getException());
                        }
                    }
                });
    }

    //Ads
    private boolean adsLoaded = false;

    public void loadAd(AdConfig adConfig) {
        if (adsLoaded) return;

        adController.setAdConfig(adConfig);
        adController.loadBannerAd();
        adController.loadInterstitialAd(true);

        adsLoaded = true;
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
                openRateDialog("#450148");
                return true;

            case R.id.sharebtnHM:
                openShareMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
