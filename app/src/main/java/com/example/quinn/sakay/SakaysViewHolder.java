package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.quinn.sakay.Models.Sakay;

/**
 * Created by Quinn on 31/12/2016.
 */

public class SakaysViewHolder extends RecyclerView.ViewHolder {
    public TextView dateAndTimeView;
    public TextView sakayWithView;
//    public TextView startView;
//    public TextView destinationView;
    public String sakayWithString;
    public String placesString;
    public TextView placesView;
    public Button viewDetails;

    public SakaysViewHolder(View itemView) {
        super(itemView);

        dateAndTimeView = (TextView) itemView.findViewById(R.id.sakay_date_time);
        sakayWithView = (TextView) itemView.findViewById(R.id.sakay_with_username);
//        startView = (TextView) itemView.findViewById(R.id.sakay_starting_point);
//        destinationView = (TextView) itemView.findViewById(R.id.sakay_destination);
        placesView = (TextView) itemView.findViewById(R.id.sakay_places);
        viewDetails = (Button) itemView.findViewById(R.id.sakay_button_view_details);
    }

    public void bindToPost(Sakay sakay, View.OnClickListener detailClickListener){
        dateAndTimeView.setText(sakay.dateAndTime);
        sakayWithString = "Sakay with " + sakay.otherAuthor;
        sakayWithView.setText(sakayWithString);
        placesString = sakay.start + " to " + sakay.destination;
        placesView.setText(placesString);

        viewDetails.setOnClickListener(detailClickListener);
    }
}
