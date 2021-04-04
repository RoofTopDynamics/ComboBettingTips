package com.rtx.combobettingtips;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rtdx.ads.controllers.UnifiedAdController;

public class BaseActivity extends AppCompatActivity {

    private boolean mExitTriggered = false;
    protected UnifiedAdController adController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adController = new UnifiedAdController(this);
    }

    protected void exitApp(boolean exitImmediate) {

        if (mExitTriggered || exitImmediate) {
            //Exit
            finishAffinity();
        } else {
            //Notify user that they have triggered exit of app
            Toast toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            //Prepare exit
            mExitTriggered = true;

            //If back not pressed within 2 secs reset exit triggered status
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                mExitTriggered = false;
            }, 2000);
        }
    }

    protected void openStoreRatePage() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    protected void openShareMenu() {
        Intent a = new Intent(Intent.ACTION_SEND);

        //this is to get the app link in the playstore before publishing your app.
        final String appPackageName = getApplicationContext().getPackageName();
        String strAppLink;

        try {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        } catch (android.content.ActivityNotFoundException anfe) {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        // this is the sharing part
        a.setType("text/link");
        String shareBody = "Hey! GET FREE 🔥 HOT 🔥 Betting Tips Daily." +
                "\n" + "" + strAppLink;
        String shareSub = "Fixed Betting Tips";
        a.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        a.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(a, "Share Using"));

    }

    // End of banner ad Override methods
    @Override
    public void onBackPressed() {
        //Override default exit and notify user they are about to exit
        exitApp(false);
    }

    @Override
    public void onPause() {
        if (adController != null) {
            adController.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adController != null) {
            adController.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (adController != null) {
            adController.onDestroy();
        }
        super.onDestroy();
    }

    //Utility methods
    protected void logMessage(String message) {
        if (BuildConfig.BUILD_DEBUG) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.d(getClass().toString().toUpperCase(), message.toUpperCase());
        }
    }
}
