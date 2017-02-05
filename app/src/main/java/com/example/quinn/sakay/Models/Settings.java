package com.example.quinn.sakay.Models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Quinn on 05/02/2017.
 */
@IgnoreExtraProperties
public class Settings {
    public String home;
    public Double homeLat;
    public Double homeLong;
    public String work;
    public Double workLat;
    public Double workLong;
    public Vehicle vehicle;
    public String phone;

    public Settings(){}

    public Settings(String home, Double homeLat, Double homeLong, String work, Double workLat, Double workLong,
                    Vehicle vehicle, String phone){
        this.home = home;
        this.homeLat = homeLat;
        this.homeLong = homeLong;
        this.work = work;
        this.workLat = workLat;
        this.workLong = workLong;
        this.vehicle = vehicle;
        this.phone = phone;
    }

}
