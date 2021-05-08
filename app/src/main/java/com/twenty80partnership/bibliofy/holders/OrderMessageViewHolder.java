package com.twenty80partnership.bibliofy.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofy.R;

public class OrderMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView messageTextView;
    public ImageView messageImageView;
    public TextView messengerTextView;
    public ImageView messengerImageView;

    public TextView messageTextView2;
    public ImageView messageImageView2;
    public TextView messengerTextView2;
    public ImageView messengerImageView2;

    public LinearLayout llOne,llTwo;

    public OrderMessageViewHolder(View v) {
        super(v);
        messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
        messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
        messengerImageView =  itemView.findViewById(R.id.messengerImageView);

        messageTextView2 = (TextView) itemView.findViewById(R.id.messageTextView2);
        messageImageView2 = (ImageView) itemView.findViewById(R.id.messageImageView2);
//            messengerTextView2 = (TextView) itemView.findViewById(R.id.messengerTextView2);
        messengerImageView2 =  itemView.findViewById(R.id.messengerImageView2);

        llOne = (LinearLayout)itemView.findViewById(R.id.ll_one);
        llTwo = itemView.findViewById(R.id.ll_two);
    }
}
