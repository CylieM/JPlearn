package com.example.jlearnn;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserModel {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    public UserModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public DatabaseReference getUserRef(String userId) {
        return userRef.child(userId);
    }
}
