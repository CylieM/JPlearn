package com.example.jlearnn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    EditText signupEmail, signupPassword, signupUsername;
    Button btnSignUp;
    TextView loginRedirectText, forgotPasswordText;
    RadioGroup roleRadioGroup;
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
        roleRadioGroup = findViewById(R.id.roleRadioGroup);

        // Button click listener for SignUp
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailID = signupEmail.getText().toString().trim();
                String paswd = signupPassword.getText().toString().trim();
                final String username = signupUsername.getText().toString().trim();

                int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedRoleButton = findViewById(selectedRoleId);
                final String role = selectedRoleButton.getText().toString();

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

                                        // Default profile picture resource name
                                        String defaultProfilePicture = "usericon.png"; // Resource name of the default profile picture
                                        long currentTimeMillis = System.currentTimeMillis(); // Get the current time in milliseconds
                                        User userObj = new User(username, emailID, defaultProfilePicture, role,
                                                0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), "1"); // Set lastLoginDate to current date
                                        userObj.setUserId(userId);
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
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "SignUp unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        });
            }
        });

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
    }

    // Define a User class to encapsulate user data
    public static class User {
        private String username;
        private String email;
        private String profilePicture;
        private String role;
        private String userId;
        private String currentLesson;
        private int KSWaves;
        private int KSHighScore;
        private int HiraganaProgress;
        private int KatakanaProgress;
        private int VocabularyProgress;
        private int NRaceBestWPM;
        private int NRaceFirstPlace;
        private int DailyStreak;
        private long lastLoginDate;



        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        public User() {}

        public User(String username, String email, String profilePicture, String role, int KSWaves, int KSHighScore, int HiraganaProgress, int KatakanaProgress, int VocabularyProgress, int NRaceBestWPM, int NRaceFirstPlace, int DailyStreak, long lastLoginDate, String currentLesson ) { // Modify this line
            this.username = username;
            this.email = email;
            this.profilePicture = profilePicture;
            this.role = role;
            this.KSWaves = KSWaves;
            this.KSHighScore = KSHighScore;
            this.HiraganaProgress = HiraganaProgress;
            this.KatakanaProgress = KatakanaProgress;
            this.VocabularyProgress = VocabularyProgress;
            this.NRaceBestWPM = NRaceBestWPM;
            this.NRaceFirstPlace = NRaceFirstPlace;
            this.DailyStreak = DailyStreak;
            this.lastLoginDate = lastLoginDate;
            this.currentLesson = currentLesson;
        }

        // Getter methods
        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
        public String getUserId() {
            return userId;
        }
        public String getProfilePicture() {
            return profilePicture;
        }

        public String getRole() {
            return role;
        }

        public int getKSWaves() {
            return KSWaves;
        }

        public int getKSHighScore() {
            return KSHighScore;
        }

        public int getHiraganaProgress() {
            return HiraganaProgress;
        }

        public int getKatakanaProgress() {
            return KatakanaProgress;
        }

        public int getVocabularyProgress() {
            return VocabularyProgress;
        }

        public int getNRaceBestWPM() {
            return NRaceBestWPM;
        }

        public int getNRaceFirstPlace() {
            return NRaceFirstPlace;
        }

        public int getDailyStreak() {
            return DailyStreak;
        }
        public String getCurrentLesson() { return currentLesson;}

        public long getLastLoginDate() { // Add this method
            return lastLoginDate;
        }

        // Setter methods
        public void setUsername(String username) {
            this.username = username;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setKSWaves(int KSWaves) {
            this.KSWaves = KSWaves;
        }

        public void setKSHighScore(int KSHighScore) {
            this.KSHighScore = KSHighScore;
        }

        public void setHiraganaProgress(int HiraganaProgress) {
            this.HiraganaProgress = HiraganaProgress;
        }

        public void setKatakanaProgress(int KatakanaProgress) {
            this.KatakanaProgress = KatakanaProgress;
        }

        public void setVocabularyProgress(int VocabularyProgress) {
            this.VocabularyProgress = VocabularyProgress;
        }

        public void setNRaceBestWPM(int NRaceBestWPM) {
            this.NRaceBestWPM = NRaceBestWPM;
        }

        public void setNRaceFirstPlace(int NRaceFirstPlace) {
            this.NRaceFirstPlace = NRaceFirstPlace;
        }

        public void setDailyStreak(int DailyStreak) {
            this.DailyStreak = DailyStreak;
        }

        public void setLastLoginDate(long lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
        }
        public void setCurrentLesson(String currentLesson) { // Add this method
            this.currentLesson = currentLesson;
        }
    }


}