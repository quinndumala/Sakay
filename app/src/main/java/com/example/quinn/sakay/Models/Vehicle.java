package com.example.quinn.sakay.Models;

/**
 * Created by Quinn on 11/01/2017.
 */

public class Vehicle {
    public String vehicleType;
    public String vehicleModel;
    public String vehicleColor;
    public String plateNo;

    public Vehicle(){}

    public Vehicle(String vehicleType, String vehicleModel, String vehicleColor, String plateNo){
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.plateNo = plateNo;
    }
}
