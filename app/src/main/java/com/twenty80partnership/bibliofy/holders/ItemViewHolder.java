package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView itemCard;
    public ImageView more;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String pic, Context ctx) {

        TextView itemName=mView.findViewById(R.id.name);
        itemName.setText(name);

            ImageView itemPic=mView.findViewById(R.id.pic);
            Picasso.get().load(pic).into(itemPic);
    }

}

