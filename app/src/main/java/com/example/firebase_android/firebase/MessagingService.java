package com.example.firebase_android.firebase;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("yazan", "Token " + token);
        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("yazan", "Message: " + message.getNotification().getBody());
        Toast.makeText(this, message.getNotification().getBody(), Toast.LENGTH_SHORT).show();
    }
}
