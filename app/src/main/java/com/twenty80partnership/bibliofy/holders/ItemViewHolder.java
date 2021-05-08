package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public ConstraintLayout itemCard;
    private View mView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String pic) {

        TextView itemName = mView.findViewById(R.id.name);
        ImageView itemPic = mView.findViewById(R.id.pic);

        if (name != null && !name.isEmpty())
            itemName.setText(name);

        if (pic != null && !pic.isEmpty())
            Picasso.get().load(pic).into(itemPic);
    }

}

