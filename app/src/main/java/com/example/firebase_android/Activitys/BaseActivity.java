package com.example.firebase_android.Activitys;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase_android.utilities.Constants;
import com.example.firebase_android.utilities.PreferencManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {


    DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencManager preferencManager = new PreferencManager(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferencManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY, 1);
    }
}
