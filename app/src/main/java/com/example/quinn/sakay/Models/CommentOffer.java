/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.example.quinn.sakay.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START comment_class]
@IgnoreExtraProperties
public class CommentOffer {

    public String uid;
    public String author;
    public String facebookId;
    public String pickUp;
    public Double pickUpLat;
    public Double pickUpLong;

    public CommentOffer() {
        // Default constructor required for calls to DataSnapshot.getValue(CommentOffer.class)
    }

    public CommentOffer(String uid, String author, String facebookId, String pickUp,
                        Double pickUpLat, Double pickUpLong) {
        this.uid = uid;
        this.author = author;
        this.facebookId = facebookId;
        this.pickUp = pickUp;
        this.pickUpLat = pickUpLat;
        this.pickUpLong = pickUpLong;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("facebookId", facebookId);
        result.put("pickUp", pickUp);
        result.put("pickUpLat", pickUpLat);
        result.put("pickUpLong", pickUpLong);

        return result;
    }

}
// [END comment_class]