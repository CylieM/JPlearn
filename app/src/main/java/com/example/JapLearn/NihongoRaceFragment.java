package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NihongoRaceFragment extends Fragment {

    private UserModel userModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nihongo_race, container, false);
        userModel = new UserModel();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button playButton = view.findViewById(R.id.btnPlay);
        Button joinRoomButton = view.findViewById(R.id.btnJoinRoom);
        Button practiceButton = view.findViewById(R.id.btnPractice);
        Button createRoomButton = view.findViewById(R.id.btnCreateRoom);
        String userUid = userModel.getFirebaseAuth().getCurrentUser().getUid();
        getUserRole(userUid);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOrCreateLobby();
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
                Intent intent = new Intent(getActivity(), CreateRoomActivity.class);
                startActivity(intent);
            }
        });
    }


    private void getUserRole(String userId) {
        DatabaseReference userRef = userModel.getUserRef(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    handleCreateRoomButtonVisibility(userRole);
                } else {
                    // Handle the case where user data doesn't exist
                    Log.w("NihongoRaceFragment", "User data does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors (e.g., database read failure)
                Log.e("NihongoRaceFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void handleCreateRoomButtonVisibility(String userRole) {
        Button createRoomButton = getView().findViewById(R.id.btnCreateRoom);

        if ("Teacher".equals(userRole) || "Admin".equals(userRole)) {
            createRoomButton.setVisibility(View.VISIBLE);
        } else {
            createRoomButton.setVisibility(View.GONE);
        }
    }

    private void checkOrCreateLobby() {
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("lobbies");

        lobbyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot lobbySnapshot : snapshot.getChildren()) {
                        String lobbyId = lobbySnapshot.getKey();
                        if (lobbyId != null) {
                            joinLobby(lobbyId);
                            return;
                        }
                    }
                }
                createLobby();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NihongoRaceFragment", "Error checking lobbies: " + error.getMessage());
            }
        });
    }

    private void joinLobby(String lobbyId) {
        Intent intent = new Intent(getActivity(), StartGameRoomActivity.class);
        intent.putExtra("LOBBY_ID", lobbyId);
        startActivity(intent);
    }

    private void createLobby() {
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("lobbies");
        String userId = userModel.getFirebaseAuth().getCurrentUser().getUid();
        String newLobbyId = lobbyRef.push().getKey();
        if (newLobbyId != null) {
            lobbyRef.child(newLobbyId).child("creator").setValue(userId);
            Intent intent = new Intent(getActivity(), StartGameRoomActivity.class);
            intent.putExtra("LOBBY_ID", newLobbyId);
            startActivity(intent);
        }
    }

    private void redirectToJoinRoom() {
        Intent intent = new Intent(getActivity(), JoinRoomActivity.class);
        startActivity(intent);
    }

    private void redirectToPracticeActivity() {
        Intent intent = new Intent(getActivity(), PracticeNihongoRaceActivity.class);
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Disable back button handling when the fragment is visible
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent back navigation
                // Optionally show a message or toast if you want
                Toast.makeText(getActivity(), "Use the logout option to exit", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
