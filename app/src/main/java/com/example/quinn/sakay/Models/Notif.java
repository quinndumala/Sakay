package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 24/01/2017.
 */

public class Notif {
    public String userId;
    public String userName;
    public String facebookId;
    public String type;
    public String postKey;
    public String action;
    public Boolean read;
    public Object timestamp;


    public Notif() {}

    public Notif(String userId, String userName, String facebookId, String type, String postKey,
                 String action, Boolean read, Object timestamp){
        this.userId = userId;
        this.userName = userName;
        this.facebookId = facebookId;
        this.type = type;
        this.postKey = postKey;
        this.action = action;
        this.read = read;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("facebookId", facebookId);
        result.put("type", type);
        result.put("postKey", postKey);
        result.put("action", action);
        result.put("read", read);
        result.put("timestamp", timestamp);
        return result;
    }

    public String getType(){return type;}
    public String getPostKey(){return postKey;}
    public Boolean getRead(){return read;}
}
