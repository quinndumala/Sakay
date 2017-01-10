package com.example.quinn.sakay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.RideOffer;
import com.facebook.Profile;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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
        ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener{


    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final int REQUEST_PLACE_PICKER = 1;

    private DatabaseReference mDatabase;
    private EditText fStart;
    private EditText fDestination;
    private ReminderDatePicker datePicker;
    public String dateAndTime = "";
    private EditText fVehicle;
    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();
    public String startOrDestination = "";

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
            }
        });

        checkConnection();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fStart = (EditText) findViewById(R.id.field_offer_start);
        fDestination = (EditText) findViewById(R.id.field_offer_destination);
        fVehicle = (EditText) findViewById(R.id.field_offer_vehicle);
        userFacebookId = profile.getId();

        fStart.setText(R.string.select_location);
        fDestination.setText(R.string.select_location);
        fVehicle.requestFocus();

        findViewById(R.id.field_offer_start).setOnClickListener(this);
        findViewById(R.id.field_offer_destination).setOnClickListener(this);

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

        if (start.equals("Select Location")){
            selectLocationAlert();
            return;
        }

        if (destination.equals("Select Location")){
            selectLocationAlert();
            return;
        }

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
                }
        );
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
        String key = mDatabase.child("rideOffers").push().getKey();
        RideOffer rideOffer = new RideOffer(userId, username, userFacebookId, start,
                destination, vehicle, dateAndTime);
        Map<String, Object> postValues = rideOffer.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/rideOffers/" + key, postValues);
        childUpdates.put("/user-rideOffers/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.field_offer_start:
                startOrDestination = "start";
                launchPlacePicker();
                break;
            case R.id.field_offer_destination:
                startOrDestination = "destination";
                launchPlacePicker();
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
        } else if (id == android.R.id.home){
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

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
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
                } else if(startOrDestination == "destination"){
                    fDestination.setText(location);
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

}
