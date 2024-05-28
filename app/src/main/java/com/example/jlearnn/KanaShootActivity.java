package com.example.jlearnn;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jlearnn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

    public class KanaShootActivity extends AppCompatActivity {
        private WebView webView;
        private FirebaseAuth firebaseAuth;
        private DatabaseReference databaseReference;

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

            // Initialize Firebase Auth
            firebaseAuth = FirebaseAuth.getInstance();
            // Initialize Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users");
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

        public void saveGameResult(int finalWave, int finalScore) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userRef = databaseReference.child(userId);

                // Update user's game result in Firebase Database
                userRef.child("KSWaves").setValue(finalWave);
                userRef.child("KSHighScore").setValue(finalScore).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(KanaShootActivity.this, "Game result saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(KanaShootActivity.this, "Failed to save game result", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(KanaShootActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }