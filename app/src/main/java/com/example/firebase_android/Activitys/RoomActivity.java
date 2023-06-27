package com.example.firebase_android.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.firebase_android.R;
import com.example.firebase_android.databinding.ActivityRoomBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;
import java.util.UUID;

public class RoomActivity extends AppCompatActivity {


    EditText id, name;
    AppCompatButton join, create;
    ActivityRoomBinding binding;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //... code
        id = binding.inputIdMeeting;
        name = binding.inputNamePerson;
        join = binding.btnJoinMeeting;
        create = binding.btnCreateNewMeeting;

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String meeting_id = id.getText().toString();
                if (meeting_id.length() != 10) {
                    id.setError("Invalid");
                    id.requestFocus();
                    return;
                }
                String meeting_name = name.getText().toString();
                if (meeting_name.isEmpty()) {
                    name.setError("Invalid");
                    name.requestFocus();
                    return;
                }
                startMeeting(meeting_id, meeting_name);
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String meeting_name = name.getText().toString();
                if (meeting_name.isEmpty()) {
                    name.setError("Invalid");
                    name.requestFocus();
                    return;
                }
                startMeeting(getRandomMeetingID(), meeting_name);
            }
        });
    }

    public void startMeeting(String id, String name) {
        String user_id = UUID.randomUUID().toString();
        Intent i = new Intent(this, MeetingActivity.class);
        i.putExtra("id", id);
        i.putExtra("name", name);
        i.putExtra("userID", user_id);
        startActivity(i);
    }

    public String getRandomMeetingID() {
        StringBuilder id_ = new StringBuilder();
        while (id_.length() != 10) {
            int random = new Random().nextInt(10);
            id_.append(random);
        }
        return id_.toString();
    }
}