package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class BookTemplateViewHolder extends RecyclerView.ViewHolder {

    public View mView;
    public TextView add, removeItem;
    public Spinner spPublication;
    public TextView tvOutOfStock,tvName;


    public BookTemplateViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        add = mView.findViewById(R.id.add);
        removeItem = mView.findViewById(R.id.remove_item);
        spPublication = mView.findViewById(R.id.sp_publication);
        tvOutOfStock = mView.findViewById(R.id.tv_out_of_stock);
        tvName = mView.findViewById(R.id.book_name);
    }

    public void updateHideStatus(Boolean defaultBook) {

        RelativeLayout bookCard = mView.findViewById(R.id.book_card);
        RelativeLayout.LayoutParams params;

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (defaultBook != null && !defaultBook) {
            params.height = 0;
            bookCard.setLayoutParams(params);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            bookCard.setLayoutParams(params);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setDetailsForTemplate(String author, String publication, String img,
                                      Integer originalPrice, Integer discountedPrice, Integer discount) {

        TextView mAuthor = mView.findViewById(R.id.tv_author);
        TextView mPublication = mView.findViewById(R.id.book_publication);
        TextView mOriginalPrice = mView.findViewById(R.id.book_original_price);
        TextView mDiscountedPrice = mView.findViewById(R.id.book_discounted_price);
        TextView mDiscount = mView.findViewById(R.id.book_discount);
        ImageView mImg = mView.findViewById(R.id.book_img);

        mOriginalPrice.setPaintFlags(mOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImg.setClipToOutline(true);
        }

        if (publication != null && !publication.isEmpty())
            mPublication.setText(publication);

        if (originalPrice!=null)
        mOriginalPrice.setText(originalPrice.toString());

        if (discountedPrice!=null)
        mDiscountedPrice.setText("₹ " + discountedPrice.toString());


        if (discount!=null && discount != 0) {
            mDiscount.setText(discount.toString() + "% off");
        } else {
            mDiscount.setText("");
        }

        if (img != null && !img.isEmpty()) {
            Picasso.get().load(img).into(mImg);
        } else {
            Picasso.get().load(R.drawable.sample_book).into(mImg);
        }

        if (author != null && !author.equals("")) {
            mAuthor.setText(author);
            mAuthor.setVisibility(View.VISIBLE);
        } else {
            mAuthor.setVisibility(View.GONE);
        }
    }
}
