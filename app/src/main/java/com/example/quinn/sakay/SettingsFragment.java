package com.example.quinn.sakay;


import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment
        implements ConnectivityReceiver.ConnectivityReceiverListener,
        View.OnClickListener{

    private static final String TAG = "SettingsFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private final String userId = getUid();

    private ViewGroup mobileNumberView;
    private ViewGroup homeAddressView;
    private ViewGroup workAddressView;
    private ViewGroup vehicleView;

    private EditText vehicleType;
    private EditText vehicleModel;
    private EditText vehicleColor;
    private EditText plateNo;
    private View positiveAction;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mobileNumberView = (ViewGroup) rootView.findViewById(R.id.settings_phone_number);
        homeAddressView = (ViewGroup) rootView.findViewById(R.id.settings_home_address);
        workAddressView = (ViewGroup) rootView.findViewById(R.id.settings_work_address);
        vehicleView = (ViewGroup) rootView.findViewById(R.id.settings_vehicle);

        mobileNumberView.setOnClickListener(this);
        homeAddressView.setOnClickListener(this);
        workAddressView.setOnClickListener(this);
        vehicleView.setOnClickListener(this);
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
    public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.settings_phone_number) {
                launchInputNumber();
            } else if (id == R.id.settings_home_address) {
                Toast.makeText(getActivity(), "change home address", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.settings_work_address) {

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
                .title("Enter your phone number")
                .positiveText("ok")
                .negativeText("cancel")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        //showToast(input.toString());
                        updateUserPhoneNumber(input.toString());
                    }
                }).show();
    }

    public void launchInputVehicle(){
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Enter vehicle details")
                .customView(R.layout.item_vehicle, true)
                .positiveText("Save")
                .negativeText("cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        updateUserVehicle(vehicleType.getText().toString(), vehicleModel.getText().toString(),
                                vehicleColor.getText().toString(), plateNo.getText().toString());
                    }
                })
                .build();
        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        vehicleType = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_type);
        vehicleModel = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_model);
        vehicleColor = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_color);
        plateNo = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_plate_no);
        dialog.show();

    }



    public void showToast(String vehicleTYpe, String vehicleModel, String vehicleColor, String plateNo){
        String string = vehicleTYpe + ", " + vehicleModel + ", " + vehicleColor + ", " + plateNo;
        Toast.makeText(getActivity(), string , Toast.LENGTH_SHORT).show();
    }

    public void updateUserPhoneNumber(String number){
        mDatabase.child("users").child(userId).child("phone").setValue(number);
    }

    public void updateUserVehicle(String vehicleTYpe, String vehicleModel, String vehicleColor,
                                  String plateNo){
        Vehicle vehicle = new Vehicle(vehicleTYpe, vehicleModel, vehicleColor, plateNo);
        mDatabase.child("users").child(userId).child("vehicle").setValue(vehicle);
    }
}
