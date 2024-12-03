package com.example.JapLearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class KanaShootActivity extends AppCompatActivity {
    private WebView webView;
    private UserModel userModel;
    private static final String PREFS_NAME = "KanaShootPrefs";
    private static final String PREF_DONT_SHOW_AGAIN = "dont_show_again";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kana_shoot);

        webView = findViewById(R.id.webView);
        userModel = new UserModel();
        // Enable JavaScript in the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Add JavaScript interface
        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "Android");

        // Load the game URL
        webView.loadUrl("https://cyliem.github.io/KanaShootMain/");

        // Set a WebViewClient to handle navigation within the WebView
        webView.setWebViewClient(new WebViewClient());

        // Initialize UserModel
        userModel = new UserModel();

        // Show directions popup
        showDirectionsPopupIfNeeded();
    }

    private void showDirectionsPopupIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean dontShowAgain = prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false);

        if (!dontShowAgain) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialogue_directions, null);
            CheckBox checkBox = dialogView.findViewById(R.id.checkbox_dont_show_again);

            new AlertDialog.Builder(this)
                    .setTitle("Game Directions")
                    .setView(dialogView)
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (checkBox.isChecked()) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.apply();
                        }
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        // Add flags to clear the activity stack if you want to ensure a clean navigation back to HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish if you want to close the current activity
    }

    public class MyJavaScriptInterface {
        KanaShootActivity activity;

        MyJavaScriptInterface(KanaShootActivity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void onGameEnded(int finalWave, int finalScore) {
            activity.saveGameResult(finalWave, finalScore);
        }
    }

    private void saveGameResult(int finalWave, int finalScore) {
        FirebaseUser currentUser = userModel.getFirebaseAuth().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = userModel.getUserRef(userId);

            // Update user's game result in Firebase Database
            userRef.child("KSWaves").setValue(finalWave).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firebase", "Wave data saved successfully: " + finalWave);
                } else {
                    Log.e("Firebase", "Failed to save wave data", task.getException());
                }
            });

            userRef.child("kshighScore").setValue(finalScore).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firebase", "Score data saved successfully: " + finalScore);
                } else {
                    Log.e("Firebase", "Failed to save score data", task.getException());
                }
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(KanaShootActivity.this, "Game result saved successfully", Toast.LENGTH_SHORT).show();

                    // Start the test
                    Intent intent = new Intent(KanaShootActivity.this, KanaBoardActivity.class);
                    startActivity(intent);

                    // Finish the current activity
                    finish();
                } else {
                    Toast.makeText(KanaShootActivity.this, "Failed to save game result", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(KanaShootActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            // Redirect to login if user is not logged in
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Optional: Finish the current activity
        }
    }
}
