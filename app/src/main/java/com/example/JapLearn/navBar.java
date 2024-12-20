package com.example.JapLearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class navBar extends AppCompatActivity {
    UserModel userModel;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeComponents();
        checkUserAuthentication();
        setBottomNavigationListener();
        loadHomeActivity();
    }
    private void loadHomeActivity() {
        Intent intent = new Intent(navBar.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Optional: call finish() to prevent back navigation
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
            intent = new Intent(navBar.this, HomeActivity.class);
        } else if (itemId == R.id.navLeaderboard) {
            intent = new Intent(navBar.this, LeaderboardActivity.class);
        } else if (itemId == R.id.navKanashoot) {
            intent = new Intent(navBar.this, KanaShootActivity.class);
        } else if (itemId == R.id.navNihongorace) {
            intent = new Intent(navBar.this, NihongoRaceActivity.class);
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
        PopupMenu popupMenu = new PopupMenu(navBar.this, view);

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
            intent = new Intent(navBar.this, ProfileActivity.class);
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;  // Return true to indicate the logout action was handled
        } else if (itemId == R.id.navAdminPanel) {
            intent = new Intent(navBar.this, LessonItemList.class);
        } else if (itemId == R.id.navUserPanel) {
            intent = new Intent(navBar.this, UserManagementActivity.class);
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
}

