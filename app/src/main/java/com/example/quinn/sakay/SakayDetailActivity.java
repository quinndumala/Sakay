package com.example.quinn.sakay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.Sakay;
import com.facebook.Profile;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

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
    private Long sakayTimestamp;

    private TextView otherAuthorNameView;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;

    private TextView vehicleTypeView;
    private TextView vehicleModelView;
    private TextView vehicleColorView;
    private TextView vehiclePlateNoView;

    public Boolean canDelete = false;

    public MaterialDialog loadingDialog;
    private static final long ONE_HOUR = 60 * 60 * 1000;

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
                if (dataSnapshot.exists()){
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
                    sakayTimestamp = sakay.timeStamp;
                    String pickupLocation = "Pickup Location: " + sakay.start;
                    String destination = "Destination: " + sakay.destination;
                    String vehicleType = "Vehicle type: " + sakay.vehicle;
                    String vehicleModel = "Manufacturer and model: " + sakay.vehicleModel;
                    String vehicleColor = "Color: " + sakay.vehicleColor;
                    String vehiclePlateNo = "Plate number: " + sakay.vehiclePlateNo;

                    Log.d(TAG, "sakayTimestamp: " + sakayTimestamp);

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
                    showFab();
                }

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
            if (canTrackUser(sakayTimestamp)){

                Intent intent = new Intent(this, TrackLocationActivity.class);
                //intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
                intent.putExtra(TrackLocationActivity.EXTRA_SAKAY_KEY, mPostKey);
                intent.putExtra(TrackLocationActivity.EXTRA_OTHER_USER_ID, otherUserId);
                intent.putExtra(TrackLocationActivity.EXTRA_OTHER_USER_NAME, otherUser);
                startActivity(intent);
            } else {
                showNotYetDialog();
            }

        } else if (id == R.id.sakay_detail_other_author){
            viewProfile();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sakay_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        } else if (id == R.id.action_delete){
            if (canDelete){
                showConfirmDelete();
            } else {
                showCantDelete();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void showFab(){
        if (sakayTimestamp != null && checkTime(sakayTimestamp)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    trackLocationButton.show(true);
                    trackLocationButton.setShowAnimation (AnimationUtils.loadAnimation(SakayDetailActivity.this, R.anim.show_from_bottom));
                    trackLocationButton.setHideAnimation(AnimationUtils.loadAnimation(SakayDetailActivity.this, R.anim.hide_to_bottom));
                }
            }, 300);
        } else {
            trackLocationButton.hide(false);
            canDelete = true;
        }
    }

    public Boolean checkTime(Object timestamp){
        long currentTime = System.currentTimeMillis();
        long sakayTime = (long) timestamp;
        long hourLater = sakayTime + TimeUnit.HOURS.toMillis(1);

        Log.d(TAG, "Current Time: " + currentTime);
        Log.d(TAG, "Report Time: " + sakayTime);

        if (currentTime > hourLater){
            Log.d(TAG, "false");
            return false;

        } else {
            Log.d(TAG, "true");
            return true;
        }

    }

    public Boolean canTrackUser(Object timestamp){
//        long currentTime = System.currentTimeMillis();
//        long sakayTime = (long) timestamp;
//        long diff = currentTime - sakayTime;
//
//        Log.d(TAG, "Current Time: " + currentTime);
//        Log.d(TAG, "Report Time: " + sakayTime);
//
//        if (diff > 60 * 60 * 1000){
//            return false;
//        } else {
//            return true;
//        }
        long currentTime = System.currentTimeMillis();
        long sakayTime = (long)timestamp;
        long hourBefore = sakayTime - TimeUnit.HOURS.toMillis(1);

        Log.d(TAG, "one hour ago: " + hourBefore);
        Log.d(TAG, "sakay time: " + sakayTime);

        if (currentTime > hourBefore) {
            //timeStamp passed is more than hour ago from right now

            return true;
        } else {

            return false;
        }
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

    public void showNotYetDialog(){
        new MaterialDialog.Builder(this)
                .title("Not yet")
                .content(R.string.not_yet_text)
                .positiveText("OK")
                .show();
    }

    public void deleteSakay(){
        finish();
        mPostReference.removeValue();
        Toast.makeText(this, "Sakay deleted", Toast.LENGTH_SHORT).show();
    }

    public void showConfirmDelete(){
        new MaterialDialog.Builder(this)
                .content("Delete this sakay?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                       deleteSakay();

                    }
                })
                .show();
    }

    public void showCantDelete(){
        new MaterialDialog.Builder(this)
                .title("Warning")
                .content("You can't delete a sakay before the scheduled date")
                .positiveText("OK")
                .show();
    }
}
