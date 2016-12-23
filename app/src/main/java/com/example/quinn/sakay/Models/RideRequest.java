package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 23/12/2016.
 */

@IgnoreExtraProperties
public class RideRequest {
    public String uid;
    public String author;
    public String facebookId;
    public String start;
    public String destination;
    public String dateAndTime;
    public Map<String, Boolean> stars = new HashMap<>();

    public RideRequest(){}

    public RideRequest(String uid, String author, String facebookId, String start, String destination,
                       String dateAndTime) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.start = start;
        this.destination = destination;
        this.dateAndTime = dateAndTime;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("facebookId", facebookId);
        result.put("start", start);
        result.put("destination", destination);
        result.put("dateAndTime", dateAndTime);

        return result;
    }
}


