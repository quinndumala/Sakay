package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.quinn.sakay.Models.RideRequest;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Quinn on 23/12/2016.
 */

public class RideRequestViewHolder extends RecyclerView.ViewHolder {
    public TextView authorView;
    public CircleImageView authorPhotoView;
    public TextView startView;
    public TextView destinationView;
    public TextView dateAndTimeView;

    public RideRequestViewHolder(View itemView) {
        super(itemView);

        authorView = (TextView) itemView.findViewById(R.id.post_author);
        authorPhotoView = (CircleImageView) itemView.findViewById(R.id.post_author_photo);
        startView = (TextView) itemView.findViewById(R.id.request_start_view);
        destinationView = (TextView) itemView.findViewById(R.id.request_destination_view);
        dateAndTimeView = (TextView) itemView.findViewById(R.id.request_dateAndTime_view);
    }

    public void bindToPost(RideRequest rideRequest) {
        setPhoto(rideRequest.facebookId);
        authorView.setText(rideRequest.author);
        startView.setText(rideRequest.start);
        destinationView.setText(rideRequest.destination);
        dateAndTimeView.setText(rideRequest.dateAndTime);
    }

    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, authorPhotoView);
    }
}
