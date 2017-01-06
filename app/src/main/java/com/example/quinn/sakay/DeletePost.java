package com.example.quinn.sakay;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by Quinn on 05/01/2017.
 */

public class DeletePost {
    public DeletePost(){}

    public static void DeleteThis(DatabaseReference postRef, DatabaseReference userPostRef){
        postRef.setValue(null);
        postRef.setValue(null);
    }

}
