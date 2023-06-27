package com.example.firebase_android.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.firebase_android.R;
import com.example.firebase_android.utilities.App_Constant;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

public class MeetingActivity extends AppCompatActivity {


    TextView meeting_id;
    String mID, name, userid;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        //.... code meeting
        meeting_id = findViewById(R.id.text_id);
        mID = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        userid = getIntent().getStringExtra("userID");
        meeting_id.setText("Meeting ID :" + mID);


        addFragment();
    }

    public void addFragment() {
        long appID = App_Constant.appId;
        String appSign = App_Constant.appSign;

        String conferenceID = mID;
        String userID = userid;
        String userName = name;

        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
        config.turnOnMicrophoneWhenJoining = false;
        config.turnOnCameraWhenJoining = false;
        ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(appID, appSign, userID, userName, conferenceID, config);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contanir, fragment)
                .commitNow();
    }

}