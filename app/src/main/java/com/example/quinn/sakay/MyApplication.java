package com.example.quinn.sakay;

/**
 * Created by Quinn on 04/12/2016.
 */

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.Firebase;
import com.google.firebase.database.FirebaseDatabase;


public class MyApplication extends Application {

    private static MyApplication mInstance;
    public static FirebaseDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        //FirebaseDatabase.setAndroidContext(this);
        Firebase.setAndroidContext(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}
