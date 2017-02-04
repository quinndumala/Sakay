package com.example.quinn.sakay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.Sakay;
import com.facebook.Profile;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.facebook.Profile.getCurrentProfile;

public class SakayDetailActivity extends BaseActivity implements
        View.OnClickListener{

    private static final String TAG = "SakayDetail";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mRootRef;
    private DatabaseReference mPostReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private String otherUser;

    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();
    private final String userId = getUid();
    private String otherUserId;

    private TextView otherAuthorNameView;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;

    private TextView vehicleTypeView;
    private TextView vehicleModelView;
    private TextView vehicleColorView;
    private TextView vehiclePlateNoView;

    public MaterialDialog loadingDialog;

//    public String driverText;
//    public String riderText;

    private FloatingActionButton trackLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sakay_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mPostReference = mRootRef.child("user-sakays").child(userId).child(mPostKey);

        otherAuthorNameView = (TextView) findViewById(R.id.sakay_detail_other_author);
        dateAndTimeView = (TextView) findViewById(R.id.sakay_detail_date_and_time);
        startView = (TextView) findViewById(R.id.sakay_detail_start);
        destinationView = (TextView) findViewById(R.id.sakay_detail_destination);

        vehicleTypeView = (TextView) findViewById(R.id.sakay_detail_vehicle_type);
        vehicleModelView = (TextView) findViewById(R.id.sakay_detail_vehicle_model);
        vehicleColorView = (TextView) findViewById(R.id.sakay_detail_vehicle_color) ;
        vehiclePlateNoView = (TextView) findViewById(R.id.sakay_detail_vehicle_plate_no);

        loadingDialog = new MaterialDialog.Builder(this)
                .title("Loading details")
                .content("Please wait")
                .progress(true, 0)
                .show();

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Sakay sakay = dataSnapshot.getValue(Sakay.class);

                String sakayWith;
                if(sakay.role.equals("driver")) {
                    sakayWith = "You will be driving for " + sakay.otherAuthor;
                } else {
                    sakayWith = "You will be riding with " + sakay.otherAuthor;
                }
                otherUser = sakay.otherAuthor;
                otherUserId = sakay.otherUid;
                String dateTime = sakay.dateAndTime;
                String pickupLocation = "Pickup Location: " + sakay.start;
                String destination = "Destination: " + sakay.destination;
                String vehicleType = "Vehicle type: " + sakay.vehicle;
                String vehicleModel = "Manufacturer and model: " + sakay.vehicleModel;
                String vehicleColor = "Color: " + sakay.vehicleColor;
                String vehiclePlateNo = "Plate number: " + sakay.vehiclePlateNo;

                Spannable spannable = new SpannableString(sakayWith);
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#2480D9")), sakayWith.indexOf(sakay.otherAuthor),
                        sakayWith.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                otherAuthorNameView.setText(spannable, TextView.BufferType.SPANNABLE);
                dateAndTimeView.setText(dateTime);
                startView.setText(pickupLocation);
                destinationView.setText(destination);
                vehicleTypeView.setText(vehicleType);
                vehicleModelView.setText(vehicleModel);
                vehicleColorView.setText(vehicleColor);
                vehiclePlateNoView.setText(vehiclePlateNo);
                // [END_EXCLUDE]
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(SakayDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addListenerForSingleValueEvent(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        trackLocationButton = (FloatingActionButton) findViewById(R.id.fabTrackLocation);
        trackLocationButton.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                trackLocationButton.show(true);
                trackLocationButton.setShowAnimation (AnimationUtils.loadAnimation(SakayDetailActivity.this, R.anim.show_from_bottom));
                trackLocationButton.setHideAnimation(AnimationUtils.loadAnimation(SakayDetailActivity.this, R.anim.hide_to_bottom));
            }
        }, 300);


        userFacebookId = profile.getId();

        trackLocationButton.setOnClickListener(this);
        otherAuthorNameView.setOnClickListener(this);

//        driverText = "You will be driving for ";
//        riderText = "You will be riding with";

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.fabTrackLocation) {
            Toast.makeText(SakayDetailActivity.this, "Retrieving " + otherUser + "'s last known location",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TrackLocationActivity.class);
            //intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
            intent.putExtra(TrackLocationActivity.EXTRA_SAKAY_KEY, mPostKey);
            intent.putExtra(TrackLocationActivity.EXTRA_OTHER_USER_ID, otherUserId);
            startActivity(intent);
        } else if (id == R.id.sakay_detail_other_author){
            viewProfile();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
    }

    public void viewProfile(){
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ViewProfileActivity.EXTRA_USER_KEY, otherUserId);
        startActivity(intent);
    }
}
