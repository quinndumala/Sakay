package com.example.quinn.sakay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.RideRequest;
import com.facebook.Profile;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
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
    private static final int REQUEST_PLACE_PICKER = 1;

    private DatabaseReference mDatabase;
    private DatabaseReference userSettingsRef;
    private DatabaseReference homeAddressRef;
    private DatabaseReference homeLatRef;
    private DatabaseReference homeLongRef;
    private DatabaseReference workAddressRef;
    private DatabaseReference workLatRef;
    private DatabaseReference workLongRef;

    final String userId = getUid();
    private EditText fStart;
    private EditText fDestination;
    private ReminderDatePicker datePicker;
    public String dateAndTime = "";
    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();
    public String startOrDestination = "";

    public Double startLat;
    public Double startLong;
    public Double destinationLat;
    public Double destinationLong;
    public Long time;

    public MaterialDialog progressDialog;

    private String currentHome;
    private Double currentHomeLat;
    private Double currentHomeLong;
    private String currentWork;
    private Double currentWorkLat;
    private Double currentWorkLong;

    public Boolean workSet = false;
    public Boolean homeSet = false;
    public Boolean isTimeValid = true;

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
                time = date.getTimeInMillis();
                if (time < System.currentTimeMillis()){
                    launchTimePassedDialog();
                    isTimeValid = false;
                } else {
                    isTimeValid = true;
                }
            }
        });


        mDatabase = FirebaseDatabase.getInstance().getReference();
        userSettingsRef = mDatabase.child("users-settings").child(userId);
        homeAddressRef = userSettingsRef.child("home");
        homeLatRef = userSettingsRef.child("homeLat");
        homeLongRef = userSettingsRef.child("homeLong");
        workAddressRef = userSettingsRef.child("work");
        workLatRef = userSettingsRef.child("workLat");
        workLongRef = userSettingsRef.child("workLong");

        fStart = (EditText) findViewById(R.id.field_request_start);
        fDestination = (EditText) findViewById(R.id.field_request_destination);
        userFacebookId = profile.getId();

        fStart.setText(R.string.select_location);
        fDestination.setText(R.string.select_location);

        findViewById(R.id.field_request_start).setOnClickListener(this);
        findViewById(R.id.field_request_destination).setOnClickListener(this);

        checkAddress();

    }

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkConnection();

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading map")
                .content("Please wait")
                .progress(true, 0)
                .build();
    }

    public void checkAddress(){
        homeAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    homeSet = true;
                    currentHome = dataSnapshot.getValue(String.class);
                } else {
                    homeSet = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        homeLatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentHomeLat = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        homeLongRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentHomeLong = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        workAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    workSet = true;
                    currentWork = dataSnapshot.getValue(String.class);
                } else {
                    workSet = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        workLatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentWorkLat = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        workLongRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentWorkLong = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void submitPost() {
        final String start = fStart.getText().toString();
        final String destination = fDestination.getText().toString();

        if (start.equals("Select Location") || destination.equals("Select Location")){
            selectLocationAlert();
            return;
        } else if(!isTimeValid) {
            launchTimePassedDialog();
            return;
        } else {
            confirmPost(start, destination);
        }

    }


    public void confirmPost(final String start, final String destination){
        new MaterialDialog.Builder(this)
                .title("Post this ride request?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postPost(start, destination);
                    }
                })
                .show();
    }

    private void postPost(final String start, final String destination){
        //setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]

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
                            writeNewPost(userId, user.getName(), userFacebookId, start, startLat, startLong,
                                    destination, destinationLat, destinationLong, dateAndTime, time);
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
    private void writeNewPost(String userId, String username, String userFacebookId, String start, Double startLat,
                              Double startLong, String destination, Double destinationLat, Double destinationLong,
                              String dateAndTime, Long timeStamp) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("rideRequests").push().getKey();
        RideRequest rideRequest = new RideRequest(userId, username, userFacebookId, start, startLat, startLong,
                destination, destinationLat, destinationLong, dateAndTime, timeStamp, true, "none");
        Map<String, Object> postValues = rideRequest.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/rideRequests/" + key, postValues);
        childUpdates.put("/user-rideRequests/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String[] arrRef;
        if (homeSet && workSet){
            arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_all);
        } else if (homeSet) {
            arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_home);
        } else if (workSet) {
            arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_work);
        } else {
            arrRef = getResources().getStringArray(R.array.offer_select_pickup_location);
        }

        switch (id){
            case R.id.field_request_start:
                startOrDestination = "start";
                pickPlace(arrRef);
                break;
            case R.id.field_request_destination:
                startOrDestination = "destination";
                pickPlace(arrRef);
                break;
        }
    }

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

    public void pickPlace(String[] arrRef){
        String dialogTitle;
        if (startOrDestination.equals("start")){
            dialogTitle = "Choose starting location";
        } else {
            dialogTitle = "Choose destination";
        }

        new MaterialDialog.Builder(this)
                .title(dialogTitle)
                .items(arrRef)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if(text.equals("Choose on Map")){
                            progressDialog.show();
                            launchPlacePicker();
                        } else if(text.equals("My Home Address")){
                            if(startOrDestination.equals("start")){
                                fStart.setText(currentHome);
                                startLat = currentHomeLat;
                                startLong = currentHomeLong;

                            } else if(startOrDestination.equals("destination")){
                                fDestination.setText(currentHome);
                                destinationLat = currentHomeLat;
                                destinationLong = currentHomeLong;
                            }

                        } else if(text.equals("My Work Address")){
                            if(startOrDestination.equals("start")){
                                fStart.setText(currentWork);
                                startLat = currentWorkLat;
                                startLong = currentWorkLong;

                            } else if(startOrDestination.equals("destination")){
                                fDestination.setText(currentWork);
                                destinationLat = currentWorkLat;
                                destinationLong = currentWorkLong;
                            }
                        }
                    }
                })
                .positiveText("cancel")
                .show();
    }

    public void launchPlacePicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);

            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 550);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, this);

                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                final LatLng placeLatLong = place.getLatLng();

                String attribution = PlacePicker.getAttributions(data);
                if(attribution == null){
                    attribution = "";
                }

                String location = name.toString();
                if (location.contains("°")){
                    location = address.toString();
                    launchAlertDialog();
                }

                if(startOrDestination == "start"){
                    fStart.setText(location);
                    startLat = placeLatLong.latitude;
                    startLong = placeLatLong.longitude;
                } else if(startOrDestination == "destination"){
                    fDestination.setText(location);
                    destinationLat = placeLatLong.latitude;
                    destinationLong = placeLatLong.longitude;
                }

//                // Update data on card.
//                getCardStream().getCard(CARD_DETAIL)
//                        .setTitle(name.toString())
//                        .setDescription(getString(R.string.detail_text, placeId, address, phone,
//                                attribution));

                Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");

            } else {
                // User has not selected a place, hide the card.
                //fStart.setText(R.string.select_location);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void launchAlertDialog(){
        MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                .title(R.string.ambiguous_location_title)
                .content(R.string.ambiguous_location_body)
                .positiveText("OK")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchPlacePicker();
                    }
                })
                .show();
    }

    public void selectLocationAlert(){
        new MaterialDialog.Builder(this)
                .content("Missing some information")
                .positiveText("OK")
                .cancelable(false)
                .show();
    }

    public void launchTimePassedDialog(){
        new MaterialDialog.Builder(this)
                .title("Warning")
                .content("Ride schedule can't be set to the past!")
                .positiveText("OK")
                .cancelable(false)
                .show();
    }
}
