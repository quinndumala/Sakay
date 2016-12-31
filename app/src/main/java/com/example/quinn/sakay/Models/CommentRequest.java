package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 30/12/2016.
 */

public class CommentRequest {
    public String uid;
    public String author;
    public String facebookId;
    public String vehicle;

    public CommentRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public CommentRequest(String uid, String author, String facebookId, String vehicle) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.vehicle = vehicle;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("facebookId", facebookId);
        result.put("vehicle", vehicle);

        return result;
    }
}
