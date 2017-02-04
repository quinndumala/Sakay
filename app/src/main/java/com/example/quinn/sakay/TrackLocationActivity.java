package com.example.quinn.sakay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.quinn.sakay.Models.Coordinates;
import com.example.quinn.sakay.Models.Sakay;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrackLocationActivity extends BaseActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener,
        RoutingListener{

    public static final String TAG = "TrackLocation";
    public static final String EXTRA_SAKAY_KEY = "sakay_key";
    public static final String EXTRA_OTHER_USER_ID = "other_user_id";
    public static final String EXTRA_OTHER_USER_NAME = "other_user_name";
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
    public DatabaseReference sakayRef;
    public DatabaseReference otherUserLocationRef;
    public String otherUserId;
    public String otherUserName;
    public LatLng otherUserLocation;
    public String userId = getUid();
    public MaterialDialog locationDialog;
    public MaterialDialog routeDialog;
    public Marker otherUserMarker;

    public LatLng startLocation;
    public LatLng endLocation;
    public Boolean isRecent = true;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.midGray_light,
            R.color.midGray_light,R.color.midGray_light,R.color.midGray_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= 21) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
            //lp.height = lp.height + getStatusBarHeight();
            //lp.setMargins(0, getStatusBarHeight(), 0, 0);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        polylines = new ArrayList<>();

        sakayKey = getIntent().getStringExtra(EXTRA_SAKAY_KEY);
        if (sakayKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        if (otherUserId == null){
            throw new IllegalArgumentException("Must pass EXTRA_OTHER_USER_ID");
        }
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        if (otherUserName == null){
            throw new IllegalArgumentException("Must pass EXTRA_OTHER_USER_NAME");
        }

        //Toast.makeText(TrackLocationActivity.this, "Sakay key: " + sakayKey, Toast.LENGTH_SHORT).show();

        try {
            MapsInitializer.initialize(this);
            mapView = (MapView) findViewById(R.id.map_track_location);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);


        } catch (InflateException e) {
            Log.e(TAG, "Inflate exception");
        }

        routeDialog = new MaterialDialog.Builder(TrackLocationActivity.this)
                .title("Fetching route information")
                .content("Please Wait")
                .progress(true, 0)
                .cancelable(false)
                .build();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        userCoordsRef = mRootRef.child("user-coordinates").child(userId);
        sakayRef = mRootRef.child("user-sakays").child(userId).child(sakayKey);
        otherUserLocationRef = mRootRef.child("user-coordinates").child(otherUserId);
        //Toast.makeText(this, "Sakay exists" + sakayKey, Toast.LENGTH_SHORT);

        otherUserLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Coordinates coordinates = dataSnapshot.getValue(Coordinates.class);
                otherUserLocation = new LatLng(coordinates.latitude, coordinates.longitude);
                showOtherUserLocation();
                if (checkTime(coordinates.timestamp)){
                    isRecent = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        sakayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Sakay sakay = dataSnapshot.getValue(Sakay.class);
                    startLocation = new LatLng(sakay.startLat, sakay.startLong);
                    endLocation = new LatLng(sakay.destinationLat, sakay.destinationLong);
                    routeDialog.show();
                    buildRoute();
                } else {
                    Toast.makeText(TrackLocationActivity.this, "Something went wrong. Try again",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        if(startLocation != null && endLocation != null){
//
//        }

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

    public void buildRoute(){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(startLocation, endLocation)
                .build();
        routing.execute();
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
        } else if (id == R.id.fab_user_location){
            showOtherUserLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
        checkLocationServices();

//        otherUserLocationRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Coordinates newCoords = dataSnapshot.getValue(Coordinates.class);
//                Toast.makeText(TrackLocationActivity.this, "Lat: " + newCoords.latitude + "Long: "
//                        + newCoords.longitude, Toast.LENGTH_SHORT).show();
//                //otherUserLocation = new LatLng(coordinates.latitude, coordinates.longitude);
//                //showOtherUserLocation();
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {}
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//
//        });

        otherUserLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Coordinates newCoords = dataSnapshot.getValue(Coordinates.class);
//                Toast.makeText(TrackLocationActivity.this, "Lat: " + newCoords.latitude + "Long: "
//                        + newCoords.longitude, Toast.LENGTH_SHORT).show();
                otherUserLocation = new LatLng(newCoords.latitude, newCoords.longitude);
                showOtherUserLocation();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void showOtherUserLocation(){
        if (otherUserLocation != null){
            BitmapDrawable bitmapdraw =(BitmapDrawable)getResources().getDrawable(R.drawable.user_location_marker);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 110, 110, false);

            if (otherUserMarker != null){
                otherUserMarker.remove();
            }
            //MarkerOptions position = new MarkerOptions().position(otherUserLocation);
            otherUserMarker = googleMap.addMarker(new MarkerOptions().position(otherUserLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    .title(otherUserName));
            //otherUserMarker.setPosition(otherUserLocation);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(otherUserLocation, 15.0f));
        }

    }

    public Boolean checkTime(Object timestamp){
        long currentTime = System.currentTimeMillis();
        long reportTime = (long) timestamp;
        long diff = currentTime - reportTime;

        Log.d(TAG, "Current Time: " + currentTime);
        Log.d(TAG, "Report Time: " + reportTime);
        Log.d(TAG, "Difference: " + diff);

        if (diff > 10 * 60 * 1000){
            return false;
        } else {
            return true;
        }

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
        //googleMap.clear();
        myLastLocation = location;

        saveUserCoordinates(location.getLatitude(), location.getLongitude());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    public void saveUserCoordinates(Double lat, Double lng){
        Coordinates coordinates = new Coordinates(lat, lng, ServerValue.TIMESTAMP);
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
        showSnack(isConnected);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setPadding(0, 160, 150, 0);

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

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        BitmapDrawable bitmapdrawA =(BitmapDrawable)getResources().getDrawable(R.drawable.marker_a);
        BitmapDrawable bitmapdrawB =(BitmapDrawable)getResources().getDrawable(R.drawable.marker_b);
        Bitmap a = bitmapdrawA.getBitmap();
        Bitmap b = bitmapdrawB.getBitmap();
        Bitmap markerA = Bitmap.createScaledBitmap(a, 75, 75, false);
        Bitmap markerB = Bitmap.createScaledBitmap(b, 75, 75, false);


        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(startLocation);
        options.icon(BitmapDescriptorFactory.fromBitmap(markerA));
        options.title("Pickup Location");
        googleMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(endLocation);
        options.icon(BitmapDescriptorFactory.fromBitmap(markerB));
        options.title("Destination");
        googleMap.addMarker(options);

        routeDialog.dismiss();
        if (isRecent){
            showNotRecentDialog();
        }

    }

    @Override
    public void onRoutingCancelled() {
        Log.i(TAG, "Routing was cancelled.");
    }

    public void showNotRecentDialog(){
        new MaterialDialog.Builder(TrackLocationActivity.this)
                .title("Notice")
                .content("The user you are tracking has not used Sakay for the past 10 minutes. The " +
                        "latest recorded information will be used but might not be accurate as a result.")
                .positiveText("OK")
                .show();
    }
}
