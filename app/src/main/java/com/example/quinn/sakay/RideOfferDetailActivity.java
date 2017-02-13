package com.example.quinn.sakay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.CommentOffer;
import com.example.quinn.sakay.Models.Notif;
import com.example.quinn.sakay.Models.RideOffer;
import com.example.quinn.sakay.Models.Sakay;
import com.example.quinn.sakay.Models.Settings;
import com.facebook.Profile;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.Profile.getCurrentProfile;

public class RideOfferDetailActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "RideOfferDetail";

    public static final String EXTRA_POST_KEY = "post_key";
    private static final int REQUEST_PLACE_PICKER = 1;

    private DatabaseReference mRootRef;
    private DatabaseReference mPostReference;
    private DatabaseReference mUserPostReference;
    private DatabaseReference mCommentsReference;
    private DatabaseReference userSettingsRef;

    private ValueEventListener mPostListener;
    private String mPostKey;
    private RideOfferDetailActivity.CommentAdapter mAdapter;

    private TextView authorView;
    private CircleImageView authorPhotoView;
    private ImageView buttonDelete;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;
    private TextView vehicleView;
//    private ViewGroup responsesTextView;
    //private TextView noResponsesYetTextView;
    private Button sakayButton;
    private Button seeRouteButton;
    private RecyclerView sakaysViewRecycler;

    private CardView responsesTextView;
    private TextView noResponsesYetTextView;
    private CardView responsesView;

    private CardView acceptedView;
    private CircleImageView acceptedPhotoView;
    private TextView acceptedAuthorView;
    private TextView acceptedBodyView;

    //private Button acceptedButon;

    private String userFacebookId = "";
    public Boolean isAuthor = true;
    public Boolean rideExists = false;
    private Profile profile = getCurrentProfile();
    public Long postTimeStamp;

    private final String userId = getUid();
    private String userAuthorId;
    private String userAuthorName;
    private String userAuthorFacebookId;

    private String start;
    private String startLat;
    private String startLong;

    private String destination;
    private Double destinationLat;
    private Double destinationLong;

    private String dateAndTime;
    private Long timeStamp;

    private String vehicle;
    private String vehicleModel;
    private String vehicleColor;
    private String vehiclePlateNo;

    private String currentHome;
    private Double currentHomeLat;
    private Double currentHomeLong;
    private String currentWork;
    private Double currentWorkLat;
    private Double currentWorkLong;

    public Boolean workSet = false;
    public Boolean homeSet = false;
    public Boolean timeNotPassed = true;

    public MaterialDialog progressDialog;
    public MaterialDialog loadingDialog;
    public User currentUser;

    public Boolean isAvailable = true;
    public String acceptedRequest;

    public String acceptedFid;
    public String acceptedBody;
    public String acceptedName;
    public Button acceptedButton;

    public Boolean isAcceptedUser = false;

    public LatLng startLocation;
    public LatLng endLocation;

    //String arrRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_offer_detail);
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
        mPostReference = mRootRef.child("rideOffers").child(mPostKey);
        mUserPostReference = mRootRef.child("user-rideOffers").child(userId).child(mPostKey);
        mCommentsReference = mRootRef.child("rideOffers-comments").child(mPostKey);
        userSettingsRef = mRootRef.child("users-settings").child(userId);
