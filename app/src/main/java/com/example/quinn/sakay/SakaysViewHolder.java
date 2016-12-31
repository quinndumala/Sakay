package com.example.quinn.sakay;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Quinn on 31/12/2016.
 */

public class SakaysViewHolder extends RecyclerView.ViewHolder {
    public TextView sakayWithView;
    public TextView dateAndTimeView;
    public TextView destinationView;

    public SakaysViewHolder(View itemView) {
        super(itemView);
    }
}
