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
    public Double startLat;
    public Double startLong;
    public String destination;
    public Double destinationLat;
    public Double destinationLong;
    public String dateAndTime;
    public Long timeStamp;
    public Boolean available;
    public String accepted;
    public Map<String, Boolean> responses = new HashMap<>();

    public RideRequest(){}

    public RideRequest(String uid, String author, String facebookId, String start, Double startLat, Double startLong,
                       String destination, Double destinationLat, Double destinationLong, String dateAndTime,
                       Long timeStamp, Boolean available, String accepted) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.start = start;
        this.startLat = startLat;
        this.startLong = startLong;
        this.destination = destination;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.dateAndTime = dateAndTime;
        this.timeStamp = timeStamp;
        this.available = available;
        this.accepted = accepted;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("facebookId", facebookId);
        result.put("start", start);
        result.put("startLat", startLat);
        result.put("startLong", startLong);
        result.put("destination", destination);
        result.put("destinationLat", destinationLat);
        result.put("destinationLong", destinationLong);
        result.put("dateAndTime", dateAndTime);
        result.put("timeStamp", timeStamp);
        result.put("available", available);
        result.put("accepted", accepted);
        //result.put("responses", responses);

        return result;
    }
}


