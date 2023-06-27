package com.example.firebase_android.Activitys;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.firebase_android.Adapter.ChatAdapter;
import com.example.firebase_android.databinding.ActivityChatBinding;
import com.example.firebase_android.model.ChatMessage;
import com.example.firebase_android.model.User;
import com.example.firebase_android.netWork.ApiClient;
import com.example.firebase_android.netWork.ApiService;
import com.example.firebase_android.utilities.Constants;
import com.example.firebase_android.utilities.PreferencManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferencManager preferencManager;
    private FirebaseFirestore db;
    private String conversionId = null;

    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ///.... code chat
        setListener();
        loadingReceiverDetails();
        init();
        listenMessages();
    }

    private void loadingReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void init() {
        preferencManager = new PreferencManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getBitmapFromEncodedString(receiverUser.image), preferencManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycler.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject jsonObject = new JSONObject(response.body());
                            JSONArray results = jsonObject.getJSONArray("results");
                            if (jsonObject.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification send successfully");
                    Log.d("yazan", "Notification send successfully");
                } else {
                    Log.e("yazan", "error " + response.message());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenerAvailabilityOfReceiver() {
        db.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
            }
            if (isReceiverAvailable) {
                binding.txtAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.txtAvailability.setVisibility(View.GONE);
            }
        }));
    }

    private void listenMessages() {
        db.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferencManager.getString(Constants.KEY_USER_ID)).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id).addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id).whereEqualTo(Constants.KEY_RECEIVER_ID, preferencManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dataTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };

    private void sendMessage() {
        if (!binding.inputMessage.getText().toString().isEmpty()) {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferencManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if (conversionId != null) {
                updateConversion(binding.inputMessage.getText().toString());
            } else {
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferencManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferencManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferencManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_Message, binding.inputMessage.getText().toString());
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
            if (!isReceiverAvailable) {
                try {
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);
                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_USER_ID, preferencManager.getString(Constants.KEY_USER_ID));
                    data.put(Constants.KEY_NAME, preferencManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferencManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                    sendNotification(body.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            binding.inputMessage.setText(null);
        } else {
            Toast.makeText(getApplicationContext(), "Typ Message", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void addConversion(HashMap<String, Object> conversion) {
        db.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> {
                    conversionId = documentReference.getId();
                });
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_Message, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferencManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferencManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        db.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(completeListener);
    }

    private final OnCompleteListener<QuerySnapshot> completeListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenerAvailabilityOfReceiver();
    }

}