//        homeAddressRef = userSettingsRef.child("home");
//        homeLatRef = userSettingsRef.child("homeLat");
//        homeLongRef = userSettingsRef.child("homeLong");
//        workAddressRef = userSettingsRef.child("work");
//        workLatRef = userSettingsRef.child("workLat");
//        workLongRef = userSettingsRef.child("workLong");

        // Initialize Views
        authorView = (TextView) findViewById(R.id.post_author_large);
        authorPhotoView = (CircleImageView) findViewById(R.id.post_author_photo_large);
        buttonDelete = (ImageView) findViewById(R.id.button_offer_detail_delete);
        startView = (TextView) findViewById(R.id.offer_start_view);
        destinationView = (TextView) findViewById(R.id.offer_destination_view);
        vehicleView = (TextView) findViewById(R.id.offer_vehicle_view);
        dateAndTimeView = (TextView) findViewById(R.id.offer_dateAndTime_view);

        responsesTextView = (CardView) findViewById(R.id.offer_responses_text_view);
        noResponsesYetTextView = (TextView) findViewById(R.id.no_responses_text_view_offer);
        responsesView = (CardView) findViewById(R.id.responses_view_offer);

        sakayButton = (Button) findViewById(R.id.button_sakay_offer);
        seeRouteButton = (Button) findViewById(R.id.button_see_route_offer);
        sakaysViewRecycler = (RecyclerView) findViewById(R.id.recycler_offer_comment);

        acceptedView = (CardView) findViewById(R.id.accepted_offer_view);
        acceptedPhotoView = (CircleImageView) findViewById(R.id.comment_accepted_author_photo_offer);
        acceptedAuthorView = (TextView) findViewById(R.id.comment_accepted_author_offer);
        acceptedBodyView = (TextView) findViewById(R.id.comment_accepted_pickup_offer);

        acceptedButton = (Button) findViewById(R.id.comment_button_view_profile_offer);

        userFacebookId = profile.getId();
        //noResponses();

        loadingDialog = new MaterialDialog.Builder(this)
                .title("Loading details")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        sakayButton.setOnClickListener(this);
        seeRouteButton.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        authorPhotoView.setOnClickListener(this);
        authorView.setOnClickListener(this);
        acceptedButton.setOnClickListener(this);
        sakaysViewRecycler.setLayoutManager(new LinearLayoutManager(this));

        mRootRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        mPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    RideOffer rideOffer = dataSnapshot.getValue(RideOffer.class);
//                    if (rideOffer.timeStamp < System.currentTimeMillis()){
//                        sakayButton.setEnabled(false);
//                        timeNotPassed = false;
//                    }
//                    isAvailable = rideOffer.available;
//                    if (!isAvailable){
//                        sakayButton.setEnabled(false);
//                        //acceptedPhotoView
//                    }
//                    acceptedRequest = rideOffer.accepted;
//                    setPhoto(rideOffer.facebookId);
//                    authorView.setText(rideOffer.author);
//                    startView.setText(rideOffer.start);
//                    destinationView.setText(rideOffer.destination);
//                    vehicleView.setText(rideOffer.vehicle);
//                    dateAndTimeView.setText(rideOffer.dateAndTime);
//                    if ((rideOffer.uid.equals(userId))){
//                        buttonDelete.setVisibility(View.VISIBLE);
//                    } else {
//                        isAuthor = false;
//                        sakayButton.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId) && timeNotPassed && isAvailable){
                    sakayButton.setText("\u2713" + " Sakay request sent");
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                finish();
            }
        });


        checkAddress();
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
        checkConnection();

        // Add value event listener to the post
        // [START post_value_event_listener]
       // noResponses();

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading map")
                .content("Please wait")
                .progress(true, 0)
                .build();

        mCommentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId) && timeNotPassed && isAvailable){
                    sakayButton.setText("\u2713" + " Sakay request sent");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                finish();
            }
        });

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get Post object and use the values to update the UI
                    RideOffer rideOffer = dataSnapshot.getValue(RideOffer.class);

                    if (rideOffer.timeStamp < System.currentTimeMillis()){
                        sakayButton.setEnabled(false);
                        timeNotPassed = false;
                    }
                    isAvailable = rideOffer.available;
                    if (!isAvailable){
                        sakayButton.setEnabled(false);
                        //acceptedPhotoView
                    }

                    setPhoto(rideOffer.facebookId);
                    authorView.setText(rideOffer.author);
                    startView.setText(rideOffer.start);
                    destinationView.setText(rideOffer.destination);
                    vehicleView.setText(rideOffer.vehicle);
                    dateAndTimeView.setText(rideOffer.dateAndTime);

                    // [START_EXCLUDE]
//                    setPhoto(rideOffer.facebookId);
//                    authorView.setText(rideOffer.author);
//                    startView.setText(rideOffer.start);
//                    destinationView.setText(rideOffer.destination);
//                    vehicleView.setText(rideOffer.vehicle);
//                    dateAndTimeView.setText(rideOffer.dateAndTime);
                    // [END_EXCLUDE]

                    userAuthorId = rideOffer.uid;
                    userAuthorName = rideOffer.author;
                    userAuthorFacebookId = rideOffer.facebookId;

                    start = rideOffer.start;
                    startLat = rideOffer.startLat.toString();
                    startLong = rideOffer.startLong.toString();

                    destination = rideOffer.destination;
                    destinationLat = rideOffer.destinationLat;
                    destinationLong = rideOffer.destinationLong;

                    dateAndTime = rideOffer.dateAndTime;
                    timeStamp = rideOffer.timeStamp;

                    vehicle = rideOffer.vehicle;
                    vehicleModel = rideOffer.vehicleModel;
                    vehicleColor = rideOffer.vehicleColor;
                    vehiclePlateNo = rideOffer.vehiclePlateNo;

                    acceptedRequest = rideOffer.accepted;
                    if (!acceptedRequest.equals("none")){
                        getAcceptedDetail(acceptedRequest);
                    }

                    if ((rideOffer.uid.equals(userId))){
                        buttonDelete.setVisibility(View.VISIBLE);
                    } else {
                        isAuthor = false;
                        sakayButton.setVisibility(View.VISIBLE);
                    }

                    noResponses();

