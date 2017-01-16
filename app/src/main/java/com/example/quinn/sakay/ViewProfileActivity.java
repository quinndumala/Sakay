package com.example.quinn.sakay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ViewProfileActivity extends BaseActivity implements
        View.OnClickListener{

    private static final String TAG = "ViewProfile";

    public static final String EXTRA_USER_KEY = "user_key";

    private String mUserKey;
    private String mUserFbKey;
    private ValueEventListener mUserListener;
    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private DatabaseReference userNameRef;
    private DatabaseReference userFacebookIdRef;
    private DatabaseReference userEmailRef;
    private DatabaseReference userPhoneNoRef;
    private DatabaseReference userRatingsRef;
    private DatabaseReference userRatingRef;
    private DatabaseReference ratingsCountRef;
    private DatabaseReference ratingsRef;

    private ImageView userPhotoView;

    private TextView userRatingTextView;
    private TextView userRatingNumView;
    private ImageView userRatingStarView;

    private TextView phoneNumberView;
    private TextView emailTextView;
    private TextView facebookNameView;

    private ViewGroup viewProfileContent;
    private ProgressBar progressbar;
    private FloatingActionButton buttonRateUser;
    private ViewGroup mobileNumberView;
    private ViewGroup emailView;
    private ViewGroup facebookPageView;

    public String USER_FACEBOOK_ID;
    public String USER_EMAIL_ADDRESS;
    public String USER_PHONE_NO;
    public final String MY_USER_ID = getUid();

    public Boolean notYetRated = true;
    public Long aveRating = 0L;
    public Long numOfRatings;

    //final ProgressBar progressBar = (ProgressBar) findViewById(R.id.view_profile_progress);
    public MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserKey = getIntent().getStringExtra(EXTRA_USER_KEY);
        if (mUserKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        userPhotoView = (ImageView) findViewById(R.id.view_profile_image);

        userRatingTextView = (TextView) findViewById(R.id.view_profile_rating_text);
        userRatingNumView = (TextView) findViewById(R.id.view_profile_rating_number);
        userRatingStarView = (ImageView) findViewById(R.id.view_profile_rating_stars);

        phoneNumberView = (TextView) findViewById(R.id.view_profile_phone_num_text);
        emailTextView = (TextView) findViewById(R.id.view_profile_email_text);
        facebookNameView = (TextView) findViewById(R.id.view_profile_facebook_name_text);

        mobileNumberView = (ViewGroup) findViewById(R.id.view_profile_phone_num);
        emailView = (ViewGroup) findViewById(R.id.view_profile_email);
        facebookPageView = (ViewGroup) findViewById(R.id.view_profile_facebook);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mRootRef.child("users").child(mUserKey);
        userNameRef = mUserRef.child("name");
        userFacebookIdRef = mUserRef.child("facebookId");
        userEmailRef = mUserRef.child("email");
        userPhoneNoRef = mRootRef.child("users-settings").child(mUserKey).child("phone");

        userRatingsRef = mRootRef.child("users-ratings").child(mUserKey);
        userRatingRef = userRatingsRef.child("rating");
        ratingsCountRef = userRatingsRef.child("ratingsCount");
        ratingsRef = userRatingsRef.child("ratings");


        viewProfileContent = (ViewGroup) findViewById(R.id.view_profile_content);
        buttonRateUser = (FloatingActionButton) findViewById(R.id.view_profile_rate_button);

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading profile")
                .content("Please wait")
                .progress(true, 0)
                .show();

        userFacebookIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                setPhoto(data);
                USER_FACEBOOK_ID = data;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Read failed: " + databaseError.getCode());
            }
        });

        userRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String value = String.valueOf(dataSnapshot.getValue());
                    loadStars(value);
                    loadNumbers(value);
                    userRatingTextView.setText("User Rating");
                    userRatingNumView.setVisibility(View.VISIBLE);
                    userRatingStarView.setVisibility(View.VISIBLE);

