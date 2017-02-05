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
import com.example.quinn.sakay.Models.RideOffer;
import com.example.quinn.sakay.Models.RideRequest;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

public class ViewMapActivity extends BaseActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener,
        RoutingListener {

    public static final String TAG = "ViewMap";
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_POST_TYPE = "post_type";

    private MapView mapView;
    private GoogleMap googleMap;
    public Location location;
    private Location myLastLocation;

    private FloatingActionButton fabMyLocation;

    public String postKey;
    public String postType;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean isInFront;

    public DatabaseReference mRootRef;
    public DatabaseReference userCoordsRef;
    public DatabaseReference postRef;
    public DatabaseReference offersRef;
    public DatabaseReference requestsRef;

    public Boolean postFound = false;

    public LatLng startLocation;
    public LatLng destination;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.midGray_light,
            R.color.midGray_light,R.color.midGray_light,R.color.midGray_light};

    public String userId = getUid();

    public MaterialDialog locationDialog;
    public MaterialDialog routeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);

//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (postKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        mRootRef = FirebaseDatabase.getInstance().getReference();
        offersRef = mRootRef.child("rideOffers").child(postKey);
        requestsRef = mRootRef.child("rideRequests").child(postKey);
        userCoordsRef = mRootRef.child("user-coordinates").child(userId);

        polylines = new ArrayList<>();

        routeDialog = new MaterialDialog.Builder(ViewMapActivity.this)
                .title("Fetching route information")
                .content("Please Wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        postType = getIntent().getStringExtra(EXTRA_POST_TYPE);
        if (postType == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_TYPE");
        } else if (postType.equals("offer")) {
            checkOffers();
        } else {
            checkRequests();
        }

        try {
            MapsInitializer.initialize(this);
            mapView = (MapView) findViewById(R.id.map_view_map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);


        } catch (InflateException e) {
            Log.e(TAG, "Inflate exception");
        }

        locationDialog = new MaterialDialog.Builder(ViewMapActivity.this)
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

        fabMyLocation = (FloatingActionButton) findViewById(R.id.fab_map_my_location);
        fabMyLocation.setOnClickListener(this);
    }

    public void checkOffers(){
        offersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postFound = true;
                    RideOffer rideOffer = dataSnapshot.getValue(RideOffer.class);
                    startLocation = new LatLng(rideOffer.startLat, rideOffer.startLong);
                    destination = new LatLng(rideOffer.destinationLat, rideOffer.destinationLong);
                    buildRoute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkRequests(){
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    postFound = true;
                    RideRequest rideRequest = dataSnapshot.getValue(RideRequest.class);
                    startLocation = new LatLng(rideRequest.startLat, rideRequest.startLong);
                    destination = new LatLng(rideRequest.destinationLat, rideRequest.destinationLong);
                    buildRoute();

                } else {
                    Log.e(TAG, "Post not found");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void buildRoute(){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(startLocation, destination)
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

        if (id == R.id.fab_map_my_location) {
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
        myLastLocation = location;
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
//        CameraUpdate center = CameraUpdateFactory.newLatLng(startLocation);
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
//
//        googleMap.moveCamera(center);
        routeDialog.dismiss();

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15.0f));

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
        options.position(destination);
        options.icon(BitmapDescriptorFactory.fromBitmap(markerB));
        options.title("Destination");
        googleMap.addMarker(options);

    }

    @Override
    public void onRoutingCancelled() {
        Log.i(TAG, "Routing was cancelled.");
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
        googleMap.setPadding(0, 160, 0, 0);

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
            //googleMap.setTrafficEnabled(true);
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
                    .make(findViewById(R.id.activity_view_map), message, Snackbar.LENGTH_INDEFINITE);

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
