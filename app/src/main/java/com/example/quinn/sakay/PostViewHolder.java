package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Quinn on 14/12/2016.
 */

public class PostViewHolder extends RecyclerView.ViewHolder{
    public TextView startView;
    public TextView destinationView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public ImageView authorPhotoView;
    //public TextView bodyView;

    public PostViewHolder(View itemView){
        super(itemView);

        startView = (TextView) itemView.findViewById(R.id.post_title);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        starView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        destinationView = (TextView) itemView.findViewById(R.id.post_body);
        authorPhotoView = (ImageView) itemView.findViewById(R.id.post_author_photo);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        startView.setText(post.start);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        destinationView.setText(post.destination);

        starView.setOnClickListener(starClickListener);

    }

    public void setIcon(String url, final String authorId) {
        GlideUtil.loadProfileIcon(url, authorPhotoView);
//        mIconView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showUserDetail(authorId);
//            }
//        });
    }
}
