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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookSdk;
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

//import static android.support.v4.widget.EdgeEffectCompatIcs.finish;

//import com.google.firebase.quickstart.database.models.Post;
//import com.google.firebase.quickstart.database.models.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment
        implements
        GoogleApiClient.OnConnectionFailedListener,
        ConnectivityReceiver.ConnectivityReceiverListener{

    private View view;
    private Button signOutFacebook;
   // private Firebase db;
    private FirebaseAuth mAuth;
    //private DatabaseReference mDatabase;
    private CircleImageView profilePhoto;
    private TextView profileName;
    private TextView userEmail;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 103;


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null){
            view = inflater.inflate(R.layout.fragment_account, container, false);
        } else {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        //db = new Firebase("https://sakay-2af91.firebaseio.com/users/");
        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        checkConnection();
        FacebookSdk.sdkInitialize(getApplicationContext());
//        profileName = (TextView) view.findViewById(R.id.profile_user_name);
//        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_user_photo);
        signOutFacebook = (Button) view.findViewById(R.id.sign_out_button);
        signOutFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                getActivity().startActivity(intent);
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        profileName = (TextView) getView().findViewById(R.id.profile_user_name);
        profilePhoto = (CircleImageView) getView().findViewById(R.id.profile_user_photo);
        userEmail = (TextView) getView().findViewById(R.id.profile_user_email) ;

        String uid = getActivity().getIntent().getExtras().getString("user_id");
        String imageUrl = getActivity().getIntent().getExtras().getString("profile_picture");
        String photoUrl = "https://graph.facebook.com/" + uid + "/picture?";

        String nameRef = String.format("users/%s/name", uid);
        String emailRef = String.format("users/%s/email", uid);

        DatabaseReference name_ref = database.getReference(nameRef);
        DatabaseReference email_ref = database.getReference(emailRef);

        //new ImageLoadTask(imageUrl, profilePhoto).execute();
        GlideUtil.loadProfileIcon(imageUrl, profilePhoto);

        name_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                profileName.setText(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        email_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                userEmail.setText(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

//        db.child(uid).child("name").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String data = dataSnapshot.getValue(String.class);
//                profileName.setText(data);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//
//        db.child(uid).child("email").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String emailData = dataSnapshot.getValue(String.class);
//                userEmail.setText(emailData);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onResume(){
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.account_title));
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
}
