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
    public String destination;
    public String dateAndTime;
    public String vehicle;
    public String otherUid;
    public String otherAuthor;
    public String otherFacbookId;

    public Sakay(){}

    public Sakay(String uid, String author, String facebookId, String role, String start, String destination,
                 String dateAndTime, String vehicle, String otherUid, String otherAuthor,
                 String otherFacbookId){
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.start = start;
        this.destination = destination;
        this.dateAndTime = dateAndTime;
        this.vehicle = vehicle;
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
        result.put("destination", destination);
        result.put("dateAndTime", dateAndTime);
        result.put("vehicle", vehicle);
        result.put("otherUid", otherUid);
        result.put("otherAuthor", otherAuthor);
        result.put("otherFacebookId", otherFacbookId);

        return result;
    }

}