//                    if (!isAvailable){
//                         noResponsesYetTextView.setText("This ride offer is no longer available.");
//                         noResponsesYetTextView.setVisibility(View.VISIBLE);
//                    }

                    rideExists = true;
                } else {
                    finish();
                    Toast.makeText(RideOfferDetailActivity.this, "Failed to load ride request details",
                            Toast.LENGTH_SHORT);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(RideOfferDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mPostReference.addValueEventListener(postListener);
        mPostListener = postListener;

        mAdapter = new RideOfferDetailActivity.CommentAdapter(this, mCommentsReference);
        sakaysViewRecycler.setAdapter(mAdapter);


    }

    public void getAcceptedDetail(final String acceptedUid){
        mCommentsReference.child(acceptedUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    CommentOffer commentOffer = dataSnapshot.getValue(CommentOffer.class);
                    acceptedFid = commentOffer.facebookId;
                    acceptedBody = commentOffer.pickUp;
                    acceptedName = commentOffer.author;
                    setAcceptedPhoto(acceptedFid);
                    acceptedAuthorView.setText(acceptedName);
                    acceptedBodyView.setText(acceptedBody);
                    Log.d(TAG, "FID: " + acceptedFid);
                    Log.d(TAG, "Author: " + acceptedBody);
                    Log.d(TAG, "Body: " + acceptedName);


                } else {
                    Toast.makeText(RideOfferDetailActivity.this, "Error loading details", Toast.LENGTH_SHORT);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();


        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_sakay_offer) {
            alreadyExists();
        } else if(id == R.id.button_see_route_offer) {
//            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                    Uri.parse("http://maps.google.com/maps?saddr=" + startLat + "," + startLong +
//                                "&daddr=" + destinationLat + "," + destinationLong));
            Intent intent = new Intent(this, ViewMapActivity.class);
            intent.putExtra(ViewMapActivity.EXTRA_POST_KEY, mPostKey);
            intent.putExtra(ViewMapActivity.EXTRA_POST_TYPE, "offer");
            startActivity(intent);
        } else if (id == R.id.button_offer_detail_delete) {
            Log.d(TAG, "delete clicked");
            launchConfirmDelete();
        } else if (id == R.id.post_author_photo_large){
            viewProfile(userAuthorId);
        } else if (id == R.id.post_author_large){
            viewProfile(userAuthorId);
        } else if (id == R.id.comment_button_view_profile_offer) {
            viewProfile(acceptedRequest);
        }

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
                    .make(this.findViewById(R.id.content_ride_offer_detail), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    public void checkAddress(){
//        homeAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    homeSet = true;
//                    currentHome = dataSnapshot.getValue(String.class);
//                } else {
//                    homeSet = false;
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        homeLatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    currentHomeLat = dataSnapshot.getValue(Double.class);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        homeLongRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    currentHomeLong = dataSnapshot.getValue(Double.class);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        workAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    workSet = true;
//                    currentWork = dataSnapshot.getValue(String.class);
//                } else {
//                    workSet = false;
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        workLatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    currentWorkLat = dataSnapshot.getValue(Double.class);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        workLongRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    currentWorkLong = dataSnapshot.getValue(Double.class);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        userSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    if (settings.home != null){
                        homeSet = true;
                        currentHome = settings.home;
                    } else {
                        homeSet = false;
                    }

                    if (settings.homeLat != null){
                        currentHomeLat = settings.homeLat;
                    }

                    if (settings.homeLong != null){
                        currentHomeLong = settings.homeLong;
                    }

                    if (settings.work != null){
                        workSet = true;
                        currentWork = settings.work;
                    } else {
                        workSet = false;
                    }

                    if (settings.workLat != null){
                        currentWorkLat = settings.workLat;
                    }

                    if (settings.workLong != null){
                        currentWorkLong = settings.workLong;
                    }
                } else {
                    homeSet = false;
                    workSet = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void viewProfile(String userId){
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ViewProfileActivity.EXTRA_USER_KEY, userId);
        startActivity(intent);
    }

    public void alreadyExists(){

        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            String[] arrRef;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)){
                    launchAlreadySentDialog();
                } else {
                    if (homeSet && workSet){
                        arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_all);
                    } else if (homeSet) {
                        arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_home);
                    } else if (workSet) {
                        arrRef = getResources().getStringArray(R.array.offer_select_pickup_location_work);
                    } else {
                        arrRef = getResources().getStringArray(R.array.offer_select_pickup_location);
                    }
                    launchPickupLocationPicker(arrRef);

                    //launchSakayDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void noResponses(){
        mCommentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    if (isAuthor){
                        if (!timeNotPassed){
                            noResponsesYetTextView.setText(R.string.time_passed_text);
                            noResponsesYetTextView.setVisibility(View.VISIBLE);
                        } else {
                            if (!isAvailable){
                                acceptedView.setVisibility(View.VISIBLE);
                                noResponsesYetTextView.setVisibility(View.GONE);
                                responsesTextView.setVisibility(View.GONE);
                                responsesView.setVisibility(View.GONE);
                            } else {
                                acceptedView.setVisibility(View.GONE);
                                noResponsesYetTextView.setVisibility(View.GONE);
                                responsesTextView.setVisibility(View.VISIBLE);
                                responsesView.setVisibility(View.VISIBLE);
                            }
                        }
//                        responsesTextView.setVisibility(View.VISIBLE);
//                        responsesView.setVisibility(View.VISIBLE);
                    } else {
                        if (!timeNotPassed){
                            noResponsesYetTextView.setText(R.string.time_passed_text);
                            noResponsesYetTextView.setVisibility(View.VISIBLE);
                        } else {
                            if (!isAvailable){
                                if (acceptedRequest.equals(userId)) {
                                    acceptedView.setVisibility(View.VISIBLE);
                                    responsesTextView.setVisibility(View.GONE);
                                    responsesView.setVisibility(View.GONE);
                                    noResponsesYetTextView.setVisibility(View.GONE);
                                } else {
                                    acceptedView.setVisibility(View.GONE);
                                    responsesTextView.setVisibility(View.GONE);
                                    responsesView.setVisibility(View.GONE);
                                    noResponsesYetTextView.setText(R.string.not_available_text);
                                    noResponsesYetTextView.setVisibility(View.VISIBLE);
                                }

                            } else {
                                acceptedView.setVisibility(View.GONE);
                                noResponsesYetTextView.setVisibility(View.GONE);
                                responsesTextView.setVisibility(View.GONE);
                                responsesView.setVisibility(View.GONE);
                            }
                        }

                    }

                } else {
//                    responsesTextView.setVisibility(View.GONE);
//                    responsesView.setVisibility(View.GONE);
//
//                    if (isAuthor){
//
//                        if (!isAvailable){
//                            noResponsesYetTextView.setText(R.string.not_available_text);
//                            noResponsesYetTextView.setVisibility(View.VISIBLE);
//                        } else {
//                            noResponsesYetTextView.setVisibility(View.VISIBLE);
//                        }
//
//                        if (!timeNotPassed){
//                            noResponsesYetTextView.setText(R.string.time_passed_text);
//                            //noResponsesYetTextView.setVisibility(View.VISIBLE);
//                        }
//
//                    } else {
//                        if (!timeNotPassed){
//                            noResponsesYetTextView.setText(R.string.time_passed_text);
//                            //noResponsesYetTextView.setVisibility(View.VISIBLE);
//                        } else if (!isAvailable){
//                            noResponsesYetTextView.setText(R.string.not_available_text);
//                            noResponsesYetTextView.setVisibility(View.VISIBLE);
//                        }
//                    }
                    acceptedView.setVisibility(View.GONE);
                    if (isAuthor){
                        if (!timeNotPassed){
                            noResponsesYetTextView.setText(R.string.time_passed_text);
                            noResponsesYetTextView.setVisibility(View.VISIBLE);
                        } else {

                            acceptedView.setVisibility(View.GONE);
                            noResponsesYetTextView.setVisibility(View.VISIBLE);
                            responsesTextView.setVisibility(View.GONE);
                            responsesView.setVisibility(View.GONE);
                        }
                    } else {
                        if (!timeNotPassed){
                            noResponsesYetTextView.setText(R.string.time_passed_text);
                            noResponsesYetTextView.setVisibility(View.VISIBLE);
                        } else {
                                acceptedView.setVisibility(View.GONE);
                                noResponsesYetTextView.setVisibility(View.GONE);
                                responsesTextView.setVisibility(View.GONE);
                                responsesView.setVisibility(View.GONE);
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deletePost(){
        finish();
        mUserPostReference.removeValue();
        mPostReference.removeValue();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        //DeletePost.DeleteThis(mPostReference, mUserPostReference);
    }

    private void postComment(final String location, final Double lat, final Double lng) {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.getName();

                        CommentOffer comment = new CommentOffer(uid, authorName, userFacebookId,
                                location, lat, lng);

                        // Push the comment, it will appear in the list
                        Map<String, Object> postValues = comment.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/rideOffers-comments/" + mPostKey + "/" + userId, postValues);

                        mRootRef.updateChildren(childUpdates);
//                        noResponses();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView authorView;
        public TextView pickUpView;
        public CircleImageView authorPhotoView;
        public Button buttonViewProfile;
        public Button buttonSakay;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorView = (TextView) itemView.findViewById(R.id.comment_author_offer);
            pickUpView = (TextView) itemView.findViewById(R.id.comment_pickup_offer);
            authorPhotoView = (CircleImageView) itemView.findViewById(R.id.comment_author_photo_offer);
            buttonSakay = (Button) itemView.findViewById(R.id.comment_button_sakay_offer);
            buttonViewProfile = (Button) itemView.findViewById(R.id.comment_button_view_profile_offer);
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<CommentOffer> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    CommentOffer comment = dataSnapshot.getValue(CommentOffer.class);

                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    noResponsesYetTextView.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    CommentOffer newComment = dataSnapshot.getValue(CommentOffer.class);
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mComments.set(commentIndex, newComment);
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    CommentOffer movedComment = dataSnapshot.getValue(CommentOffer.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment_offer, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            final CommentOffer comment = mComments.get(position);
            final String commentAuthor = comment.author;
            final String commentPickup = comment.pickUp;
            final Double commentPickupLat = comment.pickUpLat;
            final Double commentPickupLong = comment.pickUpLong;
            final String commentFacebookId = comment.facebookId;
            final String commentAuthorUid = comment.uid;

            holder.authorView.setText(commentAuthor);
            holder.pickUpView.setText(commentPickup);
            String imageUrl = "https://graph.facebook.com/" + comment.facebookId + "/picture?height=150";
            GlideUtil.loadProfileIcon(imageUrl, holder.authorPhotoView);

            holder.buttonSakay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchConfirmSakay(commentAuthorUid, commentAuthor, commentFacebookId,
                            commentPickup, commentPickupLat, commentPickupLong);
                }
            });

            holder.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    intent.putExtra(ViewProfileActivity.EXTRA_USER_KEY, commentAuthorUid);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

        public void launchConfirmSakay(final String commentAuthorId, final String commentAuthor,
                                       final String commentFacebookId, final String commentPickup,
                                       final Double commentPickupLat, final Double commentPickupLong){
            new MaterialDialog.Builder(mContext)
                    .content("Confirm Sakay?")
                    .positiveText("OK")
                    .negativeText("CANCEL")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String sakayKey = mRootRef.child("user-sakays").push().getKey();
                            newSakay(userId, userAuthorName, userFacebookId, "driver",
                                    commentPickup, commentPickupLat, commentPickupLong,
                                    destination, destinationLat, destinationLong,
                                    dateAndTime, timeStamp,
                                    vehicle, vehicleModel, vehicleColor, vehiclePlateNo,
                                    commentAuthorId, commentAuthor, commentFacebookId,
                                    sakayKey);
                            newSakay(commentAuthorId, commentAuthor, commentFacebookId, "rider",
                                    commentPickup, commentPickupLat, commentPickupLong,
                                    destination, destinationLat, destinationLong,
                                    dateAndTime, timeStamp,
                                    vehicle, vehicleModel, vehicleColor, vehiclePlateNo,
                                    userId, userAuthorName, userFacebookId,
                                    sakayKey);

                            createNotif(sakayKey, "sakay", commentAuthorId);

                            mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                                        CommentOffer commentOffer = snapshot.getValue(CommentOffer.class);
                                        Log.d(TAG, "Child uid: " + commentOffer.uid);
                                        if (!commentOffer.uid.equals(commentAuthorId)) {
                                            createNotif(mPostKey, "requestNotAvailable", commentOffer.uid);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                           // createNotif(mPostKey, "notAvailable", null);

                            setAvailableFalse(mPostReference, commentAuthorId);
                            setAvailableFalse(mUserPostReference, commentAuthorId);

                            Toast.makeText(mContext, "Sakay succesfully added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .show();
        }

        private void newSakay(String userId, String userName, String userFacebookId, String userRole,
                              String start, Double startLat, Double startLong,
                              String destination, Double destinationLat, Double destinationLong,
                              String dateAndTime, Long timeStamp,
                              String vehicle, String vehicleModel, String vehicleColor, String vehiclePlateNo,
                              String otherId, String otherName, String otherFacebookId,
                              String sakayKey){
            //String key = mRootRef.child("user-sakays").push().getKey();
            Sakay sakay = new Sakay(userId, userName, userFacebookId, userRole,
                    start, startLat, startLong,
                    destination, destinationLat, destinationLong,
                    dateAndTime, timeStamp,
                    vehicle, vehicleModel, vehicleColor, vehiclePlateNo,
                    otherId, otherName, otherFacebookId);
            Map<String, Object> sakayValues = sakay.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/user-sakays/" + userId + "/" + sakayKey, sakayValues);
            mRootRef.updateChildren(childUpdates);
        }
    }

    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, authorPhotoView);
    }

    public void setAcceptedPhoto(final String fId){
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, acceptedPhotoView);
    }

    public void launchSakayDialog(final String location, final Double lat, final Double lng){
        new MaterialDialog.Builder(this)
                .content("Pickup location has been set to " + location + ". Send sakay request?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postComment(location, lat, lng);
                        createNotif(mPostKey, "request", null);
                        Toast.makeText(RideOfferDetailActivity.this, "Sakay request sent",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void launchAlreadySentDialog(){
        new MaterialDialog.Builder(this)
                .content("Sakay request already sent")
                .positiveText("OK")
                .show();
    }

    public void launchPickupLocationPicker(String[] arrRef){
        new MaterialDialog.Builder(this)
                .title("Choose a pickup location")
                .items(arrRef)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if(text.equals("Choose on Map")){
                            progressDialog.show();
                            launchPlacePicker();
                        } else if(text.equals("My Home Address")){
                            postComment(currentHome, currentHomeLat, currentHomeLong);
                            createNotif(mPostKey, "request", null);
                            Toast.makeText(RideOfferDetailActivity.this, "Sakay request sent",
                                    Toast.LENGTH_SHORT).show();

                        } else if(text.equals("My Work Address")){
                            postComment(currentWork, currentWorkLat, currentWorkLong);
                            createNotif(mPostKey, "request", null);
                            Toast.makeText(RideOfferDetailActivity.this, "Sakay request sent",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .positiveText("cancel")
                .show();

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

    public void launchConfirmDelete(){
        new MaterialDialog.Builder(this)
                .content("Do you want to delete this ride offer?")
                .positiveText("OK")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deletePost();
                    }
                })
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

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                final LatLng placeLatLng = place.getLatLng();

                String attribution = PlacePicker.getAttributions(data);
                if(attribution == null){
                    attribution = "";
                }

                String location = name.toString();
                if (location.contains("°")){
                    location = address.toString();
                    launchAlertDialog();
                } else {
                    launchSakayDialog(location, placeLatLng.latitude, placeLatLng.longitude);
                    //Toast.makeText(this, "Chosen Location: " + location, Toast.LENGTH_SHORT).show();
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

    public void createNotif(String key, String type, String commentAuthorID){
        String notifId;
        String notifKey = mRootRef.child("user-notifications").push().getKey();
        Notif notif;
        String action;

        if (type.equals("request")){
            notifId = userAuthorId;
            action = "sent you a ride request";
        } else if (type.equals("requestNotAvailable")){
            notifId = commentAuthorID;
            action = "Ride offer is no longer available";
        } else {
            notifId = commentAuthorID;
            action = "accepted your ride request";
        }

        notif = new Notif(userId, currentUser.getName(), currentUser.getFacebookId(), type, key,
                action, false, ServerValue.TIMESTAMP);
        Map<String, Object> notifValues = notif.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-notifications/" + notifId + "/" + notifKey, notifValues);
        mRootRef.updateChildren(childUpdates);

        mRootRef.child("notif-check").child(notifId).setValue(true);

    }

    public void setAvailableFalse(DatabaseReference postRef, final String acceptedId){
        postRef.runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                RideOffer offer = mutableData.getValue(RideOffer.class);
                if (offer == null) {
                    return Transaction.success(mutableData);
                }
                offer.available = false;
                offer.accepted = acceptedId;
                mutableData.setValue(offer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
}
