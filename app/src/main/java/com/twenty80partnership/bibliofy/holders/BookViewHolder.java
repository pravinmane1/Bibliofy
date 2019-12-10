package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class BookViewHolder extends RecyclerView.ViewHolder {

    private View mView;
   public TextView add,removeItem;
   public LinearLayout removeLayout;


    public BookViewHolder(View itemView){
        super(itemView);
        mView=itemView;
        add=mView.findViewById(R.id.add);
        removeLayout=mView.findViewById(R.id.remove_layout);
        removeItem=mView.findViewById(R.id.remove_item);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String author, String publication, String img,
                           Integer originalPrice, Integer discountedPrice, Integer discount,
                           Boolean availability, Context ctx){


        TextView mName=mView.findViewById(R.id.book_name);
        TextView mAuthor=mView.findViewById(R.id.book_author);
        TextView mPublication=mView.findViewById(R.id.book_publication);
        TextView mOriginalPrice=mView.findViewById(R.id.book_original_price);
        TextView mDiscountedPrice=mView.findViewById(R.id.book_discounted_price);
        TextView mDiscount=mView.findViewById(R.id.book_discount);
        TextView mAvailability=mView.findViewById(R.id.book_availability);

        ImageView mImg=mView.findViewById(R.id.book_img);
        mOriginalPrice.setPaintFlags(mOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mName.setText(name);


        mAuthor.setText("Author: "+author);
        mPublication.setText(publication);
        mOriginalPrice.setText(originalPrice.toString());
        mDiscountedPrice.setText("₹ "+discountedPrice.toString());
        mDiscount.setText(discount.toString()+"% off");

        if (img!=null && !img.isEmpty()){
            Picasso.get()
                    .load(img)
                    .into(mImg);
        }
       else {
            Picasso.get()
                    .load(R.drawable.sample_book)
                    .into(mImg);
        }

        if (availability) {
                mAvailability.setText("AVAILABLE");
            mAvailability.setTextColor(ContextCompat.getColor(ctx,R.color.green));

        }
        else {
            mAvailability.setText("UNAVAILABLE");
            mAvailability.setTextColor(ContextCompat.getColor(ctx,R.color.red));
        }




    }
}
