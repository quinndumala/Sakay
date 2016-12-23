package com.example.quinn.sakay;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quinn.sakay.Models.RideOffer;
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

public class AddRideOffer extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener,
        ConnectivityReceiver.ConnectivityReceiverListener{


    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText fStart;
    private EditText fDestination;
    private ReminderDatePicker datePicker;
    public String dateAndTime = "";
    private EditText fVehicle;
    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride_offer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        datePicker = (ReminderDatePicker) findViewById(R.id.offer_date_picker);
        datePicker.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar date) {
                String selectedDate = getDateFormat().format(date.getTime());
                dateAndTime = selectedDate;
                //Toast.makeText(AddRideOffer.this, "Selected date: "+ selectedDate, Toast.LENGTH_SHORT).show();
                //Toast.makeText(AddRideOffer.this, "Selected date: "+ getDateFormat().format(date.getTime()), Toast.LENGTH_SHORT).show();
            }
        });

        checkConnection();

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        fStart = (EditText) findViewById(R.id.field_offer_start);
        fDestination = (EditText) findViewById(R.id.field_offer_destination);
        fVehicle = (EditText) findViewById(R.id.field_offer_vehicle);
        userFacebookId = profile.getId();

    }

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

//    @Override
//    public void onBackPressed() {
//
//    }

    private void submitPost() {
        final String start = fStart.getText().toString();
        final String destination = fDestination.getText().toString();
        final String vehicle = fVehicle.getText().toString();
//        final String dateTIme = dateAndTime;

        // Title is required
        if (TextUtils.isEmpty(start)) {
            fStart.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(destination)) {
            fDestination.setError(REQUIRED);
            return;
        }

        // Vehicle
        if (TextUtils.isEmpty(vehicle)) {
            fVehicle.setError(REQUIRED);
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
                            Toast.makeText(AddRideOffer.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.getName(), userFacebookId, start, destination,
                                    vehicle, dateAndTime);
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
        fVehicle.setEnabled(enabled);

//        if (enabled) {
//            mSubmitButton.setVisibility(View.VISIBLE);
//        } else {
//            mSubmitButton.setVisibility(View.GONE);
//        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String userFacebookId, String start,
                              String destination, String vehicle, String dateAndTime) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        RideOffer rideOffer = new RideOffer(userId, username, userFacebookId, start,
                destination, vehicle, dateAndTime);
        Map<String, Object> postValues = rideOffer.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/rideOffers/" + key, postValues);
        childUpdates.put("/user-rideOffers/" + userId + "/" + key, postValues);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_add_ride) {
//            // Create a new child with a auto-generated ID.
//            DatabaseReference childRef = myRef.push();
//
//            // Set the child's data to the value passed in from the text box.
//            childRef.setValue(text.getText().toString());
//            Toast.makeText(getApplicationContext(), "Ride Offer Added", Toast.LENGTH_SHORT).show();
//            finish();
            submitPost();
        } else if (item.getItemId() == android.R.id.home){
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

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
                    .make(findViewById(R.id.add_ride_offer_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }
}
