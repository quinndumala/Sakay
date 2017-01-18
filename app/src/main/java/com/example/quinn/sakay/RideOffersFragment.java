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
import com.example.quinn.sakay.Models.RideOffer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.simplicityapks.reminderdatepicker.lib.DateSpinner;
import com.simplicityapks.reminderdatepicker.lib.OnDateSelectedListener;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class RideOffersFragment extends Fragment
    implements ConnectivityReceiver.ConnectivityReceiverListener{
    private static final String TAG = "RideOffersFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FloatingActionButton addRideOffer;
    private View positiveAction;
    private DateSpinner filterDate;
    public String dateAndTime = "";
    public Timestamp time;

    public RideOffersFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_ride_offers, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.rideOffers_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String imageUrl = getActivity().getIntent().getExtras().getString("profile_picture");
        addRideOffer = (FloatingActionButton) view.findViewById(R.id.fabRideOffers);
        addRideOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddRideOffer.class);
                intent.putExtra("profile_picture",imageUrl);
                getActivity().startActivity(intent);
            }
        });

        checkConnection();
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


        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        /*
        Set up FirebaseRecyclerAdapter with the Query
        TODO: Check if database node has values first and set a condition
        */
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<RideOffer, RideOfferViewHolder>(RideOffer.class,
                R.layout.item_ride_offer, RideOfferViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final RideOfferViewHolder viewHolder, final RideOffer model,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), RideOfferDetailActivity.class);
                        intent.putExtra(RideOfferDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
//                if (model.stars.containsKey(getUid())) {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
//                } else {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
//                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
//                viewHolder.bindToPost(model, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View starView) {
//                        // Need to write to both places the post is stored
//                        DatabaseReference globalPostRef = mDatabase.child("rideOffers").child(postRef.getKey());
//                        DatabaseReference userPostRef = mDatabase.child("user-rideOffers").child(model.uid).child(postRef.getKey());
//
//                        // Run two transactions
////                        onStarClicked(globalPostRef);
////                        onStarClicked(userPostRef);
//                    }
//                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("rideOffers")
                .limitToFirst(100);
        // [END recent_posts_query]

        return recentPostsQuery;
    }

    public void onResume(){
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle("Ride Offers");
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
                .positiveText("OK")
                .negativeText("cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                       Toast.makeText(getActivity(), "TODO: Filter posts", Toast.LENGTH_SHORT).show();
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
                time = new Timestamp(date.getTime().getTime());
                Log.d(TAG, "Selected date: " + selectedDate);
                Log.d(TAG, "Timestamp: " + time.toString());
            }
        });

        dialog.show();
    }
}
