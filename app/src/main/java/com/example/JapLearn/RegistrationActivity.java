package com.example.JapLearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegistrationActivity extends AppCompatActivity {
    EditText signupEmail, signupPassword, signupUsername;
    Button btnSignUp;
    TextView loginRedirectText, forgotPasswordText;
    FirebaseAuth firebaseAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = firebaseDatabase.getReference("users");

        // Initialize views
        signupEmail = findViewById(R.id.edEmail);
        signupPassword = findViewById(R.id.edPassword);
        signupUsername = findViewById(R.id.edUsername);
        btnSignUp = findViewById(R.id.btnSignUp);
        loginRedirectText = findViewById(R.id.txtSignIn);
        forgotPasswordText = findViewById(R.id.txtForgotPassword);

        // Button click listener for SignUp
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailID = signupEmail.getText().toString().trim();
                String paswd = signupPassword.getText().toString().trim();
                final String username = signupUsername.getText().toString().trim();

                if (emailID.isEmpty()) {
                    signupEmail.setError("Provide your Email first!");
                    signupEmail.requestFocus();
                    return;
                }
                if (username.isEmpty()) {
                    signupUsername.setError("Provide your username!");
                    signupUsername.requestFocus();
                    return;
                }
                if (username.length() > 12) {
                    signupUsername.setError("Username must be 12 characters or less");
                    signupUsername.requestFocus();
                    return;
                }

                if (paswd.isEmpty()) {
                    signupPassword.setError("Set your password");
                    signupPassword.requestFocus();
                    return;
                }

                // Proceed with registration
                firebaseAuth.createUserWithEmailAndPassword(emailID, paswd)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        // Save username, profile picture, role, and additional stats to Realtime Database
                                        String userId = user.getUid();

                                        // Reference to Firebase Storage
                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/default.png");

                                        // Get the download URL for the default profile picture
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String defaultProfilePictureUrl = uri.toString(); // Get the download URL
                                                long currentTimeMillis = System.currentTimeMillis(); // Get the current time in milliseconds

                                                UserModel.User userObj = new UserModel.User(username, emailID, defaultProfilePictureUrl, "Student",
                                                        0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), "1"); // Set lastLoginDate to current date
                                                userObj.setUserId(userId);

                                                // Save user data to Realtime Database
                                                usersRef.child(userId).setValue(userObj)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Intent I = new Intent(RegistrationActivity.this, navBar.class);
                                                                    I.putExtra("email", emailID);
                                                                    startActivity(I);
                                                                } else {
                                                                    Toast.makeText(RegistrationActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegistrationActivity.this, "Failed to get default profile picture URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "SignUp unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }); // End of btnSignUp click listener

        // Redirect to login activity
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(I);
            }
        });

        // Redirect to forgot password activity
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I = new Intent(RegistrationActivity.this, ForgotPasswordActivity.class);
                startActivity(I);
            }
        });
    } // End of onCreate method
} // End of RegistrationActivity class

