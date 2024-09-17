package com.example.JapLearn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "UserAdapter";

    private Context context;
    private ArrayList<RegistrationActivity.User> userList;
    private ArrayList<RegistrationActivity.User> fullUserList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RegistrationActivity.User user);
    }

    public UserAdapter(Context context, ArrayList<RegistrationActivity.User> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.fullUserList = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        RegistrationActivity.User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());

        // Load the user's profile picture into the ShapeableImageView
        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePicture())
                .placeholder(R.drawable.ic_person_24)
                .into(holder.profileImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, emailTextView;
        ShapeableImageView profileImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            emailTextView = itemView.findViewById(R.id.email);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }
    }

    public void searchDataList(ArrayList<RegistrationActivity.User> searchList) {
        userList.clear();
        userList.addAll(searchList);
        notifyDataSetChanged();
    }
}