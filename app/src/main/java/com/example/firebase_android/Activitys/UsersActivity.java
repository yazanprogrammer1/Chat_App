package com.example.firebase_android.Activitys;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.firebase_android.Adapter.UserAdapter;
import com.example.firebase_android.databinding.ActivityUsersBinding;
import com.example.firebase_android.listeners.UserListener;
import com.example.firebase_android.model.User;
import com.example.firebase_android.utilities.Constants;
import com.example.firebase_android.utilities.PreferencManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferencManager preferencManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //... code users
        preferencManager = new PreferencManager(getApplicationContext());
        setListener();
        getUsers();
    }


    private void setListener() {
        binding.imageOut.setOnClickListener(v -> onBackPressed());
    }

    private void showErrorMessage() {
        binding.txtError.setText(String.format("%s", "No User Available"));
        binding.txtError.setVisibility(View.VISIBLE);
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferencManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (currentUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.recyclerUsers.setAdapter(userAdapter);
                            binding.recyclerUsers.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}