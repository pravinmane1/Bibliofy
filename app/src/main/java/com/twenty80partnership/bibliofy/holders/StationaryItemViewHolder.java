package com.twenty80partnership.bibliofy.holders;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class StationaryItemViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    public Button add;
    public ImageView plus,minus;
    public TextView mQuantity;
    public TextView removeItem;

    public StationaryItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        add = mView.findViewById(R.id.add);
        plus=mView.findViewById(R.id.plus);
        minus=mView.findViewById(R.id.minus);
        mQuantity=mView.findViewById(R.id.s_quantity);
        removeItem=mView.findViewById(R.id.remove_item);
    }

    public  void setDetails(String name,String company,String img,Context ctx,
                            String ink,int pages,String type,String warranty,String features,int discountedPrice,int discount,int originalPrice){
        LoaderImageView tImg = mView.findViewById(R.id.s_img);
        TextView tCompany = mView.findViewById(R.id.company);
        TextView tPagesOrInk = mView.findViewById(R.id.pages_or_ink);
        TextView tType = mView.findViewById(R.id.type);
        TextView tDiscountedPrice = mView.findViewById(R.id.s_discounted_price);
        TextView tDiscount = mView.findViewById(R.id.s_discount);
        TextView tOriginalPrice = mView.findViewById(R.id.s_original_price);
        tOriginalPrice.setPaintFlags(tOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        Picasso.get()
                .load(img)
                .into(tImg);

        tCompany.setText(company);
        if (pages==0){
            tPagesOrInk.setText(ink);
        }
        else if (ink.equals("na")){
            tPagesOrInk.setText(String.valueOf(pages)+" Pages");
        }

        if (type.equals("na")){
            tType.setVisibility(View.GONE);
        }
        else {
            tType.setText(type);
        }
        tDiscountedPrice.setText("₹ "+String.valueOf(discountedPrice));
        tDiscount.setText(discount+"% off");
        tOriginalPrice.setText("₹ "+(originalPrice));
    }
}

