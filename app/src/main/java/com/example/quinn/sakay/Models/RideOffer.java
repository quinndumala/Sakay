package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class RideOffer {
    //public Author author;
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

    public String vehicle;
    public String vehicleModel;
    public String vehicleColor;
    public String vehiclePlateNo;
    public Boolean available;
    public String accepted;
    public Map<String, Boolean> stars = new HashMap<>();

    //public int starCount = 0;

    public RideOffer() {
    }

    public RideOffer(String uid, String author, String facebookId, String start, Double startLat, Double startLong,
                     String destination, Double destinationLat, Double destinationLong, String vehicle,
                     String vehicleModel, String vehicleColor, String vehiclePlateNo,
                     String dateAndTime, Long timeStamp, Boolean available, String accepted) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.start = start;
        this.startLat = startLat;
        this.startLong = startLong;
        this.destination = destination;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.vehicle = vehicle;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehiclePlateNo = vehiclePlateNo;
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
        result.put("vehicle", vehicle);
        result.put("vehicleModel", vehicleModel);
        result.put("vehicleColor", vehicleColor);
        result.put("vehiclePlateNo", vehiclePlateNo);
        result.put("dateAndTime", dateAndTime);
        result.put("timeStamp", timeStamp);
        result.put("available", available);
        result.put("accepted", accepted);


        return result;
    }

    public String getFacebookId(){return facebookId;}

    //public Author getAuthor() {return author;}
}
