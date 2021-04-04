package com.rtx.combobettingtips;

import com.rtdx.ads.configs.AdConfig;
import com.rtdx.ads.configs.AdType;

public final class AppConstants {

    public static final String URL = "https://goldtiickets.blogspot.com/?m=0";

    public static AdConfig ADCOLONY() {
        AdConfig adColonyConfig = new AdConfig(AdType.ADCOLONY);
        adColonyConfig.APP_ID = "app7157710837c94a20b3";
        adColonyConfig.BANNER_ID = "vzf2eacbba2d5b4f3e9b";
        adColonyConfig.INTERSTITIAL_ID = "vz6bf9ceebda334dcb8c";
        adColonyConfig.REWARDED_ID = "vz19ca02a325b7407ba7";
        return adColonyConfig.autoTestMode();
    }

    public static AdConfig FACEBOOK() {
        AdConfig facebookConfig = new AdConfig(AdType.FACEBOOK);
//        facebookConfig.BANNER_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
//        facebookConfig.INTERSTITIAL_ID = "IMG_16_9_APP_INSTALL#2029572424039676_2029575330706052";
        return facebookConfig.autoTestMode();

    }

    public static AdConfig ADMOB() {
        AdConfig adMobConfig = new AdConfig(AdType.ADMOB);
        adMobConfig.APP_ID = "ca-app-pub-3779926585245428~9179272245";
        adMobConfig.BANNER_ID = "ca-app-pub-3779926585245428/9917638846";
        adMobConfig.INTERSTITIAL_ID = "ca-app-pub-3779926585245428/1452290035";
        return adMobConfig.autoTestMode();
    }

    public static AdConfig UNITY() {
        AdConfig unityConfig = new AdConfig(AdType.UNITY);
        unityConfig.APP_ID = "4067465";
        unityConfig.BANNER_ID = "Banner_Android";
        unityConfig.INTERSTITIAL_ID = "Interstitial_Android";
        unityConfig.REWARDED_ID = "Rewarded_Android";
        return unityConfig.autoTestMode();
    }
}
