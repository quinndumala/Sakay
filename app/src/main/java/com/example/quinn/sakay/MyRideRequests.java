package com.example.quinn.sakay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.quinn.sakay.Models.RideRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;

public class MyRideRequests extends BaseActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener{
    private static final String TAG = "MyRideRequests";

    private DatabaseReference mDatabase;
    private DatabaseReference notifCheckRef;

    private FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<RideRequest, RideRequestViewHolder> filterAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    private TextView noRequestsView;

    public Query allPostsQuery;

    public String userId = getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ride_requests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecycler = (RecyclerView) findViewById(R.id.rideRequest_list);
        mRecycler.setHasFixedSize(true);

        noRequestsView = (TextView) findViewById(R.id.no_requests_text);
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

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkConnection();

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        getAllPosts();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("user-rideRequests").child(userId).
                limitToFirst(100);

        return recentPostsQuery;
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
                        Intent intent = new Intent(MyRideRequests.this, RideRequestDetailActivity.class);
                        intent.putExtra(RideRequestDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });
                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
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
                    .make(findViewById(R.id.fragment_ride_requests_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }
}
