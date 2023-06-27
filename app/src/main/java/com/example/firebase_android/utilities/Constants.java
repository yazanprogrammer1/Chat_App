package com.example.firebase_android.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "usersId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_Message = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "content-type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeader = null;

    public static HashMap<String, String> getRemoteMsgHeader() {
        if (remoteMsgHeader == null) {
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAcyPIUO4:APA91bGJCxnzFEJPpfSfyYOAXh75t0gISRCXL58XpVcsObRYUYzPcnW4jNlHb8ixipi2rJ_x1PPfNsq1SxIuwG-gEVm7TdnMPAP_sRaqGDV216yMWitcrqhEKHHns_jSMcfxDiOiNtlQ"
            );
            remoteMsgHeader.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }


}
