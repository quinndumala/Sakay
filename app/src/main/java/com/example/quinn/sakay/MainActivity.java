package com.example.quinn.sakay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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


public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        TrafficFragment.OnFragmentInteractionListener,
        SakaysFragment.OnFragmentInteractionListener,
        RideOffersFragment.OnFragmentInteractionListener,
        RideRequestsFragment.OnFragmentInteractionListener,
        AccountFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        BlankFragment.OnFragmentInteractionListener,
        ConnectivityReceiver.ConnectivityReceiverListener
{
    //private Firebase db;
    private FirebaseAuth mAuth;
    private CircleImageView navProfilePhoto;
    private TextView navProfileName;
    private TextView navUserEmail;
    public MaterialDialog progressDialog;
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    String title = "Sakay";

//    FragmentTransaction ft;
//    FragmentManager fm;
//    TrafficFragment trafficFragment = new TrafficFragment();
//    SakaysFragment sakaysFragment = new SakaysFragment();
//    RideOffersFragment offersFragment = new RideOffersFragment();
//    RideRequestsFragment requestsFragment = new RideRequestsFragment();
//    AccountFragment accountFragment = new AccountFragment();
//    SettingsFragment settingsFragment = new SettingsFragment();
//    BlankFragment helpFragment = new BlankFragment();
//    BlankFragment aboutFragment = new BlankFragment();
//
//    private Fragment[] fragments = new Fragment[] {trafficFragment, sakaysFragment,
//            offersFragment, requestsFragment, accountFragment, settingsFragment,
//            helpFragment, aboutFragment};
//
//    String[] fragmentTAGS = new String[]{"traffic_tag","sakays_tag","offers_tag",
//            "requests_tag","account_tag", "settings_tag", "help_tag", "about_tag"};

    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading account information")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

//        if (savedInstanceState == null) {
//            Fragment fragment = null;
//            Class fragmentClass = null;
//            fragmentClass = TrafficFragment.class;
//            try {
//                fragment = (Fragment) fragmentClass.newInstance();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();
//        }

        if (savedInstanceState == null){
            fragmentManager.beginTransaction().add(R.id.content_main, new TrafficFragment(), "traffic").commit();
            setTitle("Traffic");
        }

        navProfilePhoto = (CircleImageView) header.findViewById(R.id.nav_user_photo);
        navProfileName = (TextView) header.findViewById(R.id.nav_user_name);
        //navUserEmail = (TextView) header.findViewById(R.id.nav_email);

        String uid = getIntent().getExtras().getString("user_id");
        String imageUrl = getIntent().getExtras().getString("profile_picture");

        GlideUtil.loadProfileIcon(imageUrl, navProfilePhoto);

        String nameRef = String.format("users/%s/name", uid);
        String emailRef = String.format("users/%s/email", uid);
        DatabaseReference name_ref = database.getReference(nameRef);
        DatabaseReference email_ref = database.getReference(emailRef);
        name_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                navProfileName.setText(data);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
//        email_ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String data = dataSnapshot.getValue(String.class);
//                navUserEmail.setText(data);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_notifications){
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_filter_date) {

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = null;
        Class fragmentClass = null;


        if (id == R.id.nav_traffic) {
            fragmentClass = TrafficFragment.class;
            title = "Traffic";

            if(fragmentManager.findFragmentByTag("traffic") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("traffic")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new TrafficFragment(), "traffic").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_sakays) {
            fragmentClass = SakaysFragment.class;
            title = "Sakays";

            if(fragmentManager.findFragmentByTag("sakays") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("sakays")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new SakaysFragment(), "sakays").commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_rideRequests) {
            fragmentClass = RideRequestsFragment.class;
            title = "Ride Requests";

            if(fragmentManager.findFragmentByTag("requests") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("requests")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new RideRequestsFragment(), "requests").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_rideOffers) {
            fragmentClass = RideOffersFragment.class;
            title = "Ride Offers";

            if(fragmentManager.findFragmentByTag("offers") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("offers")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new RideOffersFragment(), "offers").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_account) {
            fragmentClass = AccountFragment.class;
            title = "Account";

            if(fragmentManager.findFragmentByTag("account") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("account")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new AccountFragment(), "account").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_settings) {
            fragmentClass = SettingsFragment.class;
            title = "Settings";

            if(fragmentManager.findFragmentByTag("settings") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("settings")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new SettingsFragment(), "settings").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_helpAndSupport) {
            fragmentClass = BlankFragment.class;
            Toast.makeText(this, "Help and Support", Toast.LENGTH_SHORT).show();
            title = "Sakay";

            if(fragmentManager.findFragmentByTag("help") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("help")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new BlankFragment(), "help").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
            if(fragmentManager.findFragmentByTag("about") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("about")).commit();
            }

        } else if (id == R.id.nav_aboutSakay) {
            fragmentClass = BlankFragment.class;
            title = "Sakay";

            if(fragmentManager.findFragmentByTag("about") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("about")).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.content_main, new BlankFragment(), "about").commit();
            }
            if(fragmentManager.findFragmentByTag("sakays") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("sakays")).commit();
            }
            if(fragmentManager.findFragmentByTag("offers") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("offers")).commit();
            }
            if(fragmentManager.findFragmentByTag("requests") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("requests")).commit();
            }
            if(fragmentManager.findFragmentByTag("account") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("account")).commit();
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")).commit();
            }
            if(fragmentManager.findFragmentByTag("help") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("help")).commit();
            }
            if(fragmentManager.findFragmentByTag("traffic") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("traffic")).commit();
            }
        }

//        try {
//            fragment = (Fragment) fragmentClass.newInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Fragment containerFragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (!(containerFragment.getClass().getName().equalsIgnoreCase(fragment.getClass().getName()))){
//            transaction.replace(R.id.content_main, fragment);
//   //         transaction.addToBackStack(null);
//            transaction.commit();
//        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setActionBarTitle(title);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
//    @Override
//    public void onMapReady(GoogleMap map) {
//        mMap = map;
//
//        mMap.setOnMyLocationButtonClickListener(this);
//        enableMyLocation();
//    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Method to manually check connection status
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
                    .make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
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

