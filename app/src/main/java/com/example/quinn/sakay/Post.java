package com.example.quinn.sakay;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {
    public String uid;
    public String author;
    public String start;
    public String destination;
    public String date;
    public String time;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String start, String destination, String date, String time) {
        this.uid = uid;
        this.author = author;
        this.start = start;
        this.destination = destination;
        this.date = date;
        this.time = time;
    }


    // [END post_to_map]// [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("start", start);
        result.put("destination", destination);
        result.put("date", date);
        result.put("time", time);

        return result;
    }
    public String getAuthor() {
        return author;
    }

    public String getUid(){
        return uid;
    }
}
