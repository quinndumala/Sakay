package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.example.quinn.sakay.Models.Notif;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Quinn on 24/01/2017.
 */

public class NotifsViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView authorPhotoView;
    public TextView authorNameView;
    public TextView authorActionView;
    public TextView timeView;

    public NotifsViewHolder(View itemView){
        super(itemView);

        authorPhotoView = (CircleImageView) itemView.findViewById(R.id.notif_photo);
        authorNameView = (TextView) itemView.findViewById(R.id.notif_user_name);
        authorActionView = (TextView) itemView.findViewById(R.id.notif_user_description);
        timeView = (TextView) itemView.findViewById(R.id.notif_time);
    }

    public void bindToPost(Notif notif) {
        setPhoto(notif.facebookId);
        authorNameView.setText(notif.userName);
        authorActionView.setText(notif.action);

        timeView.setText(DateUtils.getRelativeTimeSpanString(
                (long) notif.timestamp).toString());

    }

    public void setPhoto(final String fId) {
        String imageUrl = "https://graph.facebook.com/" + fId + "/picture?height=150";
        GlideUtil.loadProfileIcon(imageUrl, authorPhotoView);
    }
}
