package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NihongoRaceActivity extends AppCompatActivity {

    private UserModel userModel;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nihongo_race); // Ensure you have a corresponding layout file

        userModel = new UserModel();
        initializeComponents();
        bottomNavigationView.setSelectedItemId(R.id.navNihongorace);
        checkUserAuthentication();
        setBottomNavigationListener();

        // Initialize your buttons
        Button playButton = findViewById(R.id.btnPlay);
        Button joinRoomButton = findViewById(R.id.btnJoinRoom);
        Button practiceButton = findViewById(R.id.btnPractice);
        Button createRoomButton = findViewById(R.id.btnCreateRoom);

        // Get the current user's ID
        String userUid = userModel.getFirebaseAuth().getCurrentUser().getUid();

        // Call getUserRole with the user ID and a callback
        getUserRole(userUid, new UserRoleCallback() {
            @Override
            public void onRoleRetrieved(String role) {
                handleCreateRoomButtonVisibility(role); // Adjust button visibility based on the user role
            }

            @Override
            public void onError(String error) {
                // Handle any error that occurs during fetching the user role
                Toast.makeText(NihongoRaceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NihongoRaceActivity.this, StartGameRoomActivity.class);
                startActivity(intent);
            }
        });

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToJoinRoom();
            }
        });

        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToPracticeActivity();
            }
        });

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NihongoRaceActivity.this, CreateRoomActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUserRole(String userId, UserRoleCallback callback) {
        DatabaseReference userRef = userModel.getUserRef(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    // Call the callback with the retrieved role
                    callback.onRoleRetrieved(userRole);
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


    private void handleCreateRoomButtonVisibility(String userRole) {
        Button createRoomButton = findViewById(R.id.btnCreateRoom);

        if ("Teacher".equals(userRole) || "Admin".equals(userRole)) {
            createRoomButton.setVisibility(View.VISIBLE);
        } else {
            createRoomButton.setVisibility(View.GONE);
        }
    }

    private void redirectToJoinRoom() {
        Intent intent = new Intent(NihongoRaceActivity.this, JoinRoomActivity.class);
        startActivity(intent);
    }

    private void redirectToPracticeActivity() {
        Intent intent = new Intent(NihongoRaceActivity.this, PracticeNihongoRaceActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Disable back button handling when the activity is visible
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent back navigation
                // Optionally show a message or toast if you want
                Toast.makeText(NihongoRaceActivity.this, "Use the logout option to exit", Toast.LENGTH_SHORT).show();
            }
        });
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
            intent = new Intent(NihongoRaceActivity.this, HomeActivity.class);
        } else if (itemId == R.id.navLeaderboard) {
            intent = new Intent(NihongoRaceActivity.this, LeaderboardActivity.class);
        } else if (itemId == R.id.navKanashoot) {
            intent = new Intent(NihongoRaceActivity.this, KanaShootActivity.class);
        } else if (itemId == R.id.navNihongorace) {
            intent = new Intent(NihongoRaceActivity.this, NihongoRaceActivity.class);
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
        getUserRole(user.getUid(), new NihongoRaceActivity.UserRoleCallback() {
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
        PopupMenu popupMenu = new PopupMenu(NihongoRaceActivity.this, view);

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
            intent = new Intent(NihongoRaceActivity.this, ProfileActivity.class);
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;  // Return true to indicate the logout action was handled
        } else if (itemId == R.id.navAdminPanel) {
            intent = new Intent(NihongoRaceActivity.this, LessonItemList.class);
        } else if (itemId == R.id.navUserPanel) {
            intent = new Intent(NihongoRaceActivity.this, UserManagementActivity.class);
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


}
