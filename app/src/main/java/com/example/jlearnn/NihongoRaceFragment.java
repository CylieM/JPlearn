package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NihongoRaceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nihongo_race, container, false);
    }

    private void getUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    // Now you have the user's role, proceed with handling visibility
                    handleCreateRoomButtonVisibility(userRole);
                } else {
                    // Handle the case where user data doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors (e.g., database read failure)
            }
        });
    }

    private void handleCreateRoomButtonVisibility(String userRole) {
        Button createRoomButton = getView().findViewById(R.id.btnCreateRoom);

        if ("Teacher".equals(userRole)) {
            // User is a teacher, make the button visible
            createRoomButton.setVisibility(View.VISIBLE);
            Log.d("NihongoRaceFragment", "User is a teacher. Button visible.");
        } else {
            // User is not a teacher, hide the button
            createRoomButton.setVisibility(View.GONE);
            Log.d("NihongoRaceFragment", "User is not a teacher. Button hidden.");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button playButton = view.findViewById(R.id.btnPlay);
        Button joinRoomButton = view.findViewById(R.id.btnJoinRoom);
        Button createRoomButton = view.findViewById(R.id.btnCreateRoom);
        Button practiceButton = view.findViewById(R.id.btnPractice);
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                Intent intent = new Intent(getActivity(), JoinRoomActivity.class);
                startActivity(intent);
            }
        });

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GameRoomActivity.class);
                startActivity(intent);
            }
        });

        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PracticeNihongoRaceActivity.class);
                startActivity(intent);
            }
        });
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String newLobbyId = lobbyRef.push().getKey();
        if (newLobbyId != null) {
            lobbyRef.child(newLobbyId).child("creator").setValue(userId);
            Intent intent = new Intent(getActivity(), StartGameRoomActivity.class);
            intent.putExtra("LOBBY_ID", newLobbyId);
            startActivity(intent);
        }
    }
}

