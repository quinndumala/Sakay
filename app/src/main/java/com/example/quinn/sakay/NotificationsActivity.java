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
import android.widget.Toast;

import com.example.quinn.sakay.Models.Notif;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class NotificationsActivity extends BaseActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener{
    private static final String TAG = "NotifsActivity";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Notif, NotifsViewHolder> mAdapter;
    private RecyclerView mRecycler;

    private LinearLayoutManager mManager;
    private TextView noNotifsView;
    public Query notifsQuery;
    public String userId = getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) findViewById(R.id.notifications_list);
        mRecycler.setHasFixedSize(true);

        noNotifsView = (TextView) findViewById(R.id.no_notifs_text);
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
    public void onStart() {
        super.onStart();
        checkConnection();

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        getNotifsList();
    }

    public void onResume(){
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }


    public Query getQuery(DatabaseReference databaseReference){
        Query recentNotifsQuery = databaseReference.child("user-notifications").child(userId).limitToFirst(100);
        return recentNotifsQuery;
    }

    public void isQueryEmpty(Query query){
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    noNotifsView.setVisibility(View.VISIBLE);
                } else {
                    noNotifsView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getNotifsList(){
        notifsQuery = getQuery(mDatabase);
        isQueryEmpty(notifsQuery);
        mAdapter = new FirebaseRecyclerAdapter<Notif, NotifsViewHolder>(Notif.class,
                R.layout.item_notification, NotifsViewHolder.class, notifsQuery) {
            @Override
            protected void populateViewHolder(final NotifsViewHolder viewHolder, final Notif model,
                                              final int position) {
                final DatabaseReference notifRef = getRef(position);

                final String notifKey = notifRef.getKey();
//                notifRef.child("type")
//                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                if(dataSnapshot.exists()){
//                                    notifType = dataSnapshot.getValue().toString();
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
                final String notifType = model.getType();
                final String postKey = model.getPostKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(notifType.equals("request")){
                            viewRideOffer(postKey);

                        } else if (notifType.equals("offer")){
                            viewRideRequest(postKey);
                        } else if(notifType.equals("sakay")){
                            viewSakay(postKey);
                        }
                        else {
                            Toast.makeText(NotificationsActivity.this, "Not found", Toast.LENGTH_SHORT);
                        }
                    }
                });

                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public void viewRideOffer(String refKey){
        Intent intent = new Intent(this, RideOfferDetailActivity.class);
        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, refKey);
        startActivity(intent);
    }

    public void viewRideRequest(String refKey){
        Intent intent = new Intent(this, RideRequestDetailActivity.class);
        intent.putExtra(RideRequestDetailActivity.EXTRA_POST_KEY, refKey);
        startActivity(intent);
    }

    public void viewSakay(String sakayKey){
        Intent intent = new Intent(this, SakayDetailActivity.class);
        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, sakayKey);
        startActivity(intent);
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
                    .make(findViewById(R.id.activity_notifs_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }
}
