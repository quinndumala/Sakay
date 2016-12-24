package com.example.quinn.sakay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quinn.sakay.Models.RideRequest;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplicityapks.reminderdatepicker.lib.OnDateSelectedListener;
import com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.facebook.Profile.getCurrentProfile;

public class AddRideRequest extends BaseActivity
    implements ConnectivityReceiver.ConnectivityReceiverListener,
    View.OnClickListener{

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText fStart;
    private EditText fDestination;
    private ReminderDatePicker datePicker;
    public String dateAndTime = "";
    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        datePicker = (ReminderDatePicker) findViewById(R.id.request_date_picker);
        datePicker.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar date) {
                String selectedDate = getDateFormat().format(date.getTime());
                dateAndTime = selectedDate;
            }
        });

        checkConnection();

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        fStart = (EditText) findViewById(R.id.field_request_start);
        fDestination = (EditText) findViewById(R.id.field_request_destination);
        userFacebookId = profile.getId();

        fStart.setText("Your current location");
        fDestination.requestFocus();

        findViewById(R.id.field_request_start).setOnClickListener(this);
    }

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

    private void submitPost() {
        final String start = fStart.getText().toString();
        final String destination = fDestination.getText().toString();

        if (TextUtils.isEmpty(start)) {
            fStart.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(destination)) {
            fDestination.setError(REQUIRED);
            return;
        }

        //setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(AddRideRequest.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.getName(), userFacebookId, start, destination,
                                    dateAndTime);
                        }

                        // Finish this Activity, back to the stream
                        //setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        fStart.setEnabled(enabled);
        fDestination.setEnabled(enabled);
//        if (enabled) {
//            mSubmitButton.setVisibility(View.VISIBLE);
//        } else {
//            mSubmitButton.setVisibility(View.GONE);
//        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String userFacebookId, String start,
                              String destination, String dateAndTime) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        RideRequest rideRequest = new RideRequest(userId, username, userFacebookId, start,
                destination, dateAndTime);
        Map<String, Object> postValues = rideRequest.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/rideRequests/" + key, postValues);
        childUpdates.put("/user-rideRequests/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_ride_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add_ride) {
            submitPost();
        } else if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color = Color.WHITE;
        if (!(isConnected)) {
            message = "No connection";
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.add_ride_request_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.field_request_start:
                Intent searchAddressIntent = new Intent(this, SearchAddressActivity.class);
                startActivity(searchAddressIntent);
                break;
        }
    }
}
