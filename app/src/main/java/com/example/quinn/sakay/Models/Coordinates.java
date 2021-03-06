package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 29/01/2017.
 */

public class Coordinates {
    public Double latitude;
    public Double longitude;
    public Object timestamp;

    public Coordinates(){}

    public Coordinates(Double latitude, Double longitude, Object timestamp){
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("timestamp", timestamp);
        return result;
    }
}
