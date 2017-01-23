package com.example.quinn.sakay;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.quinn.sakay.R.id.map_traffic;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrafficFragment extends Fragment
        implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ConnectivityReceiver.ConnectivityReceiverListener {

    private View rootView;
    private static final String TAG = "TrafficFragment";

    private MapView mapView;
    private GoogleMap googleMap;

    //private static LocationService instance = null;
    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;

    private FloatingActionMenu menuTraffic;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private Handler mUiHandler = new Handler();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

//
//    private static final double lat = 9.306840;
//    private static final double lon = 123.305447;




    public TrafficFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.fragment_traffic, container, false);
            MapsInitializer.initialize(this.getActivity());
            mapView = (MapView) rootView.findViewById(map_traffic);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);


        } catch (InflateException e) {
            Log.e(TAG, "Inflate exception");
        }


        return rootView;
//        View view = inflater.inflate(R.layout.fragment_traffic, container, false);
//        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        menuTraffic = (FloatingActionMenu) view.findViewById(R.id.fabTraffic);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fabLight);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fabModerate);
        fab3 = (FloatingActionButton) view.findViewById(R.id.fabHeavy);

        menuTraffic.setClosedOnTouchOutside(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fab1.setOnClickListener(clickListener);
        fab2.setOnClickListener(clickListener);
        fab3.setOnClickListener(clickListener);
        menuTraffic.hideMenuButton(false);
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                menuTraffic.showMenuButton(true);
                menuTraffic.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fab_scale_up));
                menuTraffic.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fab_scale_down));
            }
        }, 350);
        createCustomAnimation();

       // setRetainInstance(true);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter_date);
        item.setVisible(false);
    }

    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menuTraffic.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menuTraffic.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menuTraffic.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menuTraffic.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                menuTraffic.getMenuIconView().setImageResource(menuTraffic.isOpened()
                        ? R.drawable.ic_broadcast : R.drawable.ic_close);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        menuTraffic.setIconToggleAnimatorSet(set);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fabLight:
                    Toast.makeText(getActivity(), "Traffic Successfully Reported", Toast.LENGTH_SHORT).show();
                    menuTraffic.toggle(true);
                    break;
                case R.id.fabModerate:
                    Toast.makeText(getActivity(), "Traffic Successfully Reported", Toast.LENGTH_SHORT).show();
                    menuTraffic.toggle(true);
                    break;
                case R.id.fabHeavy:
                    Toast.makeText(getActivity(), "Traffic Successfully Reported", Toast.LENGTH_SHORT).show();
                    menuTraffic.toggle(true);
                    break;
            }
        }
    };



//    private void setupMapIfNeeded{
//        if(googleMap == null){
//
//        }
//    }

//    private boolean isGooglePlayServicesAvailable() {
//        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (ConnectionResult.SUCCESS == status) {
//            return true;
//        } else {
//            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
//            return false;
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        checkConnection();

    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set title bar
//        ((MainActivity) getActivity())
//                .setActionBarTitle("Traffic");

//        if (mPermissionDenied) {
//            // Permission was not granted, display error dialog.
//            showMissingPermissionError();
//            mPermissionDenied = false;
//        }
        mapView.onResume();
        MyApplication.getInstance().setConnectivityListener(this);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        //googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(TrafficFragment.this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) TrafficFragment.this.getActivity(),
                    LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(true);
            //To setup location manager
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            //To request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        }
    }


    //   @Override
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
//        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    public void onLocationChanged(Location location) {
        //To clear map data
        googleMap.clear();

        //To hold location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //To create marker in map
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("My Location");
        //adding marker to the map
        //googleMap.addMarker(markerOptions);

        //opening position with some zoom level in the map
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
                    .make(getView().findViewById(R.id.fragment_traffic_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

//    private Location getMyLocation() {
//        // Get location from GPS if it's available
//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        // Location wasn't found, check the next most accurate place for the current location
//        if (myLocation == null) {
//            Criteria criteria = new Criteria();
//            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//            // Finds a provider that matches the criteria
//            String provider = lm.getBestProvider(criteria, true);
//            // Use the provider to get the last known location
//            myLocation = lm.getLastKnownLocation(provider);
//        }
//
//        return myLocation;
//    }

//    public void loadMap(){
//        try {
//            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            Criteria criteria = new Criteria();
//
//            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
//            if (location != null)
//            {
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude());
//
//                CameraPosition cameraPosition = new CameraPosition.Builder()
//                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//                        .zoom(17)                   // Sets the zoom
//                        .bearing(90)                // Sets the orientation of the camera to east
//                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                        .build();                   // Creates a CameraPosition from the builder
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            }
//
//        } catch (Exception e){
//            Toast.makeText(getActivity(), "Problem with permissions", Toast.LENGTH_SHORT);
//            Log.d(TAG, "Excelption: " + e);
//        }
//
//    }
}
