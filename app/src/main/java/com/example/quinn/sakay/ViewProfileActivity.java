package com.example.quinn.sakay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
        userPhoneNoRef = mUserRef.child("phone");

        viewProfileContent = (ViewGroup) findViewById(R.id.view_profile_content);
        buttonRateUser = (FloatingActionButton) findViewById(R.id.view_profile_rate_button);

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading profile")
                .content("Please wait")
                .progress(true, 0)
                .show();


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
                USER_FACEBOOK_ID = data;
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

        mobileNumberView.setOnClickListener(this);
        emailView.setOnClickListener(this);
        facebookPageView.setOnClickListener(this);
        buttonRateUser.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 550);

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
        int id = view.getId();

        if (id == R.id.view_profile_facebook) {
//            Intent facebookIntent = getOpenFacebookIntent(this);
//            startActivity(facebookIntent);

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

//    public Intent getOpenFacebookIntent(Context context) {
//        String url = "https://www.facebook.com/"+USER_FACEBOOK_ID;
//        try {
//            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
//            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href="+url));
//        } catch (Exception e) {
//            return new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.facebook.com/" + USER_FACEBOOK_ID));
//        }
//    }

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
                        showToast();
                        return true;
                    }
                })
                .positiveText("OK")
                .show();
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
