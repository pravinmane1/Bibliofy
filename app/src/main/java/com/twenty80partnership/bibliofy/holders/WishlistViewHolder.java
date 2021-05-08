package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class WishlistViewHolder extends RecyclerView.ViewHolder {

    public TextView add, removeItem, tvOutOfStock;
    private View mView;


    public WishlistViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        add = mView.findViewById(R.id.add);
        removeItem = mView.findViewById(R.id.remove_item);
        tvOutOfStock = mView.findViewById(R.id.tv_out_of_stock);
    }

    public void setDetails(String name, String author, String publication, String img,
                           Integer originalPrice, Integer discountedPrice, Integer discount,
                           Boolean availability, Context ctx, String type, Boolean visibility, Integer count) {


        TextView mName = mView.findViewById(R.id.book_name);
        TextView mAuthor = mView.findViewById(R.id.book_author);
        TextView mPublication = mView.findViewById(R.id.book_publication);
        TextView mOriginalPrice = mView.findViewById(R.id.book_original_price);
        TextView mDiscountedPrice = mView.findViewById(R.id.book_discounted_price);
        TextView mDiscount = mView.findViewById(R.id.book_discount);
        TextView mAvailability = mView.findViewById(R.id.book_availability);
        ImageView mImg = mView.findViewById(R.id.book_img);

        mOriginalPrice.setPaintFlags(mOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImg.setClipToOutline(true);
        }

        if (name != null && !name.isEmpty())
            mName.setText(name);

        if (type.equals("stationary")) {
            if (author != null && !author.equals("")) {
                mAuthor.setText(author);
            }

            if (publication != null && !publication.equals("")) {
                mPublication.setText(publication);
            }

        } else {
            mPublication.setText(publication);
        }

        if (originalPrice!=null)
            mOriginalPrice.setText(originalPrice.toString());

        if (discountedPrice!=null)
            mDiscountedPrice.setText("₹ " + discountedPrice.toString());

        if (discount!=null && discount!=0)
            mDiscount.setText(discount.toString() + "% off");
        else
            mDiscount.setText("");


        if (img != null && !img.isEmpty()) {
            Picasso.get().load(img).into(mImg);
        } else {
            Picasso.get().load(R.drawable.sample_book).into(mImg);
        }

        if (availability != null) {
            if (availability) {
                mAvailability.setText("AVAILABLE");
                mAvailability.setTextColor(ContextCompat.getColor(ctx, R.color.green));

            } else {
                mAvailability.setText("UNAVAILABLE");
                mAvailability.setTextColor(ContextCompat.getColor(ctx, R.color.red));
            }
        }

    }
}
