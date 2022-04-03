package com.example.quinn.sakay;

/**
 * Created by Quinn on 08/12/2016.
 */

public class User {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String facebookId;


    public User() {
    }

    public User(String id, String name, String phoneNumber, String email, String facebookId) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.facebookId = facebookId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookId(){return facebookId; }

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
}
