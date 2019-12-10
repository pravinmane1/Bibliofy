package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.twenty80partnership.bibliofy.R;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView orderCard;

    public OrderViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        orderCard = mView.findViewById(R.id.order_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String orderId, Integer count, String status, Context ctx) {

        TextView tOrderId =mView.findViewById(R.id.order_id);
        TextView tCount = mView.findViewById(R.id.count);
        TextView toStatus = mView.findViewById(R.id.order_status);

        final String[] states = {"Placed", "Confirmed", "Dispatched", "Delivered","Cancelled"};


        status = status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase();

        toStatus.setText("Order "+status);

        toStatus.setTextColor(ContextCompat.getColor(ctx,R.color.dheader));

        switch(status){
            case "Placed":
                toStatus.setBackgroundColor(ContextCompat.getColor(ctx,R.color.white));
                toStatus.setTextColor(ContextCompat.getColor(ctx,R.color.green));
                break;
            case "Confirmed":
                toStatus.setBackgroundColor(ContextCompat.getColor(ctx,R.color.aqua));
                break;
            case "Dispatched":
                toStatus.setBackgroundColor(ContextCompat.getColor(ctx,R.color.neongreen));
                break;
            case "Delivered":
                toStatus.setBackgroundColor(ContextCompat.getColor(ctx,R.color.light_green));
                break;
            case "Cancelled":
                toStatus.setTextColor(ContextCompat.getColor(ctx,R.color.white));
                toStatus.setBackgroundColor(ContextCompat.getColor(ctx,R.color.red));
                break;

        }

        tOrderId.setText(orderId);
        if (count!=null){
            if (count==1){
                tCount.setText(count.toString()+" Item");

            }
            else {
                tCount.setText(count.toString()+" Items");

            }

        }
       else {
            tCount.setText("loading...");

        }

    }

}
