package com.example.firebase_android.Activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.firebase_android.Adapter.RecentConversionAdapter;
import com.example.firebase_android.databinding.ActivityMainBinding;
import com.example.firebase_android.listeners.ConversionListener;
import com.example.firebase_android.model.ChatMessage;
import com.example.firebase_android.model.User;
import com.example.firebase_android.utilities.Constants;
import com.example.firebase_android.utilities.PreferencManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferencManager preferencManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // code App
        preferencManager = new PreferencManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenerConversations();
    }

    private void setListeners() {
        binding.imageOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.fabVideoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RoomActivity.class);
                startActivity(i);
            }
        });
    }


    // get  Conversion Data
    private void init() {
        conversions = new ArrayList<>();
        conversionAdapter = new RecentConversionAdapter(conversions, this);
        binding.conversionRecycler.setAdapter(conversionAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void loadUserDetails() {
        binding.textName.setText(preferencManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferencManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void listenerConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener)
        ;
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferencManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_Message);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversions.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversions.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receiverId)) {
                            conversions.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_Message);
                            conversions.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversions, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversionAdapter.notifyDataSetChanged();
            binding.conversionRecycler.smoothScrollToPosition(0);
            binding.conversionRecycler.setVisibility(View.VISIBLE);
            binding.porgHome.setVisibility(View.GONE);
        }
    });

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferencManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(preferencManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnSuccessListener(unused -> {
            showToast("Token Update Successfully");
        }).addOnFailureListener(error -> {
            showToast("Unable To Update Token");
        });
    }

    private void signOut() {
        showToast("Sign out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(
                preferencManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferencManager.clear();
                    startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                    finish();
                })
                .addOnFailureListener(error -> {
                    showToast("Unable to sign out");
                });
    }

    @Override
    public void onConversionClicked(User user) {
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra(Constants.KEY_USER, user);
        startActivity(i);
    }
}