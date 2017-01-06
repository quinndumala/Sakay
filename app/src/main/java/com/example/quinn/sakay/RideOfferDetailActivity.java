package com.example.quinn.sakay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.CommentOffer;
import com.example.quinn.sakay.Models.RideOffer;
import com.example.quinn.sakay.Models.Sakay;
import com.facebook.Profile;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.Profile.getCurrentProfile;

public class RideOfferDetailActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "RideOfferDetail";

    public static final String EXTRA_POST_KEY = "post_key";
    private static final int REQUEST_PLACE_PICKER = 1;

    private DatabaseReference mRootRef;
    private DatabaseReference mRideOffersRef;
    private DatabaseReference mPostReference;
    private DatabaseReference mUserPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private RideOfferDetailActivity.CommentAdapter mAdapter;

    private TextView authorView;
    private CircleImageView authorPhotoView;
    private ImageView buttonDelete;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;
    private TextView vehicleView;
//    private ViewGroup responsesTextView;
    //private TextView noResponsesYetTextView;
    private Button sakayButton;
    private RecyclerView sakaysViewRecycler;

    private CardView responsesTextView;
    private TextView noResponsesYetTextView;
    private CardView responsesView;

    private String userFacebookId = "";
    public Boolean isAuthor = true;
    private Profile profile = getCurrentProfile();

    private final String userId = getUid();
    private String userAuthorName;
    private String start;
    private String destination;
    private String dateAndTime;
    private String vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_offer_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRideOffersRef = mRootRef.child("rideOffers");
        mPostReference = mRootRef.child("rideOffers").child(mPostKey);
        mUserPostReference = mRootRef.child("user-rideOffers").child(userId).child(mPostKey);
        mCommentsReference = mRootRef.child("rideOffers-comments").child(mPostKey);

        // Initialize Views
        authorView = (TextView) findViewById(R.id.post_author_large);
        authorPhotoView = (CircleImageView) findViewById(R.id.post_author_photo_large);
        buttonDelete = (ImageView) findViewById(R.id.button_offer_detail_delete);
        startView = (TextView) findViewById(R.id.offer_start_view);
        destinationView = (TextView) findViewById(R.id.offer_destination_view);
        vehicleView = (TextView) findViewById(R.id.offer_vehicle_view);
        dateAndTimeView = (TextView) findViewById(R.id.offer_dateAndTime_view);

        responsesTextView = (CardView) findViewById(R.id.offer_responses_text_view);
        noResponsesYetTextView = (TextView) findViewById(R.id.no_responses_text_view_offer);
        responsesView = (CardView) findViewById(R.id.responses_view_offer);

        sakayButton = (Button) findViewById(R.id.button_sakay_offer);
        sakaysViewRecycler = (RecyclerView) findViewById(R.id.recycler_offer_comment);

        userFacebookId = profile.getId();
        noResponses();

        sakayButton.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        sakaysViewRecycler.setLayoutManager(new LinearLayoutManager(this));
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

        // Add value event listener to the post
        // [START post_value_event_listener]

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get Post object and use the values to update the UI
                    RideOffer rideOffer = dataSnapshot.getValue(RideOffer.class);
                    if (!(rideOffer.uid.equals(userId))){
                        isAuthor = false;
                        sakayButton.setVisibility(View.VISIBLE);
                    } else {
                        buttonDelete.setVisibility(View.VISIBLE);
                    }
                    // [START_EXCLUDE]
                    setPhoto(rideOffer.facebookId);
                    authorView.setText(rideOffer.author);
                    startView.setText(rideOffer.start);
                    destinationView.setText(rideOffer.destination);
                    vehicleView.setText(rideOffer.vehicle);
                    dateAndTimeView.setText(rideOffer.dateAndTime);
                    // [END_EXCLUDE]

                    userAuthorName = rideOffer.author;
                    start = rideOffer.start;
                    destination = rideOffer.destination;
                    dateAndTime = rideOffer.dateAndTime;
                    vehicle = rideOffer.vehicle;
                }


                mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)){
                            sakayButton.setText("\u2713" + " Sakay request sent");
                        } else {
                            //launchSakayDialog();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        finish();
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(RideOfferDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new RideOfferDetailActivity.CommentAdapter(this, mCommentsReference);
        sakaysViewRecycler.setAdapter(mAdapter);


    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_sakay_offer) {
            alreadyExists();
        } else if (id == R.id.button_offer_detail_delete) {
            Log.d(TAG, "delete clicked");
            launchConfirmDelete();
        }
    }

    public void alreadyExists(){
        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)){
                    launchAlreadySentDialog();
                } else {
                    launchPickupLocationPicker();
                    //launchSakayDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void noResponses(){
        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() && isAuthor){
                    responsesTextView.setVisibility(View.VISIBLE);
                    responsesView.setVisibility(View.VISIBLE);
                } else {
                    if (isAuthor){
                        noResponsesYetTextView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deletePost(){

//        mPostReference.setValue(null);

        finish();
        mUserPostReference.removeValue();
        mPostReference.removeValue();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        //DeletePost.DeleteThis(mPostReference, mUserPostReference);
    }

    private void postComment(final String location) {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.getName();

                        CommentOffer comment = new CommentOffer(uid, authorName, userFacebookId, location);

                        // Push the comment, it will appear in the list
                        Map<String, Object> postValues = comment.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/rideOffers-comments/" + mPostKey + "/" + userId, postValues);

                        mRootRef.updateChildren(childUpdates);
                        noResponses();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView authorView;
        public TextView pickUpView;
        public CircleImageView authorPhotoView;
        public Button buttonViewProfile;
        public Button buttonSakay;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorView = (TextView) itemView.findViewById(R.id.comment_author_offer);
            pickUpView = (TextView) itemView.findViewById(R.id.comment_pickup_offer);
            authorPhotoView = (CircleImageView) itemView.findViewById(R.id.comment_author_photo_offer);
            buttonSakay = (Button) itemView.findViewById(R.id.comment_button_sakay_offer);
            buttonViewProfile = (Button) itemView.findViewById(R.id.comment_button_view_profile_offer);
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<CommentOffer> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    CommentOffer comment = dataSnapshot.getValue(CommentOffer.class);

                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    CommentOffer newComment = dataSnapshot.getValue(CommentOffer.class);
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mComments.set(commentIndex, newComment);
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    CommentOffer movedComment = dataSnapshot.getValue(CommentOffer.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment_offer, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            final CommentOffer comment = mComments.get(position);
            final String commentAuthor = comment.author;
            final String commentPickup = comment.pickUp;
            final String commentFacebookId = comment.facebookId;
            final String commentAuthorUid = comment.uid;

            holder.authorView.setText(commentAuthor);
            holder.pickUpView.setText(commentPickup);
            String imageUrl = "https://graph.facebook.com/" + comment.facebookId + "/picture?height=150";
            GlideUtil.loadProfileIcon(imageUrl, holder.authorPhotoView);

            holder.buttonSakay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchConfirmSakay(commentAuthorUid, commentAuthor, commentFacebookId, commentPickup);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

        public void launchConfirmSakay(final String commentAuthorId, final String commentAuthor,
                                       final String commentFacebookId, final String commentPickup){
            new MaterialDialog.Builder(mContext)
                    .content("Confirm Sakay? This action is irreversible.")
                    .positiveText("OK")
                    .negativeText("CANCEL")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String sakayKey = mRootRef.child("user-sakays").push().getKey();
                            newSakay(userId, userAuthorName, userFacebookId, "driver", commentPickup,
                                    destination, dateAndTime, vehicle, commentAuthorId, commentAuthor,
                                    commentFacebookId,
                                    sakayKey);
                            newSakay(commentAuthorId, commentAuthor, commentFacebookId, "rider", commentPickup,
                                    destination, dateAndTime, vehicle, userId, userAuthorName, userFacebookId,
                                    sakayKey);
                            Toast.makeText(mContext, "Sakay succesfully added", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }

        private void newSakay(String userId, String userName, String userFacebookId, String userRole,
                              String start, String destination, String dateAndTime, String vehicle,
                              String otherId, String otherName, String otherFacebookId,
                              String sakayKey){
            //String key = mRootRef.child("user-sakays").push().getKey();
            Sakay sakay = new Sakay(userId, userName, userFacebookId, userRole, start, destination, dateAndTime,
                    vehicle, otherId, otherName, otherFacebookId);
            Map<String, Object> sakayValues = sakay.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/user-sakays/" + userId + "/" + sakayKey, sakayValues);
            mRootRef.updateChildren(childUpdates);
        }
    }

    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, authorPhotoView);
    }

    public void launchSakayDialog(final String location){
        new MaterialDialog.Builder(this)
                .content("Pickup location has been set to " + location + ". Send sakay request?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postComment(location);
                        Toast.makeText(RideOfferDetailActivity.this, "Sakay request sent",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void launchAlreadySentDialog(){
        new MaterialDialog.Builder(this)
                .content("Sakay request already sent")
                .positiveText("OK")
                .show();
    }

    public void launchPickupLocationPicker(){
        new MaterialDialog.Builder(this)
                .title("Choose a pickup location")
                .items(R.array.offer_select_pickup_location)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if(text.equals("Choose on Map")){
                            launchPlacePicker();
                        }
                    }
                })
                .positiveText("cancel")
                .show();

    }

    public void launchAlertDialog(){
        MaterialDialog alertDialog = new MaterialDialog.Builder(this)
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

    public void launchConfirmDelete(){
        new MaterialDialog.Builder(this)
                .content("Do you want to delete this ride offer?")
                .positiveText("OK")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deletePost();
                    }
                })
                .show();
    }

    public void launchPlacePicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);

            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, this);

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
                    launchSakayDialog(location);
                    //Toast.makeText(this, "Chosen Location: " + location, Toast.LENGTH_SHORT).show();
                }

//                // Update data on card.
//                getCardStream().getCard(CARD_DETAIL)
//                        .setTitle(name.toString())
//                        .setDescription(getString(R.string.detail_text, placeId, address, phone,
//                                attribution));

                Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");

            } else {
                // User has not selected a place, hide the card.
                //fStart.setText(R.string.select_location);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
