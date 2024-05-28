package com.example.jlearnn;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


    public class LoginActivity extends AppCompatActivity {
    
        public EditText loginEmail, loginPassword;
        Button btnLogin;
        TextView registerRedirectText, forgotPasswordText;
        FirebaseAuth firebaseAuth;
    
        private FirebaseAuth.AuthStateListener authStateListener;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
    
            firebaseAuth = FirebaseAuth.getInstance();
            loginEmail = findViewById(R.id.loginEmail);
            loginPassword = findViewById(R.id.loginPassword);
            btnLogin = findViewById(R.id.btnLogIn);
            registerRedirectText = findViewById(R.id.txtRegister);
            forgotPasswordText = findViewById(R.id.forgotPasswordText);
    
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        Toast.makeText(LoginActivity.this, "User logged in", Toast.LENGTH_SHORT).show();
                        Intent I = new Intent(LoginActivity.this, navBar.class);
                        startActivity(I);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            };
    
            registerRedirectText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent I = new Intent(LoginActivity.this, RegistrationActivity.class);
                    startActivity(I);
                }
            });
    
            forgotPasswordText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent I = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(I);
                }
            });
    
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userEmail = loginEmail.getText().toString();
                    String userPassword = loginPassword.getText().toString();
                    if (userEmail.isEmpty()) {
                        loginEmail.setError("Provide your Email first!");
                        loginEmail.requestFocus();
                    } else if (userPassword.isEmpty()) {
                        loginPassword.setError("Enter Password!");
                        loginPassword.requestFocus();
                    } else if (userEmail.isEmpty() && userPassword.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
                    } else if (!(userEmail.isEmpty() && userPassword.isEmpty())) {
                        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Get the current date
                                    long currentDate = System.currentTimeMillis();
    
                                    // Get the user's ID
                                    String userId = firebaseAuth.getCurrentUser().getUid();
    
                                    // Get a reference to the user's data in Firebase
                                    DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId);
    
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            RegistrationActivity.User user = dataSnapshot.getValue(RegistrationActivity.User.class);
    
                                            if (user != null) {
                                                // Get the last login date
                                                long lastLoginDate = user.getLastLoginDate();
    
                                                // Calculate the difference in days between the last login date and the current date
                                                long diffInMillies = Math.abs(currentDate - lastLoginDate);
                                                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);
    
                                                if (diffInDays == 1) {
                                                    // If the difference is exactly one day, increment the daily streak
                                                    user.setDailyStreak(user.getDailyStreak() + 1);
                                                } else if (diffInDays > 1) {
                                                    // If the difference is more than one day, reset the daily streak to 1
                                                    user.setDailyStreak(1);
                                                }
    
                                                // Update the last login date and daily streak in Firebase
                                                user.setLastLoginDate(currentDate);
                                                userRef.setValue(user);
                                            }
                                        }
    
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle possible errors.
                                        }
                                    });
    
                                    startActivity(new Intent(LoginActivity.this, navBar.class));
                                } else {
                                    Toast.makeText(LoginActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    
        @Override
        protected void onStart() {
            super.onStart();
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

