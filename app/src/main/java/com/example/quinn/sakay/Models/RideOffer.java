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
    public String destination;
    public String dateAndTime;
    public String vehicle;
    public String vehicleModel;
    public String vehicleColor;
    public String vehiclePlateNo;
    public Map<String, Boolean> stars = new HashMap<>();

    //public int starCount = 0;

    public RideOffer() {
    }

    public RideOffer(String uid, String author, String facebookId, String start, String destination,
                     String vehicle, String vehicleModel, String vehicleColor, String vehiclePlateNo,
                     String dateAndTime) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.start = start;
        this.destination = destination;
        this.vehicle = vehicle;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehiclePlateNo = vehiclePlateNo;
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
        result.put("vehicle", vehicle);
        result.put("vehicleModel", vehicleModel);
        result.put("vehicleColor", vehicleColor);
        result.put("vehiclePlateNo", vehiclePlateNo);
        result.put("dateAndTime", dateAndTime);

        return result;
    }

    public String getFacebookId(){return facebookId;}

    //public Author getAuthor() {return author;}
}
