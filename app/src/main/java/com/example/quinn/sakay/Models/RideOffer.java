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
    public String profilePicture;
    public String start;
    public String destination;
    public String dateAndTime;
    public String vehicle;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public RideOffer() {
    }

//    public RideOffer(String uid, String author, String profilePicture, String start, String destination,
//                     String dateAndTime, String vehicle){
//        this.uid = uid;
//        this.author = author;
//        this.profilePicture = profilePicture;
//        this.start = start;
//        this.destination = destination;
//        this.dateAndTime = dateAndTime;
//        this.vehicle = vehicle;
//    }

    public RideOffer(String uid, String author, String profilePicture, String start, String destination,
                     String vehicle, String dateAndTime) {
        this.uid = uid;
        this.author = author;
        this.start = start;
        this.destination = destination;
        this.profilePicture = profilePicture;
        this.vehicle = vehicle;
        this.dateAndTime = dateAndTime;
    }

    // [END post_to_map]// [START post_to_map]
//    @Exclude
//    public Map<String, Object> toMap() {
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("uid", uid);
//        result.put("author", author);
//        result.put("profilePicture", profilePicture);
//        result.put("start", start);
//        result.put("destination", destination);
//        result.put("dateAndTime", dateAndTime);
//        result.put("vehicle", vehicle);
//
//        return result;
//    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("profilePicture", profilePicture);
        result.put("start", start);
        result.put("destination", destination);
        result.put("vehicle", vehicle);
        result.put("dateAndTime", dateAndTime);

        return result;
    }

    //public Author getAuthor() {return author;}
}
