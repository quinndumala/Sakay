package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.quinn.sakay.Models.RideOffer;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Quinn on 21/12/2016.
 */

public class RideOfferViewHolder extends RecyclerView.ViewHolder{
    public TextView authorView;
    public CircleImageView authorPhotoView;
    public TextView startView;
    public TextView destinationView;
    public TextView vehicleView;
    public TextView dateAndTimeView;

    public RideOfferViewHolder(View itemView) {
        super(itemView);

        authorView = (TextView) itemView.findViewById(R.id.post_author);
        authorPhotoView = (CircleImageView) itemView.findViewById(R.id.post_author_photo);
        startView = (TextView) itemView.findViewById(R.id.offer_start_view);
        destinationView = (TextView) itemView.findViewById(R.id.offer_destination_view);
        vehicleView = (TextView) itemView.findViewById(R.id.offer_vehicle_view);
        dateAndTimeView = (TextView) itemView.findViewById(R.id.offer_dateAndTime_view);
    }

    public void bindToPost(RideOffer rideOffer) {
        setPhoto(rideOffer.facebookId);
        authorView.setText(rideOffer.author);
        startView.setText(rideOffer.start);
        destinationView.setText(rideOffer.destination);
        vehicleView.setText(rideOffer.vehicle);
        dateAndTimeView.setText(rideOffer.dateAndTime);
    }

    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, authorPhotoView);
    }

}
