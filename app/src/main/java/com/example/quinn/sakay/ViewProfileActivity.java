package com.example.quinn.sakay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewProfileActivity extends AppCompatActivity implements
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

    private ImageView userPhotoView;

    private TextView userRatingTextView;
    private TextView userRatingNumView;
    private ImageView userRatingStarView;

    private TextView phoneNumberView;
    private TextView emailView;
    private TextView facebookNameView;

    private ViewGroup viewProfileContent;
    private ProgressBar progressbar;
    private FloatingActionButton ButtonRateUser;
    //final ProgressBar progressBar = (ProgressBar) findViewById(R.id.view_profile_progress);

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
        emailView = (TextView) findViewById(R.id.view_profile_email_text);
        facebookNameView = (TextView) findViewById(R.id.view_profile_facebook_name_text);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mRootRef.child("users").child(mUserKey);
        userNameRef = mUserRef.child("name");
        userFacebookIdRef = mUserRef.child("facebookId");

        viewProfileContent = (ViewGroup) findViewById(R.id.view_profile_content);
        ButtonRateUser = (FloatingActionButton) findViewById(R.id.view_profile_rate_button);

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

        userFacebookIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                setPhoto(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Read failed: " + databaseError.getCode());
            }
        });



//        viewProfileContent.setVisibility(View.VISIBLE);
//        ButtonRateUser.setVisibility(View.VISIBLE);


    }

    @Override
    public void onStart() {
        super.onStart();


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

    }



    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=350";
        GlideUtil.loadProfileIcon(imageUrl, userPhotoView);
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