//                    userRatingNumView.setText(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 550);
        if (!(mUserKey.equals(MY_USER_ID))){
            buttonRateUser.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                setTitle(data);
                facebookNameView.setText(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Read failed: " + databaseError.getCode());
            }
        });



        userEmailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                emailTextView.setText(data);
                USER_EMAIL_ADDRESS = data;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Read failed: " + databaseError.getCode());
            }
        });

        userPhoneNoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    USER_PHONE_NO = dataSnapshot.getValue(String.class);
                    phoneNumberView.setText(USER_PHONE_NO);
                    mobileNumberView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //hasRated();
        //listenToRatings();

        mobileNumberView.setOnClickListener(this);
        emailView.setOnClickListener(this);
        facebookPageView.setOnClickListener(this);
        buttonRateUser.setOnClickListener(this);


    }

    @Override
    public void onStop() {
        super.onStop();
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
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.view_profile_facebook) {
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            startActivity(facebookIntent);
        } else if (id == R.id.view_profile_email) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{USER_EMAIL_ADDRESS});
            try {
                startActivity(Intent.createChooser(intent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ViewProfileActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.view_profile_rate_button){
            launchRateDialog();
        } else if (id == R.id.view_profile_phone_num){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + USER_PHONE_NO));
            startActivity(intent);
        }
    }



    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=350";
        GlideUtil.loadProfileIcon(imageUrl, userPhotoView);
    }


    public String getFacebookPageURL(Context context) {
        String url = "https://www.facebook.com/"+USER_FACEBOOK_ID;
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + url;
            } else { //older versions of fb app
                return "fb://page/" + USER_FACEBOOK_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return url; //normal web url
        }
    }

    public void launchRateDialog() {
        new MaterialDialog.Builder(this)
                .title("Rate this user")
                .items(R.array.offer_select_rating_score)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        submitRating(text);
                        //showToast();
                        return true;
                    }
                })
                .positiveText("OK")
                .show();
    }

    public void loadStars(String value){
        if (value == "1"){
            userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_one));
        } else if (value == "2"){
            userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_two));
        } else if (value == "3"){
            userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_three));
        } else if (value == "4"){
            userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_four));
        } else if (value == "5"){
            userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_five));
        }
    }

    public void loadNumbers(String value){
        if (value == "1"){
            userRatingNumView.setText("1.0");
        } else if (value == "2"){
            userRatingNumView.setText("2.0");
        } else if (value == "3"){
            userRatingNumView.setText("3.0");
        } else if (value == "4"){
            userRatingNumView.setText("4.0");
        } else if (value == "5"){
            userRatingNumView.setText("5.0");
        }
    }

    public void updateRatings(){
        Log.d(TAG, "on listenToRatings");
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ratingsCountRef.setValue(dataSnapshot.getChildrenCount());

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        int value = snapshot.getValue(int.class);
                        aveRating = aveRating + value;
                    }

                    aveRating = aveRating/dataSnapshot.getChildrenCount();
                    userRatingRef.setValue(aveRating);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void submitRating(CharSequence text){
        Log.d(TAG, "on submitRating");
        int score;

        String rating = text.toString();
        if (rating.equals("5  Excellent")){
            score = 5;
        } else if (rating.equals("4  Great")) {
            score = 4;
        } else if (rating.equals("3  Satisfactory")){
            score = 3;
        } else if (rating.equals("2  Unsatisfied")){
            score = 2;
        } else {
            score = 1;
        }


        //String key = ratingsRef.child(MY_USER_ID).push().getKey();
//        if (notYetRated) {
//            addRatingsCount();
//        }

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users-ratings/" + mUserKey + "/ratings/" + MY_USER_ID, score);
        mRootRef.updateChildren(childUpdates);
        updateRatings();

        showToast();

    }

    public void addRatingsCount() {
        ratingsCountRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long value = currentData.getValue(Long.class);
                if(value == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(value + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                //This method will be called once with the results of the transaction.
                Log.d(TAG, "transaction:onComplete:" + databaseError);
            }
        });
    }

    public void showToast(){
        Toast.makeText(this, "User Rating submitted", Toast.LENGTH_SHORT).show();
    }

//    public void loadProfileImage(String url, ImageView imageView){
//        Context context = imageView.getContext();
//        Glide.with(context)
//                .load(url)
//                .listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        progressBar.setVisibility(View.GONE);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        progressBar.setVisibility(View.GONE);
//                        return false;
//                    }
//                })
//                .into(imageView);
//    }
}
