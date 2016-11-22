package com.example.quinn.sakay;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        TrafficFragment.OnFragmentInteractionListener,
        SakaysFragment.OnFragmentInteractionListener,
        RideOffersFragment.OnFragmentInteractionListener,
        RideRequestsFragment.OnFragmentInteractionListener,
        BlankFragment.OnFragmentInteractionListener{

   //SupportMapFragment sMapFragment;

//    /**
//     * Request code for location permission request.
//     *
//     * @see #onRequestPermissionsResult(int, String[], int[])
//     */
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
//
//    /**
//     * Flag indicating whether a requested permission has been denied after returning in
//     * {@link #onRequestPermissionsResult(int, String[], int[])}.
//     */
//    private boolean mPermissionDenied = false;
//
//    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = null;
            fragmentClass = TrafficFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Report Traffic", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;


        if (id == R.id.nav_traffic) {
//            TrafficFragment trafficFragment = new TrafficFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    trafficFragment,
//                    trafficFragment.getTag()
//            ).commit();

            fragmentClass = TrafficFragment.class;

        } else if (id == R.id.nav_sakays) {
            fragmentClass = SakaysFragment.class;

        } else if (id == R.id.nav_rideRequests) {
            fragmentClass = RideRequestsFragment.class;
        } else if (id == R.id.nav_rideOffers) {
            fragmentClass = RideOffersFragment.class;
        } else if (id == R.id.nav_account) {
            fragmentClass = BlankFragment.class;
            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            fragmentClass = BlankFragment.class;
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_helpAndSupport) {
            fragmentClass = BlankFragment.class;
            Toast.makeText(this, "Help and Support", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_aboutSakay) {
            fragmentClass = BlankFragment.class;
            Toast.makeText(this, "About Sakay", Toast.LENGTH_SHORT).show();
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

}

