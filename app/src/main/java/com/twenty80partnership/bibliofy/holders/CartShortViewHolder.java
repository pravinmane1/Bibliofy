package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

import androidx.recyclerview.widget.RecyclerView;

public class CartShortViewHolder extends RecyclerView.ViewHolder {

private View mView;

public CartShortViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

        }

@SuppressLint("SetTextI18n")
public void setDetails(String name, String publication,Integer discountedPrice) {

       TextView itemName=mView.findViewById(R.id.name);
        TextView itemPublication=mView.findViewById(R.id.publicatioin);
        TextView itemDiscountedPrice=mView.findViewById(R.id.price);

        itemName.setText(name);
        itemPublication.setText(publication);
        itemDiscountedPrice.setText("₹ "+discountedPrice.toString());
        }

        }

