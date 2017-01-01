package com.example.quinn.sakay;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.CommentRequest;
import com.example.quinn.sakay.Models.RideRequest;
import com.example.quinn.sakay.Models.Sakay;
import com.facebook.Profile;
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

public class RideRequestDetailActivity extends BaseActivity implements
        View.OnClickListener{

    private static final String TAG = "RideRequestDetail";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mRootRef;
    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView authorView;
    private CircleImageView authorPhotoView;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;
    private ViewGroup responsesTextView;
    private TextView noResponsesYetTextView;
    private Button sakayButton;
    private RecyclerView sakaysViewRecycler;

    private String userFacebookId = "";
    public Boolean isAuthor = true;
    private Profile profile = getCurrentProfile();

    private final String userId = getUid();
    private String userAuthorName;
    private String start;
    private String destination;
    private String dateAndTime;

    private String requesteeUid;
    private String requesteefacebookId;
    private String requesteeAuthorname;

    public String getUserAuthorName(){
        return userAuthorName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request_detail);
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
        mPostReference = mRootRef.child("rideRequests").child(mPostKey);
        mCommentsReference = mRootRef.child("rideRequests-comments").child(mPostKey);

        // Initialize Views
        authorView = (TextView) findViewById(R.id.post_author_large);
        authorPhotoView = (CircleImageView) findViewById(R.id.post_author_photo_large);
        startView = (TextView) findViewById(R.id.request_start_view);
        destinationView = (TextView) findViewById(R.id.request_destination_view);
        dateAndTimeView = (TextView) findViewById(R.id.request_dateAndTime_view);
        responsesTextView = (ViewGroup) findViewById(R.id.request_responses_text);
        sakayButton = (Button) findViewById(R.id.button_sakay_request);
        sakaysViewRecycler = (RecyclerView) findViewById(R.id.recycler_request_comment);
        noResponsesYetTextView = (TextView) findViewById(R.id.no_responses_yet_text_request);
        userFacebookId = profile.getId();
        noResponses();

        sakayButton.setOnClickListener(this);
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
                // Get Post object and use the values to update the UI
                RideRequest rideRequest = dataSnapshot.getValue(RideRequest.class);
                if (!(rideRequest.uid.equals(userId))){
                    isAuthor = false;
                    sakaysViewRecycler.setVisibility(View.GONE);
                    responsesTextView.setVisibility(View.GONE);
                    sakayButton.setVisibility(View.VISIBLE);
                }
                // [START_EXCLUDE]
                setPhoto(rideRequest.facebookId);
                authorView.setText(rideRequest.author);
                startView.setText(rideRequest.start);
                destinationView.setText(rideRequest.destination);
                dateAndTimeView.setText(rideRequest.dateAndTime);
                // [END_EXCLUDE]

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

                    }
                });

                userAuthorName = rideRequest.author;
                start = rideRequest.start;
                destination = rideRequest.destination;
                dateAndTime = rideRequest.dateAndTime;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(RideRequestDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
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

        if (id == R.id.button_sakay_request) {
            alreadyExists();
        }
    }

    public void alreadyExists(){
        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)){
                    launchAlreadySentDialog();
                } else {
                    launchSakayDialog();
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
                } else {
                    if (isAuthor){ noResponsesYetTextView.setVisibility(View.VISIBLE);}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void postComment(final String vehicle) {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.getName();

                        CommentRequest comment = new CommentRequest(uid, authorName, userFacebookId, vehicle);

                        // Push the comment, it will appear in the list
                        Map<String, Object> postValues = comment.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/rideRequests-comments/" + mPostKey + "/" + userId, postValues);

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
        public TextView vehicleView;
        public CircleImageView authorPhotoView;
        public Button buttonViewProfile;
        public Button buttonSakay;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorView = (TextView) itemView.findViewById(R.id.comment_author_request);
            vehicleView = (TextView) itemView.findViewById(R.id.comment_vehicle_request);
            authorPhotoView = (CircleImageView) itemView.findViewById(R.id.comment_author_photo_request);
            buttonViewProfile = (Button) itemView.findViewById(R.id.comment_button_view_profile_request);
            buttonSakay = (Button) itemView.findViewById(R.id.comment_button_sakay_request);
        }
    }


    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<CommentRequest> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    CommentRequest comment = dataSnapshot.getValue(CommentRequest.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    CommentRequest newComment = dataSnapshot.getValue(CommentRequest.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    CommentRequest movedComment = dataSnapshot.getValue(CommentRequest.class);
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
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment_request, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            final CommentRequest comment = mComments.get(position);
            final String commentAuthor = comment.author;
            final String commentFacebookId = comment.facebookId;
            final String commentAuthorUid = comment.uid;
            final String commentVehicle = comment.vehicle;

            holder.authorView.setText(commentAuthor);
            holder.vehicleView.setText(commentVehicle);

            String imageUrl = "https://graph.facebook.com/" + commentFacebookId + "/picture?height=150";
            GlideUtil.loadProfileIcon(imageUrl, holder.authorPhotoView);

            holder.buttonSakay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchConfirmSakay(commentAuthorUid, commentAuthor, commentFacebookId, commentVehicle);
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
                                       final String commentFacebookId, final String commentVehicle){
            new MaterialDialog.Builder(mContext)
                    .content("Confirm Sakay? This action is irreversible.")
                    .positiveText("OK")
                    .negativeText("CANCEL")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String sakayKey = mRootRef.child("user-sakays").push().getKey();
                            newSakay(userId, userAuthorName, userFacebookId, "rider", start, destination,
                                    dateAndTime, commentVehicle, commentAuthorId, commentAuthor, commentFacebookId,
                                    sakayKey);
                            newSakay(commentAuthorId, commentAuthor, commentFacebookId, "driver", start,
                                    destination, dateAndTime, commentVehicle, userId, userAuthorName,
                                    userFacebookId, sakayKey);
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

    public void launchSakayDialog(){
        new MaterialDialog.Builder(this)
                .content("Sakay this ride?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchInputVehicle();
                    }
                })
                .show();
    }

    public void launchAlreadySentDialog(){
        new MaterialDialog.Builder(this)
                .content("Sakay request has already been sent for this ride request")
                .positiveText("OK")
                .show();
    }

    public void launchInputVehicle(){
        new MaterialDialog.Builder(this)
                .title("Input Vehicle")
                .positiveText("submit")
                .cancelable(false)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        postComment(input.toString());
                        Toast.makeText(RideRequestDetailActivity.this, "Sakay request sent",
                                Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }



}
