package com.example.quinn.sakay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InflateException;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.Coordinates;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

public class TrackLocationActivity extends BaseActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener{

    public static final String TAG = "TrackLocation";
    public static final String EXTRA_SAKAY_KEY = "sakay_key";
    private MapView mapView;
    private GoogleMap googleMap;
    private String sakayKey;

    public Location location;
    private Location myLastLocation;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabUserLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean isInFront;

    public DatabaseReference mRootRef;
    public DatabaseReference userCoordsRef;
    public String userId = getUid();
    public MaterialDialog locationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        if (Build.VERSION.SDK_INT >= 21) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
            lp.setMargins(0, getStatusBarHeight(), 0, 0);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sakayKey = getIntent().getStringExtra(EXTRA_SAKAY_KEY);
        if (sakayKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        try {
            MapsInitializer.initialize(this);
            mapView = (MapView) findViewById(R.id.map_track_location);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);


        } catch (InflateException e) {
            Log.e(TAG, "Inflate exception");
        }

        fabMyLocation = (FloatingActionButton) findViewById(R.id.fab_my_location);
        fabUserLocation = (FloatingActionButton) findViewById(R.id.fab_user_location);

        locationDialog = new MaterialDialog.Builder(TrackLocationActivity.this)
                .title("Location not found")
                .content("Please turn on location services to continue using Sakay")
                .positiveText("OK")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .build();

        fabMyLocation.setOnClickListener(this);
        fabUserLocation.setOnClickListener(this);

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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

        if (id == R.id.fab_my_location) {
            ZoomToMyLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
        checkLocationServices();
    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
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
        isInFront = true;

        mapView.onResume();
        MyApplication.getInstance().setConnectivityListener(this);

    }

    @Override
    public void onLocationChanged(Location location) {
        googleMap.clear();
        myLastLocation = location;

        //saveUserCoordinates(location.getLatitude(), location.getLongitude());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    public void saveUserCoordinates(Double lat, Double lng){
        Coordinates coordinates = new Coordinates(lat, lng);
        userCoordsRef.setValue(coordinates);
    }

    private void ZoomToMyLocation() {
        if (myLastLocation != null){
            LatLng latLng = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        }

    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        locationDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (isInFront){
            turnOnLocationDialog();
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(true);
            //To setup location manager
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //To request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
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
                    .make(findViewById(R.id.activity_track_location), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    private void checkLocationServices(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            turnOnLocationDialog();
        }
    }

    private void turnOnLocationDialog(){
        locationDialog.show();
    }

}
