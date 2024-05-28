package com.example.jlearnn;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class MyJavaScriptInterface {
    private KanaShootActivity activity;

    public MyJavaScriptInterface(KanaShootActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void onGameEnded(int finalWave, int finalScore) {
        // Handle the final wave and score received from the game
        Log.d("KanaShootActivity", "Final Wave: " + finalWave + ", Final Score: " + finalScore);
        // Here you can store the final wave and score in the user's account data
        activity.saveGameResult(finalWave, finalScore);
    }
}
