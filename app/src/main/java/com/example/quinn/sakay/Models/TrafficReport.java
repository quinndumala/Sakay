package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 29/01/2017.
 */

public class TrafficReport {
    public Double latitude;
    public Double longitude;
    public String intensity;
    public Object timestamp;

    public TrafficReport(){}

    public TrafficReport(Double latitude, Double longitude, String intensity, Object timestamp){
        this.latitude = latitude;
        this.longitude = longitude;
        this.intensity = intensity;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("intensity", intensity);
        result.put("timestamp", timestamp);
        return result;
    }
}
