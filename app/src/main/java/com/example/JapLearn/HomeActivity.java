package com.example.JapLearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView tvCurrLesson;
    private boolean isBackPressedOnce = false;
    UserModel userModel;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Make sure to update the layout resource name

        // Initialize components
        initializeComponents();
        checkUserAuthentication();
        setBottomNavigationListener();

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Handle button clicks
        Button btnLessons = findViewById(R.id.btnLessons);
        Button btnTest = findViewById(R.id.btnTest);
        tvCurrLesson = findViewById(R.id.tv_currLesson);

        btnLessons.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LessonList.class);
            intent.putExtra("reviewMode", false); // Not in review mode
            startActivity(intent);
        });

        btnTest.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LessonsActivity.class);
            intent.putExtra("reviewMode", false); // Not in review mode
            startActivity(intent);
        });

        // Load the current lesson from SharedPreferences
        loadCurrentLesson();
    }

    private void initializeComponents() { // Initializes the necessary UI components and the UserModel.
        bottomNavigationView = findViewById(R.id.bottomNavView);
        userModel = new UserModel();
        user = userModel.getFirebaseAuth().getCurrentUser();
    }

    private void checkUserAuthentication() { //Checks if the user is authenticated. If not, redirects to the login activity.
        if (user == null) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() { //Redirects the user to the login activity.
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setBottomNavigationListener() { // Sets the listener for the bottom navigation view to handle item selections.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return handleNavigationItemSelected(menuItem.getItemId());
            }
        });
    }

    private boolean handleNavigationItemSelected(int itemId) { // Handles the selection of the navigation items.
        Intent intent = null;

        if (itemId == R.id.navHome) {
            intent = new Intent(HomeActivity.this, HomeActivity.class);
        } else if (itemId == R.id.navLeaderboard) {
            intent = new Intent(HomeActivity.this, LeaderboardActivity.class);
        } else if (itemId == R.id.navKanashoot) {
            intent = new Intent(HomeActivity.this, KanaShootActivity.class);
        } else if (itemId == R.id.navNihongorace) {
            intent = new Intent(HomeActivity.this, NihongoRaceActivity.class);
        } else if (itemId == R.id.navProfile) {
            handleProfileSelection();
            return false;  // Return false to not select the profile item
        }

        if (intent != null) {
            startActivity(intent);
            finish();  // Optionally, you may want to finish the current activity to prevent going back to it.
        }

        return true;  // Item was handled
    }

    private void handleProfileSelection() {
        getUserRole(user.getUid(), new UserRoleCallback() {
            @Override
            public void onRoleRetrieved(String role) {
                showProfilePopupMenu(role);
            }

            @Override
            public void onError(String error) {
                Log.e("User Role", "Error: " + error);
            }
        });
    }

    private void showProfilePopupMenu(String role) { //Displays the profile popup menu based on the user's role.
        View view = bottomNavigationView.findViewById(R.id.navProfile);
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);

        if ("Admin".equals(role)) {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        } else if ("Teacher".equals(role)) {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu_teacher, popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu_student, popupMenu.getMenu());
        }

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleProfilePopupMenuItemClick(item.getItemId());
            }
        });
    }

    private boolean handleProfilePopupMenuItemClick(int itemId) {
        Intent intent = null;

        if (itemId == R.id.navProfile) {
            intent = new Intent(HomeActivity.this, ProfileActivity.class);
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;  // Return true to indicate the logout action was handled
        } else if (itemId == R.id.navAdminPanel) {
            intent = new Intent(HomeActivity.this, LessonItemList.class);
        } else if (itemId == R.id.navUserPanel) {
            intent = new Intent(HomeActivity.this, UserManagementActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
        return false;  // Return false if no intent was started
    }

    private void handleLogout() { //Logs out the user and redirects to the login activity.
        userModel.getFirebaseAuth().signOut();
        redirectToLogin();
    }

    public interface UserRoleCallback {
        void onRoleRetrieved(String role);
        void onError(String error);
    }

    private void getUserRole(String userId, UserRoleCallback callback) { //Fetches the user's role from the database.
        DatabaseReference userRef = userModel.getUserRef(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    callback.onRoleRetrieved(role);
                } else {
                    callback.onError("User data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    private void loadCurrentLesson() {
        // Reference to Firebase Database for the current user
        DatabaseReference currentLessonRef = userModel.getUserRef(user.getUid()).child("currentLesson");

        // Attach a listener to retrieve the currentLesson value from Firebase
        currentLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the currentLesson value from Firebase (stored as a string)
                    String currentLessonStr = dataSnapshot.getValue(String.class);

                    if (currentLessonStr != null) {
                        try {
                            // Parse the string into an integer for comparison
                            int currentLesson = Integer.parseInt(currentLessonStr);

                            // Map currentLesson to the corresponding lesson name
                            updateCurrentLessonText(currentLesson);
                        } catch (NumberFormatException e) {
                            // In case of a parsing error, default to lesson 1 (Hiragana)
                            updateCurrentLessonText(1);
                        }
                    } else {
                        // Handle null value by defaulting to lesson 1 (Hiragana)
                        updateCurrentLessonText(1);
                    }
                } else {
                    // If no value exists, default to lesson 1 (Hiragana)
                    updateCurrentLessonText(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle Firebase errors, if any
                Log.e("Firebase", "Error fetching currentLesson: " + databaseError.getMessage());
                // Default to lesson 1 (Hiragana) in case of error
                updateCurrentLessonText(1);
            }
        });
    }

    private void updateCurrentLessonText(int currentLesson) {
        String lessonName;

        // Map the current lesson number to the corresponding lesson name
        switch (currentLesson) {
            case 2:
                lessonName = "Katakana";
                break;
            case 3:
                lessonName = "Vocabulary";
                break;
            case 4:
                lessonName = "Grammar";
                break;
            case 1:
            default:
                lessonName = "Hiragana";
                break;
        }

        // Display the lesson name in tvCurrLesson
        tvCurrLesson.setText(lessonName);
    }





    @Override
    public void onResume() {
        super.onResume();
        // Disable back button handling when the activity is visible
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }
}

