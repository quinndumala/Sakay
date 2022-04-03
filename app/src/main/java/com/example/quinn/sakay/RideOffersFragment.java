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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.RideOffer;
import com.facebook.Profile;
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

import static com.facebook.Profile.getCurrentProfile;


/**
 * A simple {@link Fragment} subclass.
 */
public class RideOffersFragment extends Fragment
    implements ConnectivityReceiver.ConnectivityReceiverListener{
    private static final String TAG = "RideOffersFragment";

    private DatabaseReference mDatabase;
    private DatabaseReference notifCheckRef;

    private FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder> filterAdapter;
    private RecyclerView mRecycler;

    private LinearLayoutManager mManager;
    private FloatingActionButton addRideOffer;
    private View positiveAction;
    private TextView noOffersView;
    private DateSpinner filterDate;
    public String dateAndTime = "";
    public Long filterTime;
    public Query allPostsQuery;
    public String userId = getUid();
    public Boolean hasNotifs;

    private Profile profile = getCurrentProfile();

    public RideOffersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_notifications){
            notifCheckRef.setValue(false);
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_filter_date) {
            launchFilterPosts();
        } else if (id == R.id.action_notifications_no_badge){
            notifCheckRef.setValue(false);
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ride_offers, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        notifCheckRef = mDatabase.child("notif-check").child(userId);

        mRecycler = (RecyclerView) rootView.findViewById(R.id.rideOffers_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String imageUrl = getActivity().getIntent().getExtras().getString("profile_picture");
        noOffersView = (TextView) view.findViewById(R.id.no_offers_text);
        addRideOffer = (FloatingActionButton) view.findViewById(R.id.fabRideOffers);
        addRideOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddRideOffer.class);
                intent.putExtra("profile_picture",imageUrl);
                getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        checkConnection();

        notifCheckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Boolean data = dataSnapshot.getValue(Boolean.class);
                    if(data){
                        hasNotifs = true;

                    } else {
                        hasNotifs = false;
                    }

                    if(getActivity() != null){
                        getActivity().invalidateOptionsMenu();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addRideOffer.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addRideOffer.show(true);
                addRideOffer.setShowAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.show_from_bottom));
                addRideOffer.setHideAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.hide_to_bottom));
            }
        }, 450);

        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 && addRideOffer.isShown())
                    addRideOffer.hide(true);
                else if (dy<0)
                    addRideOffer.show(true);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                //super.onScrollStateChanged(recyclerView, newState);
            }
        });

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        getAllPosts();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("rideOffers").limitToFirst(100);

        return recentPostsQuery;
    }

    public Query filterQuery(DatabaseReference databaseReference){
        long endTime = filterTime + TimeUnit.HOURS.toMillis(24);
        Log.d(TAG, "End Time: " + endTime);
        Query filterQuery = databaseReference.child("rideOffers").orderByChild("timeStamp").startAt(filterTime)
                .endAt(endTime);
        return filterQuery;
    }

    public void isQueryEmpty(Query query){
       query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    noOffersView.setVisibility(View.VISIBLE);
                } else {
                    noOffersView.setVisibility(View.GONE);
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
        mAdapter = new FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder>(RideOffer.class,
                R.layout.item_ride_offer, RideOfferViewHolder.class, allPostsQuery) {
            @Override
            protected void populateViewHolder(final RideOfferViewHolder viewHolder, final RideOffer model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), RideOfferDetailActivity.class);
                        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
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
        filterAdapter = new FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder>(RideOffer.class,
                R.layout.item_ride_offer, RideOfferViewHolder.class, filter) {
            @Override
            protected void populateViewHolder(final RideOfferViewHolder viewHolder, final RideOffer model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), RideOfferDetailActivity.class);
                        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
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
//                .setActionBarTitle("Ride Offers");
        MyApplication.getInstance().setConnectivityListener(this);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
                    .make(getView().findViewById(R.id.fragment_ride_offers_layout), message, Snackbar.LENGTH_INDEFINITE);

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
                .title("Select date to filter ride offers")
                .customView(R.layout.item_date_picker, true)
                .autoDismiss(false)
                .positiveText("OK")
                .negativeText("cancel")
                .neutralText("show all")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getActivity(), "Searching...", Toast.LENGTH_SHORT).show();
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
