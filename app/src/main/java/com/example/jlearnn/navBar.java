package com.example.jlearnn;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
        setContentView(R.layout.activity_navbar);
        initializeComponents();
        checkUserAuthentication();
        setBottomNavigationListener();
        loadFragment(new HomeFragment(), true);
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

    private boolean handleNavigationItemSelected(int itemId) { //Handles the selection of the profile item in the bottom navigation view.
        if (itemId == R.id.navHome) {
            loadFragment(new HomeFragment(), false);
        } else if (itemId == R.id.navLeaderboard) {
            loadFragment(new LeaderboardFragment(), false);
        } else if (itemId == R.id.navKanashoot) {
            startActivity(new Intent(navBar.this, KanaShootActivity.class));
            finish();
        } else if (itemId == R.id.navNihongorace) {
            loadFragment(new NihongoRaceFragment(), false);
        } else if (itemId == R.id.navProfile) {
            handleProfileSelection();
            return false;  // return false to not select the profile item
        }
        return true;
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
        if (itemId == R.id.navProfile) {
            loadFragment(new ProfileFragment(), false);
            return true;
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;
        } else if (itemId == R.id.navAdminPanel) {
            startActivity(new Intent(navBar.this, LessonItemList.class));
            return true;
        } else if (itemId == R.id.navUserPanel) {
            startActivity(new Intent(navBar.this, UserManagementActivity.class));
            return true;
        }
        return false;
    }

    private void handleLogout() { //Logs out the user and redirects to the login activity.
        userModel.getFirebaseAuth().signOut();
        redirectToLogin();
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) { //Loads a fragment into the frame layout.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isAppInitialized) {
            fragmentTransaction.add(R.id.framelayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.framelayout, fragment);
        }
        fragmentTransaction.commit();
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
