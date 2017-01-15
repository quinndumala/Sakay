package com.example.quinn.sakay;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.Vehicle;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment
        implements ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener{

    private static final String TAG = "SettingsFragment";
    private static final String REQUIRED = "Required";
    private static final int REQUEST_PLACE_PICKER = 1;

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private DatabaseReference phoneRef;
    private DatabaseReference homeAddressRef;
    private DatabaseReference homeAddressIdRef;
    private DatabaseReference workAddressRef;
    private DatabaseReference workAddressIdRef;
    private DatabaseReference vehicleRef;

    private ValueEventListener mPhoneListener;

    private final String userId = getUid();

    private ViewGroup mobileNumberView;
    private ViewGroup homeAddressView;
    private ViewGroup workAddressView;
    private ViewGroup vehicleView;

    private TextView mobileNumberText;
    private TextView homeAddressText;
    private TextView workAddressText;
    private TextView vehicleText;

    private EditText EditVehicleType;
    private EditText EditVehicleModel;
    private EditText EditVehicleColor;
    private EditText EditPlateNo;
    private View positiveAction;

    public String homeOrWork;
    public Boolean vehicleExists = false;
    public String currentPhoneNumber = "";
    public String currentVehicleType;
    public String currentVehicleModel;
    public String currentVehicleColor;
    public String currentVehiclePlateNo;

    public MaterialDialog progressDialog;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        phoneRef = mDatabase.child("users-settings").child(userId).child("phone");
        homeAddressRef = mDatabase.child("users-settings").child(userId).child("home");
        homeAddressIdRef = mDatabase.child("users-settings").child(userId).child("homeId");
        workAddressRef = mDatabase.child("users-settings").child(userId).child("work");
        workAddressIdRef = mDatabase.child("users-settings").child(userId).child("workId");
        vehicleRef = mDatabase.child("users-settings").child(userId).child("vehicle");

        mobileNumberView = (ViewGroup) rootView.findViewById(R.id.settings_phone_number);
        homeAddressView = (ViewGroup) rootView.findViewById(R.id.settings_home_address);
        workAddressView = (ViewGroup) rootView.findViewById(R.id.settings_work_address);
        vehicleView = (ViewGroup) rootView.findViewById(R.id.settings_vehicle);

        mobileNumberText = (TextView) rootView.findViewById(R.id.settings_phone_number_text);
        homeAddressText = (TextView) rootView.findViewById(R.id.settings_home_address_text);
        workAddressText = (TextView) rootView.findViewById(R.id.settings_work_address_text);
        vehicleText = (TextView) rootView.findViewById(R.id.settings_vehicle_text);

        mobileNumberView.setOnClickListener(this);
        homeAddressView.setOnClickListener(this);
        workAddressView.setOnClickListener(this);
        vehicleView.setOnClickListener(this);

        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Loading map")
                .content("Please wait")
                .progress(true, 0)
                .build();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkConnection();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener phoneListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentPhoneNumber = dataSnapshot.getValue(String.class);
                    mobileNumberText.setText(currentPhoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPhoneNumber:onCancelled", databaseError.toException());
            }
        };

        ValueEventListener vehicleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    vehicleExists = true;
                    Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);
                    currentVehicleType = vehicle.vehicleType;
                    currentVehicleColor = vehicle.vehicleColor;
                    currentVehicleModel = vehicle.vehicleModel;
                    currentVehiclePlateNo = vehicle.plateNo;
                    String text = currentVehicleColor + " " + currentVehicleModel + ", " + currentVehiclePlateNo;
                    vehicleText.setText(text);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPhoneNumber:onCancelled", databaseError.toException());
            }
        };

        ValueEventListener homeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String homeAddress = dataSnapshot.getValue(String.class);
                    homeAddressText.setText(homeAddress);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPhoneNumber:onCancelled", databaseError.toException());
            }
        };

        ValueEventListener workListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String workAddress = dataSnapshot.getValue(String.class);
                    workAddressText.setText(workAddress);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        phoneRef.addValueEventListener(phoneListener);
        homeAddressRef.addValueEventListener(homeListener);
        workAddressRef.addValueEventListener(workListener);
        vehicleRef.addValueEventListener(vehicleListener);

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.settings_phone_number) {
                launchInputNumber();
            } else if (id == R.id.settings_home_address) {
                homeOrWork = "home";
                homeAddressView.setEnabled(false);
                progressDialog.show();
                launchPlacePicker();
                homeAddressView.setEnabled(true);
            } else if (id == R.id.settings_work_address) {
                homeOrWork = "work";
                workAddressView.setEnabled(false);
                progressDialog.show();
                launchPlacePicker();
                workAddressView.setEnabled(true);
            } else if (id == R.id.settings_vehicle) {
                launchInputVehicle();
            }

    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Settings");

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

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
                    .make(getView().findViewById(R.id.fragment_sakays_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void launchInputNumber(){
        new MaterialDialog.Builder(getActivity())
                .title("Your phone number")
                .positiveText("ok")
                .negativeText("cancel")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input(null, currentPhoneNumber, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        //showToast(input.toString());
                        updateUserPhoneNumber(input.toString());
                    }
                }).show();
    }

    public void launchInputVehicle(){
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Your vehicle details")
                .customView(R.layout.item_vehicle, true)
                .positiveText("Save")
                .negativeText("cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        updateUserVehicle(EditVehicleType.getText().toString(),
                                EditVehicleModel.getText().toString(),
                                EditVehicleColor.getText().toString(),
                                EditPlateNo.getText().toString());
                    }
                })
                .build();
        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        EditVehicleType = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_type);
        EditVehicleModel = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_model);
        EditVehicleColor = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_color);
        EditPlateNo = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_plate_no);

        if (vehicleExists){
            EditVehicleType.setText(currentVehicleType);
            EditVehicleModel.setText(currentVehicleModel);
            EditVehicleColor.setText(currentVehicleColor);
            EditPlateNo.setText(currentVehiclePlateNo);
        }
        dialog.show();

    }



    public void showToast(String vehicleTYpe, String vehicleModel, String vehicleColor, String plateNo){
        String string = vehicleTYpe + ", " + vehicleModel + ", " + vehicleColor + ", " + plateNo;
        Toast.makeText(getActivity(), string , Toast.LENGTH_SHORT).show();
    }

    public void updateUserPhoneNumber(String number){
        phoneRef.setValue(number);
    }

    public void updateUserVehicle(String vehicleTYpe, String vehicleModel, String vehicleColor,
                                  String plateNo){
        Vehicle vehicle = new Vehicle(vehicleTYpe, vehicleModel, vehicleColor, plateNo);
        vehicleRef.setValue(vehicle);
    }

    public void launchPlacePicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());

            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 450);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                String attribution = PlacePicker.getAttributions(data);
                if(attribution == null){
                    attribution = "";
                }

                String location = name.toString();
                if (location.contains("°")){
                    location = address.toString();
                    launchAlertDialog();
                } else {
                    if(homeOrWork == "home"){
                        homeAddressRef.setValue(location);
                        homeAddressIdRef.setValue(placeId);
                    } else if(homeOrWork == "work"){
                        workAddressRef.setValue(location);
                        workAddressIdRef.setValue(placeId);
                    }

                    Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");
                }



            } else {
                // User has not selected a place, hide the card.
                //fStart.setText(R.string.select_location);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void launchAlertDialog(){
        new MaterialDialog.Builder(getActivity())
                .title(R.string.ambiguous_location_title)
                .content(R.string.ambiguous_location_body)
                .positiveText("OK")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchPlacePicker();
                    }
                })
                .show();
    }



}
