package com.example.quinn.sakay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quinn.sakay.Models.CommentRequest;
import com.example.quinn.sakay.Models.Notif;
import com.example.quinn.sakay.Models.RideRequest;
import com.example.quinn.sakay.Models.Sakay;
import com.example.quinn.sakay.Models.Vehicle;
import com.facebook.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
    private DatabaseReference mUserPostReference;
    private DatabaseReference mCommentsReference;
    private DatabaseReference vehicleRef;

    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView authorView;
    private CircleImageView authorPhotoView;
    private ImageView buttonDelete;
    private TextView startView;
    private TextView destinationView;
    private TextView dateAndTimeView;

    private Button sakayButton;
    private Button seeRouteButton;
    private RecyclerView sakaysViewRecycler;

    private CardView responsesTextView;
    private TextView noResponsesYetTextView;
    private CardView responsesView;

    private String userFacebookId = "";
    public Boolean isAuthor = true;
    private Profile profile = getCurrentProfile();

    private final String userId = getUid();
    private String userAuthorId;
    private String userAuthorFacebookId;
    private String userAuthorName;

    private String start;
    private Double startLat;
    private Double startLong;

    private String destination;
    private Double destinationLat;
    private Double destinationLong;

    private String dateAndTime;
    private Long timeStamp;

    private View positiveAction;
    private EditText EditVehicleType;
    private EditText EditVehicleModel;
    private EditText EditVehicleColor;
    private EditText EditPlateNo;

    public String currentVehicleType;
    public String currentVehicleModel;
    public String currentVehicleColor;
    public String currentVehiclePlateNo;

    public Boolean carSet = false;
    public User currentUser;
    public MaterialDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mPostReference = mRootRef.child("rideRequests").child(mPostKey);
        mUserPostReference = mRootRef.child("user-rideRequests").child(userId).child(mPostKey);
        mCommentsReference = mRootRef.child("rideRequests-comments").child(mPostKey);
        vehicleRef = mRootRef.child("users-settings").child(userId).child("vehicle");

        authorView = (TextView) findViewById(R.id.post_author_large);
        authorPhotoView = (CircleImageView) findViewById(R.id.post_author_photo_large);
        buttonDelete = (ImageView) findViewById(R.id.button_request_detail_delete);
        startView = (TextView) findViewById(R.id.request_start_view);
        destinationView = (TextView) findViewById(R.id.request_destination_view);
        dateAndTimeView = (TextView) findViewById(R.id.request_dateAndTime_view);

        responsesTextView = (CardView) findViewById(R.id.request_responses_text_view);
        noResponsesYetTextView = (TextView) findViewById(R.id.no_responses_text_view_request);
        responsesView = (CardView) findViewById(R.id.responses_view_request);

        sakayButton = (Button) findViewById(R.id.button_sakay_request);
        seeRouteButton = (Button) findViewById(R.id.button_see_route_request);
        sakaysViewRecycler = (RecyclerView) findViewById(R.id.recycler_request_comment);
        userFacebookId = profile.getId();

        loadingDialog = new MaterialDialog.Builder(this)
                .title("Loading details")
                .content("Please wait")
                .progress(true, 0)
                .show();

        checkForVehicle();

        sakayButton.setOnClickListener(this);
        seeRouteButton.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        authorPhotoView.setOnClickListener(this);
        authorView.setOnClickListener(this);
        sakaysViewRecycler.setLayoutManager(new LinearLayoutManager(this));

        mPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    RideRequest rideRequest = dataSnapshot.getValue(RideRequest.class);
                    if ((rideRequest.uid.equals(userId))){
                        buttonDelete.setVisibility(View.VISIBLE);
                    } else {
                        isAuthor = false;
                        sakayButton.setVisibility(View.VISIBLE);
                    }
                    setPhoto(rideRequest.facebookId);
                    authorView.setText(rideRequest.author);
                    startView.setText(rideRequest.start);
                    destinationView.setText(rideRequest.destination);
                    dateAndTimeView.setText(rideRequest.dateAndTime);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        noResponses();

        mRootRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCommentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)){
                    sakayButton.setText("\u2713" + " Sakay offer sent");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    RideRequest rideRequest = dataSnapshot.getValue(RideRequest.class);

                    userAuthorId = rideRequest.uid;
                    userAuthorName = rideRequest.author;
                    userAuthorFacebookId = rideRequest.facebookId;

                    start = rideRequest.start;
                    startLat = rideRequest.startLat;
                    startLong = rideRequest.startLong;

                    destination = rideRequest.destination;
                    destinationLat = rideRequest.destinationLat;
                    destinationLong = rideRequest.destinationLong;

                    dateAndTime = rideRequest.dateAndTime;
                    timeStamp = rideRequest.timeStamp;
                }



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

        mPostListener = postListener;

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
        } else if(id == R.id.button_see_route_request) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" + startLat + "," + startLong +
                            "&daddr=" + destinationLat + "," + destinationLong));
            startActivity(intent);
        } else if (id == R.id.button_request_detail_delete){
            launchConfirmDelete();
        } else if (id == R.id.post_author_photo_large){
            viewProfile();
        } else if (id == R.id.post_author_large){
            viewProfile();
        }
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
                    .make(this.findViewById(R.id.content_ride_request_detail), message, Snackbar.LENGTH_INDEFINITE);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    public void viewProfile(){
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra(ViewProfileActivity.EXTRA_USER_KEY, userAuthorId);
        startActivity(intent);
    }

    public void alreadyExists(){
        mCommentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)){
                    launchAlreadySentDialog();
                } else {
                    //launchSakayDialog();
                    if (carSet){
                        launchMyVehicleInput();
                    } else {
                        launchInputVehicle();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void noResponses(){
        mCommentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() && isAuthor){
                    responsesTextView.setVisibility(View.VISIBLE);
                    responsesView.setVisibility(View.VISIBLE);
                } else {
                    responsesTextView.setVisibility(View.GONE);
                    responsesView.setVisibility(View.GONE);
                    if (isAuthor){ noResponsesYetTextView.setVisibility(View.VISIBLE);}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deletePost(){
        finish();
        mUserPostReference.removeValue();
        mPostReference.removeValue();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
    }

    private void postComment(final String vehicle, final String vehicleModel, final String vehicleColor,
                             final String vehiclePlateNo) {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.getName();

                        CommentRequest comment = new CommentRequest(uid, authorName, userFacebookId,
                                vehicle, vehicleModel, vehicleColor, vehiclePlateNo);

                        // Push the comment, it will appear in the list
                        Map<String, Object> postValues = comment.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/rideRequests-comments/" + mPostKey + "/" + userId, postValues);

                        mRootRef.updateChildren(childUpdates);
//                        noResponses();
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
            final String commentVehicleModel = comment.vehicleModel;
            final String commentVehicleColor = comment.vehicleColor;
            final String commentVehiclePlateNo = comment.vehiclePlateNo;

            holder.authorView.setText(commentAuthor);
            holder.vehicleView.setText(commentVehicle);

            String imageUrl = "https://graph.facebook.com/" + commentFacebookId + "/picture?height=150";
            GlideUtil.loadProfileIcon(imageUrl, holder.authorPhotoView);

            holder.buttonSakay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchConfirmSakay(commentAuthorUid, commentAuthor, commentFacebookId,
                            commentVehicle, commentVehicleModel, commentVehicleColor, commentVehiclePlateNo);
                }
            });

            holder.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    intent.putExtra(ViewProfileActivity.EXTRA_USER_KEY, commentAuthorUid);
                    startActivity(intent);
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
                                       final String commentFacebookId, final String commentVehicle,
                                       final String commentVehicleModel, final String commentVehicleColor,
                                       final String commentVehiclePlateNo){
            new MaterialDialog.Builder(mContext)
                    .content("Confirm Sakay?")
                    .positiveText("OK")
                    .negativeText("CANCEL")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String sakayKey = mRootRef.child("user-sakays").push().getKey();
                            newSakay(userId, userAuthorName, userFacebookId, "rider",
                                    start, startLat, startLong,
                                    destination, destinationLat, destinationLong,
                                    dateAndTime, timeStamp,
                                    commentVehicle, commentVehicleModel, commentVehicleColor, commentVehiclePlateNo,
                                    commentAuthorId, commentAuthor, commentFacebookId,
                                    sakayKey);
                            newSakay(commentAuthorId, commentAuthor, commentFacebookId, "driver",
                                    start, startLat, startLong,
                                    destination, destinationLat, destinationLong,
                                    dateAndTime, timeStamp,
                                    commentVehicle, commentVehicleModel, commentVehicleColor, commentVehiclePlateNo,
                                    userId, userAuthorName, userFacebookId,
                                    sakayKey);
                            createNotif(sakayKey, "sakay", commentAuthorId);
                            Toast.makeText(mContext, "Sakay succesfully added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .show();
        }

        private void newSakay(String userId, String userName, String userFacebookId, String userRole,
                              String start, Double startLat, Double startLong,
                              String destination, Double destinationLat, Double destinationLong,
                              String dateAndTime, Long timeStamp,
                              String vehicle, String vehicleModel, String vehicleColor, String vehiclePlateNo,
                              String otherId, String otherName, String otherFacebookId,
                              String sakayKey){
            //String key = mRootRef.child("user-sakays").push().getKey();
            Sakay sakay = new Sakay(userId, userName, userFacebookId, userRole,
                    start, startLat, startLong,
                    destination, destinationLat, destinationLong,
                    dateAndTime, timeStamp,
                    vehicle, vehicleModel, vehicleColor, vehiclePlateNo,
                    otherId, otherName, otherFacebookId);

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
                        if (carSet){
                            launchMyVehicleInput();
                        } else {
                            launchInputVehicle();
                        }
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

//    public void launchInputVehicle(){
//        new MaterialDialog.Builder(this)
//                .title("Input Vehicle")
//                .positiveText("submit")
//                .cancelable(false)
//                .input(null, null, false, new MaterialDialog.InputCallback() {
//                    @Override
//                    public void onInput(MaterialDialog dialog, CharSequence input) {
//                        postComment(input.toString());
//                        Toast.makeText(RideRequestDetailActivity.this, "Sakay request sent",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }).show();
//    }

    public void launchInputVehicle(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Your vehicle details")
                .customView(R.layout.item_vehicle, true)
                .positiveText("ok")
                .negativeText("cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postComment(EditVehicleType.getText().toString(),
                                EditVehicleModel.getText().toString(),
                                EditVehicleColor.getText().toString(),
                                EditPlateNo.getText().toString());
                        createNotif(mPostKey, "offer", null);
                        Toast.makeText(RideRequestDetailActivity.this, "Sakay request sent",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        EditVehicleType = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_type);
        EditVehicleModel = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_model);
        EditVehicleColor = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_color);
        EditPlateNo = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_plate_no);


        dialog.show();

    }

    public void launchMyVehicleInput(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Your vehicle details")
                .customView(R.layout.item_vehicle, true)
                .positiveText("ok")
                .negativeText("cancel")
                .neutralText("use my vehicle")
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postComment(EditVehicleType.getText().toString(),
                                EditVehicleModel.getText().toString(),
                                EditVehicleColor.getText().toString(),
                                EditPlateNo.getText().toString());
                        createNotif(mPostKey, "offer", null);
                        Toast.makeText(RideRequestDetailActivity.this, "Sakay offer sent",
                                Toast.LENGTH_SHORT).show();
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
                        EditVehicleType.setText(currentVehicleType);
                        EditVehicleModel.setText(currentVehicleModel);
                        EditVehicleColor.setText(currentVehicleColor);
                        EditPlateNo.setText(currentVehiclePlateNo);
                    }
                })
                .build();
        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        EditVehicleType = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_type);
        EditVehicleModel = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_model);
        EditVehicleColor = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_color);
        EditPlateNo = (EditText) dialog.getCustomView().findViewById(R.id.vehicle_plate_no);


        dialog.show();
    }

    public void launchConfirmDelete(){
        new MaterialDialog.Builder(this)
                .content("Do you want to delete this ride request?")
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

    public void checkForVehicle(){
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carSet = true;
                    Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);
                    currentVehicleType = vehicle.vehicleType;
                    currentVehicleColor = vehicle.vehicleColor;
                    currentVehicleModel = vehicle.vehicleModel;
                    currentVehiclePlateNo = vehicle.plateNo;
                } else {
                    carSet = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createNotif(String key, String type, String commentAuthorID){
        String notifId;
        String notifKey = mRootRef.child("user-notifications").push().getKey();
        Notif notif;
        String action;

        if(type.equals("offer")){
            notifId = userAuthorId;
            action = "sent you a ride offer";
        } else {
            notifId = commentAuthorID;
            action = "accepted your ride offer";
        }

        notif = new Notif(userId, currentUser.getName(), currentUser.getFacebookId(), type, key,
                action, false, ServerValue.TIMESTAMP);
        Map<String, Object> notifValues = notif.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-notifications/" + notifId + "/" + notifKey, notifValues);
        mRootRef.updateChildren(childUpdates);

        mRootRef.child("notif-check").child(notifId).setValue(true);
    }

}
