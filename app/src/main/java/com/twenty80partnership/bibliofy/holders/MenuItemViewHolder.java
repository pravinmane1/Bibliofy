package com.twenty80partnership.bibliofy.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class MenuItemViewHolder extends RecyclerView.ViewHolder {

    public View mView;

    public MenuItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setDetails(String top, String img, String title) {

        TextView itemTop = mView.findViewById(R.id.top);
        ImageView itemImg = mView.findViewById(R.id.img);
        TextView itemTitle = mView.findViewById(R.id.title);

        itemTop.setVisibility(View.VISIBLE);
        itemImg.setVisibility(View.VISIBLE);
        itemTitle.setVisibility(View.VISIBLE);

        if (top != null && !top.equals("")) {
            itemTop.setBackgroundResource(R.color.fire);
        } else {
            itemTop.setBackgroundResource(0);
        }
        itemTop.setText(top);

        if (img != null && !img.equals(""))
            Picasso.get().load(img).into(itemImg);
        else
            itemImg.setImageDrawable(null);

        if (title != null && !title.isEmpty())
            itemTitle.setText(title);

    }

    public void setDetails(String top, String img, String title, String type) {

        TextView itemTop = mView.findViewById(R.id.top);
        ImageView itemImg = mView.findViewById(R.id.img);
        TextView itemTitle = mView.findViewById(R.id.title);
        TextView itemMore = mView.findViewById(R.id.tv_more);


        if (type.equals("more")) {
            itemTop.setVisibility(View.GONE);
            itemImg.setVisibility(View.GONE);
            itemTitle.setVisibility(View.GONE);
            itemMore.setVisibility(View.VISIBLE);

            itemMore.setText("More...");
        } else {
            itemTop.setVisibility(View.VISIBLE);
            itemImg.setVisibility(View.VISIBLE);
            itemTitle.setVisibility(View.VISIBLE);
            itemMore.setVisibility(View.GONE);

            if (top != null && !top.equals("")) {
                itemTop.setBackgroundResource(R.color.fire);
            } else {
                itemTop.setBackgroundResource(0);
            }
            itemTop.setText(top);

            if (img != null && !img.equals(""))
                Picasso.get().load(img).into(itemImg);
            else
                itemImg.setImageDrawable(null);

            if (title != null && !title.isEmpty())
                itemTitle.setText(title);

        }


    }

    public void setDetailsYMAL(String top, String img, String title, String type) {

        ImageView itemImg = mView.findViewById(R.id.img);
        TextView itemMore = mView.findViewById(R.id.tv_more);


//        if (type.equals("more")) {
//            itemImg.setVisibility(View.GONE);
//            itemMore.setVisibility(View.VISIBLE);
//
//            itemMore.setText("More...");
//        }
//        else
            {
            itemImg.setVisibility(View.VISIBLE);
            itemMore.setVisibility(View.GONE);

            if (img != null && !img.equals(""))
                Picasso.get().load(img).into(itemImg);
            else
                itemImg.setImageDrawable(null);
        }


    }


}

