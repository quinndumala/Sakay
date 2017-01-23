package com.example.quinn.sakay;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.RideRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.simplicityapks.reminderdatepicker.lib.DateSpinner;
import com.simplicityapks.reminderdatepicker.lib.OnDateSelectedListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class RideRequestsFragment extends Fragment
    implements ConnectivityReceiver.ConnectivityReceiverListener{
    private static final String TAG = "RideRequestsFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder> filterAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FloatingActionButton addRideRequest;
    private View positiveAction;
    private TextView noRequestsView;
    private DateSpinner filterDate;
    public String dateAndTime = "";
    public Long filterTime;
    public Query allPostsQuery;

    public RideRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_notifications){
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_filter_date) {
            //Toast.makeText(getActivity(), "Filter date", Toast.LENGTH_SHORT).show();
            launchFilterPosts();
        }

        return super.onOptionsItemSelected(item);
    }

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ride_requests, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.rideRequest_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noRequestsView = (TextView) view.findViewById(R.id.no_requests_text);
        addRideRequest = (FloatingActionButton) view.findViewById(R.id.fabRideRequests);
        addRideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddRideRequest.class);
                getActivity().startActivity(intent);
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        checkConnection();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addRideRequest.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addRideRequest.show(true);
                addRideRequest.setShowAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.show_from_bottom));
                addRideRequest.setHideAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.hide_to_bottom));
            }
        }, 300);

        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 && addRideRequest.isShown())
                    addRideRequest.hide(true);
                else if (dy<0)
                    addRideRequest.show(true);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                //super.onScrollStateChanged(recyclerView, newState);
            }
        });

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        /*
        Set up FirebaseRecyclerAdapter with the Query
        TODO: Check if database node has values first and set a condition
        */
        getAllPosts();
    }

//    private Query getQuery(DatabaseReference mDatabase) {
//        return mDatabase;
//    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("rideRequests")
                .limitToFirst(100);

        return recentPostsQuery;
    }

    public Query filterQuery(DatabaseReference databaseReference){
        long endTime = filterTime + TimeUnit.HOURS.toMillis(24);
        Log.d(TAG, "End Time: " + endTime);
        Query filterQuery = databaseReference.child("rideRequests").orderByChild("timeStamp").startAt(filterTime)
                .endAt(endTime);
        return filterQuery;
    }

    public void isQueryEmpty(Query query){
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    noRequestsView.setVisibility(View.VISIBLE);
                } else {
                    noRequestsView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getAllPosts(){
        allPostsQuery = getQuery(mDatabase);
        isQueryEmpty(allPostsQuery);
        mAdapter = new FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder>(RideRequest.class,
                R.layout.item_ride_request, RideRequestViewHolder.class, allPostsQuery) {
            @Override
            protected void populateViewHolder(final RideRequestViewHolder viewHolder, final RideRequest model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), RideRequestDetailActivity.class);
                        intent.putExtra(RideRequestDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });
                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public void updatePosts(){
        Query filter = filterQuery(mDatabase);
        isQueryEmpty(filter);
        filterAdapter = new FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder>(RideRequest.class,
                R.layout.item_ride_request, RideRequestViewHolder.class, filter) {
            @Override
            protected void populateViewHolder(final RideRequestViewHolder viewHolder, final RideRequest model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), RideRequestDetailActivity.class);
                        intent.putExtra(RideRequestDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(filterAdapter);
    }

    public void onResume(){
        super.onResume();

        // Set title bar
//        ((MainActivity) getActivity())
//                .setActionBarTitle("Ride Requests");
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mAdapter != null) {
//            mAdapter.cleanup();
//        }
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
            addRideRequest.setVisibility(View.GONE);
            message = "No connection";
            Snackbar snackbar = Snackbar
                    .make(getView().findViewById(R.id.fragment_ride_requests_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void launchFilterPosts(){
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Select date to filter ride requests")
                .customView(R.layout.item_date_picker, true)
                .autoDismiss(false)
                .positiveText("OK")
                .negativeText("cancel")
                .neutralText("show all")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getActivity(), "Searching", Toast.LENGTH_SHORT).show();
                        updatePosts();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback(){
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //getAllPosts();
                        mRecycler.setAdapter(mAdapter);
                        isQueryEmpty(allPostsQuery);
                        dialog.dismiss();
                    }
                })
                .build();
        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        filterDate = (DateSpinner) dialog.getCustomView().findViewById(R.id.filter_date_picker);
        filterDate.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar date) {
                String selectedDate = getDateFormat().format(date.getTime());
                dateAndTime = selectedDate;
                filterTime = date.getTimeInMillis();
                Log.d(TAG, "Selected date: " + selectedDate);
                Log.d(TAG, "Timestamp: " + filterTime);
            }
        });

        dialog.show();
    }

}
