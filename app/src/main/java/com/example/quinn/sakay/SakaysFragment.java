package com.example.quinn.sakay;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.quinn.sakay.Models.Sakay;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SakaysFragment extends Fragment
        implements ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = "SakaysFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private DatabaseReference userSakaysRef;

    private FirebaseRecyclerAdapter<Sakay, SakaysViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<Sakay, SakaysViewHolder> todayAdapter;
    private RecyclerView mRecycler;
    private RecyclerView todayRecycler;
    private LinearLayoutManager mManager;
    private LinearLayoutManager todayManager;
    private final String userId = getUid();
    private TextView noSakays;

    public TextView todayTextView;

    public SakaysFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter_date);
        item.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sakays, container, false);
        noSakays = (TextView) rootView.findViewById(R.id.no_sakays_text);
        todayTextView = (TextView) rootView.findViewById(R.id.sakay_today_text);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userSakaysRef = mDatabase.child("user-sakays").child(userId);

        mRecycler = (RecyclerView) rootView.findViewById(R.id.sakays_list);
        todayRecycler = (RecyclerView) rootView.findViewById(R.id.sakays_today_list);
        //mRecycler.setHasFixedSize(true);

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

        userSakaysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    noSakays.setVisibility(View.GONE);
                } else {
                    noSakays.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setNestedScrollingEnabled(false);
        mRecycler.setLayoutManager(mManager);

        todayManager = new LinearLayoutManager(getActivity());
        todayManager.setReverseLayout(true);
        todayManager.setStackFromEnd(true);
        todayRecycler.setNestedScrollingEnabled(false);
        todayRecycler.setLayoutManager(todayManager);

        getTodaySakays();
        getAllSakays();
    }

    public static Long getStartOfDay() {
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();

    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentSakaysQuery = databaseReference.child("user-sakays").child(userId).orderByChild("timeStamp")
                .limitToFirst(20);
        // [END recent_posts_query]

        return recentSakaysQuery;
    }

    public Query getTodayQuery(DatabaseReference databaseReference){
        Long todayMidnight = getStartOfDay();
        long endTime = todayMidnight + TimeUnit.HOURS.toMillis(24);
        Query sakaysTodayQuery = databaseReference.child("user-sakays").child(userId).orderByChild("timeStamp")
                .startAt(todayMidnight).endAt(endTime);
        return sakaysTodayQuery;
    }

    public void isQueryEmpty(Query query){;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    //noRequestsView.setVisibility(View.VISIBLE);
                    todayTextView.setText("No sakays for today");

                } else {
                    //noRequestsView.setVisibility(View.GONE);
                    todayTextView.setText("Today");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getAllSakays(){
        Query postsQuery = getQuery(mDatabase);

        mAdapter = new FirebaseRecyclerAdapter<Sakay, SakaysViewHolder>(Sakay.class,
                R.layout.item_sakay, SakaysViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final SakaysViewHolder viewHolder, final Sakay model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        Intent intent = new Intent(getActivity(), SakayDetailActivity.class);
                        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public void getTodaySakays(){
        Query todayQuery = getTodayQuery(mDatabase);
        isQueryEmpty(todayQuery);

        todayAdapter = new FirebaseRecyclerAdapter<Sakay, SakaysViewHolder>(Sakay.class,
                R.layout.item_sakay, SakaysViewHolder.class, todayQuery) {
            @Override
            protected void populateViewHolder(final SakaysViewHolder viewHolder, final Sakay model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        Intent intent = new Intent(getActivity(), SakayDetailActivity.class);
                        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

            }
        };
        todayRecycler.setAdapter(todayAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        checkConnection();
    }

    @Override
    public void onResume(){
        super.onResume();
//        ((MainActivity) getActivity())
//                .setActionBarTitle("Sakays");

        MyApplication.getInstance().setConnectivityListener(this);
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
            message = "No connection";
            Snackbar snackbar = Snackbar
                    .make(getView().findViewById(R.id.fragment_sakays_layout), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
