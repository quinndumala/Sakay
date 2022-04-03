package com.example.quinn.sakay;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.facebook.Profile.getCurrentProfile;

//import static android.support.v4.widget.EdgeEffectCompatIcs.finish;

//import com.google.firebase.quickstart.database.models.Post;
//import com.google.firebase.quickstart.database.models.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment
        implements
        GoogleApiClient.OnConnectionFailedListener,
        ConnectivityReceiver.ConnectivityReceiverListener {

    private ViewGroup accountView;
    //private View view;
    private Button signOutFacebook;
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;
    private DatabaseReference userRef;

    private DatabaseReference userRatingsRef;
    private DatabaseReference userRatingRef;
    private DatabaseReference ratingsCountRef;
    private DatabaseReference ratingsRef;

    private ImageView userPhoto;
    private TextView profileName;
    private TextView userEmail;

    private TextView userRatingTextView;
    private TextView userRatingNumView;
    private ImageView userRatingStarView;

    private ImageView accountReputationIcon;
    private TextView accountReputationText;
    private ViewGroup accountOffersView;
    private ViewGroup accountRequestsView;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 103;

    public String uid;
    public String userId = getUid();
    public String facebookUserId = "";

    public Profile profile = getCurrentProfile();

    public MaterialDialog progressDialog;

    public AccountFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter_date);
        item.setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (view == null){
//            view = inflater.inflate(R.layout.fragment_account, container, false);
//        } else {
//            ((ViewGroup) view.getParent()).removeView(view);
//        }
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        uid = getActivity().getIntent().getExtras().getString("user_id");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("users").child(userId);

        userRatingsRef = mDatabase.child("users-ratings").child(userId);
        userRatingRef = userRatingsRef.child("rating");
        ratingsCountRef = userRatingsRef.child("ratingsCount");
        ratingsRef = userRatingsRef.child("ratings");

        //accountView = (ViewGroup) view.findViewById(R.id.profile);
        mAuth = FirebaseAuth.getInstance();
        facebookUserId = profile.getId();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        signOutFacebook = (Button) view.findViewById(R.id.sign_out_button);
        signOutFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignOutDialog();
            }
        });

        profileName = (TextView) getView().findViewById(R.id.profile_user_name);
        userPhoto = (ImageView) getView().findViewById(R.id.account_user_photo);
        userEmail = (TextView) getView().findViewById(R.id.profile_user_email) ;

        //userRatingTextView = (TextView) getView().findViewById(R.id.view_profile_rating_text);
        //userRatingNumView = (TextView) getView().findViewById(R.id.view_profile_rating_number);
        //userRatingStarView = (ImageView) getView().findViewById(R.id.view_profile_rating_stars);

        accountReputationIcon = (ImageView) getView().findViewById(R.id.account_reputation_icon);
        accountReputationText = (TextView) getView().findViewById(R.id.account_reputation_text);
        accountOffersView = (ViewGroup) getView().findViewById(R.id.account_offers_view);
        accountRequestsView = (ViewGroup) getView().findViewById(R.id.account_requests_view);


        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Loading account information")
                .content("Please wait")
                .progress(true, 0)
                .show();


        String photoUrl =  "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
        GlideUtil.loadProfileIcon(photoUrl, userPhoto);

        //String userRef = String.format("users/%s", uid);

        //DatabaseReference user_ref = database.getReference(userRef);
        userRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String value = String.valueOf(dataSnapshot.getValue());
                    loadStars(value);
                    //loadNumbers(value);
                   // userRatingTextView.setText("Reputation");
                    accountReputationText.setText("Reputation");
                    //userRatingNumView.setVisibility(View.VISIBLE);
                    //userRatingStarView.setVisibility(View.VISIBLE);

//                    userRatingNumView.setText(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                userEmail.setText(user.getEmail());
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



//        name_ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String data = dataSnapshot.getValue(String.class);
//                profileName.setText(data);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//        email_ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String data = dataSnapshot.getValue(String.class);
//                userEmail.setText(data);
//                progressDialog.dismiss();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.account_offers_icon:

                    break;
                case R.id.account_offers_view:
                    Intent offersIntent = new Intent(getActivity(), MyRideOffers.class);
                    //intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
                    startActivity(offersIntent);
                    break;

                case R.id.account_requests_view:
                    Intent requestsIntent = new Intent(getActivity(), MyRideRequests.class);
                    startActivity(requestsIntent);
                    break;

            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        accountOffersView.setOnClickListener(clickListener);
        accountRequestsView.setOnClickListener(clickListener);

    }

    @Override
    public void onStart(){
        super.onStart();
        checkConnection();

        userRatingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String value = String.valueOf(dataSnapshot.getValue());
                    loadStars(value);
                    accountReputationText.setText("Reputation");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onResume(){
        super.onResume();
//        ((MainActivity) getActivity())
//                .setActionBarTitle(getString(R.string.account_title));
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color = Color.WHITE;
        if (!(isConnected)) {
            accountView.setVisibility(View.GONE);
            message = "No connection";
            Snackbar snackbar = Snackbar
                    .make(getView().findViewById(R.id.fragment_account_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private CircleImageView imageView;

        public ImageLoadTask(String url, CircleImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }



    public void launchSignOutDialog(){
        new MaterialDialog.Builder(getActivity())
                .content("Sign Out of Sakay?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mAuth.signOut();
                        LoginManager.getInstance().logOut();
                        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    }
                })
                .show();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadStars(String value){
        if (value == "1"){
           // userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_one));
            accountReputationIcon.setImageDrawable(getResources().getDrawable(R.drawable.reputation_one));
        } else if (value == "2"){
           // userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_two));
            accountReputationIcon.setImageDrawable(getResources().getDrawable(R.drawable.reputation_two));
        } else if (value == "3"){
           // userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_three));
            accountReputationIcon.setImageDrawable(getResources().getDrawable(R.drawable.reputation_three));
        } else if (value == "4"){
           // userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_four));
            accountReputationIcon.setImageDrawable(getResources().getDrawable(R.drawable.reputation_four));
        } else if (value == "5"){
           // userRatingStarView.setImageDrawable(getResources().getDrawable(R.drawable.ratings_five));
            accountReputationIcon.setImageDrawable(getResources().getDrawable(R.drawable.reputation_five));
        }
    }

//    public void loadNumbers(String value){
//        if (value == "1"){
//            userRatingNumView.setText("1.0");
//        } else if (value == "2"){
//            userRatingNumView.setText("2.0");
//        } else if (value == "3"){
//            userRatingNumView.setText("3.0");
//        } else if (value == "4"){
//            userRatingNumView.setText("4.0");
//        } else if (value == "5"){
//            userRatingNumView.setText("5.0");
//        }
//    }
}
