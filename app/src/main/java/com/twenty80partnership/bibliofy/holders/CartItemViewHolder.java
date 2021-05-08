package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class CartItemViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout wishlist, remove, wishlistRemoveLayout;
    public View splitline;
    public TextView quantity;
    public ImageView plus, minus;
    private View mView;

    public CartItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        wishlist = mView.findViewById(R.id.add_to_wishlist);
        remove = mView.findViewById(R.id.remove);
        wishlistRemoveLayout = mView.findViewById(R.id.wishlist_remove_layout);
        splitline = mView.findViewById(R.id.splitline);
        quantity = mView.findViewById(R.id.item_quantity);
        plus = mView.findViewById(R.id.plus);
        minus = mView.findViewById(R.id.minus);
    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String publication, String img,
                           Integer originalPrice, Integer discountedPrice, Integer discount,
                           Integer quantity, String type, Integer count) {

        TextView itemName = mView.findViewById(R.id.item_name);
        TextView itemPublication = mView.findViewById(R.id.item_publication);
        LoaderImageView itemImg = mView.findViewById(R.id.item_img);
        TextView itemOriginalPrice = mView.findViewById(R.id.item_original_price);
        TextView itemDiscountedPrice = mView.findViewById(R.id.item_discounted_price);
        TextView itemDiscount = mView.findViewById(R.id.item_discount);
        TextView tvOutOfStock = mView.findViewById(R.id.tv_out_of_stock);

        itemOriginalPrice.setPaintFlags(itemOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if (name != null && !name.isEmpty())
            itemName.setText(name);

        if (publication != null && !publication.isEmpty())
            itemPublication.setText(publication);

        if (originalPrice != null)
            itemOriginalPrice.setText("₹ " + originalPrice.toString());

        if (discountedPrice != null)
            itemDiscountedPrice.setText("₹ " + discountedPrice.toString());

        if (discount != null)
            itemDiscount.setText(discount.toString() + "% off");

        if (img != null && !img.isEmpty()) {
            Picasso.get().load(img).into(itemImg);
        } else {
            itemImg.setImageResource(R.drawable.sample_book);
        }

        if (type != null && !type.isEmpty()) {
            if (type.equals("stationary")) {
                mView.findViewById(R.id.quantity_layout).setVisibility(View.VISIBLE);
            } else {
                mView.findViewById(R.id.quantity_layout).setVisibility(View.GONE);
            }
        }


        if (count != null) {
            if (count < quantity || count == 0) {
                tvOutOfStock.setVisibility(View.VISIBLE);
            } else {
                tvOutOfStock.setVisibility(View.GONE);
            }
        } else {
            tvOutOfStock.setVisibility(View.GONE);
        }
    }

}
