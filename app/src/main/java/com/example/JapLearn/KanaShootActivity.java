package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class KanaShootActivity extends AppCompatActivity {
    private WebView webView;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kana_shoot);

        webView = findViewById(R.id.webView);

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

            userRef.child("KSHighScore").setValue(finalScore).addOnCompleteListener(task -> {
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
        }
    }
}