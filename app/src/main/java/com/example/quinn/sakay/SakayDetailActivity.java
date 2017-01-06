package com.example.quinn.sakay;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

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

    private String userFacebookId = "";
    private Profile profile = getCurrentProfile();
    private final String userId = getUid();
    private TextView otherAuthorNameView;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;
    private TextView vehicleView;
    private FloatingActionButton trackLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sakay_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mPostReference = mRootRef.child("user-sakays").child(userId).child(mPostKey);

        //Views
        otherAuthorNameView = (TextView) findViewById(R.id.sakay_detail_other_author);
        dateAndTimeView = (TextView) findViewById(R.id.sakay_detail_date_and_time);
        startView = (TextView) findViewById(R.id.sakay_detail_start);
        destinationView = (TextView) findViewById(R.id.sakay_detail_destination);
        vehicleView = (TextView) findViewById(R.id.sakay_detail_vehicle);

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

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.fabTrackLocation) {
            Toast.makeText(SakayDetailActivity.this, "Track Location", Toast.LENGTH_SHORT).show();
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

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Sakay sakay = dataSnapshot.getValue(Sakay.class);
                // [START_EXCLUDE]
                //Photo
                // setPhoto(rideOffer.facebookId);
                String sakayWith;
                if(sakay.role == "driver")
                    sakayWith = "You will be joined by " + sakay.otherAuthor;
                else
                    sakayWith = "You will be joining " + sakay.otherAuthor;
                String dateTime = sakay.dateAndTime;
                String pickupLocation = "Pickup Location: " + sakay.start;
                String destination = "Destination: " + sakay.destination;
                String onVehicle = "Vehicle: " + sakay.vehicle;

                otherAuthorNameView.setText(sakayWith);
                dateAndTimeView.setText(dateTime);
                startView.setText(pickupLocation);
                destinationView.setText(destination);
                vehicleView.setText(onVehicle);
                // [END_EXCLUDE]

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
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
    }
}
