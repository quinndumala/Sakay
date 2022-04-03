package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 30/12/2016.
 */

public class Sakay {
    public String uid;
    public String author;
    public String facebookId;
    public String role;
    public String start;
    public Double startLat;
    public Double startLong;
    public String destination;
    public Double destinationLat;
    public Double destinationLong;
    public String dateAndTime;
    public Long timeStamp;
    public String vehicle;
    public String vehicleModel;
    public String vehicleColor;
    public String vehiclePlateNo;
    public String otherUid;
    public String otherAuthor;
    public String otherFacbookId;

    public Sakay(){}

    public Sakay(String uid, String author, String facebookId, String role,
                 String start, Double startLat, Double startLong,
                 String destination, Double destinationLat, Double destinationLong,
                 String dateAndTime, Long timeStamp,
                 String vehicle, String vehicleModel, String vehicleColor, String vehiclePlateNo,
                 String otherUid, String otherAuthor, String otherFacbookId){
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
        this.vehicle = vehicle;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehiclePlateNo = vehiclePlateNo;
        this.role = role;
        this.otherUid = otherUid;
        this.otherAuthor = otherAuthor;
        this.otherFacbookId = otherFacbookId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("facebookId", facebookId);
        result.put("role", role);
        result.put("start", start);
        result.put("startLat", startLat);
        result.put("startLong", startLong);
        result.put("destination", destination);
        result.put("destinationLat", destinationLat);
        result.put("destinationLong", destinationLong);
        result.put("dateAndTime", dateAndTime);
        result.put("timeStamp", timeStamp);
        result.put("vehicle", vehicle);
        result.put("vehicleModel", vehicleModel);
        result.put("vehicleColor", vehicleColor);
        result.put("vehiclePlateNo", vehiclePlateNo);
        result.put("otherUid", otherUid);
        result.put("otherAuthor", otherAuthor);
        result.put("otherFacebookId", otherFacbookId);

        return result;
    }

}
