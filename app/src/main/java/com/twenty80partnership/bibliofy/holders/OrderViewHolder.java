package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.models.Date;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public View mView;
    public CardView orderCard;

    public OrderViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        orderCard = mView.findViewById(R.id.order_card);

    }


    @SuppressLint("SetTextI18n")
    public void setDetails(String orderId, Integer daysForDelivery, Long userTimeAdded, Integer count, String status, Context ctx) {

        TextView tOrderId = mView.findViewById(R.id.order_id);
        TextView tCount = mView.findViewById(R.id.count);
        TextView toStatus = mView.findViewById(R.id.order_status);
        TextView tDate = mView.findViewById(R.id.date);
        TextView tDeliveryDate = mView.findViewById(R.id.delivery_date);
        LinearLayout llMain = mView.findViewById(R.id.ll_main);

        Date date = new Date();


        final String[] states = {"Placed", "Confirmed", "Dispatched", "Delivered", "Cancelled"};


        status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();

        if (!status.isEmpty())
            toStatus.setText("Order " + status + " !");

        String dateString = date.convertLongDateIntoSplit(userTimeAdded);

        if (dateString != null)
            tDate.setText(dateString);


        //tDeliveryDate.setText("Delivery expected by " + date.incrementDate(userTimeAdded, daysForDelivery));

        switch (status) {
            case "Placed":
                llMain.setBackgroundColor(ContextCompat.getColor(ctx, R.color.lightGreen2));
                break;
            case "Confirmed":
                llMain.setBackgroundColor(ContextCompat.getColor(ctx, R.color.lightGreen2));
                break;
            case "Dispatched":
                llMain.setBackgroundColor(ContextCompat.getColor(ctx, R.color.lightGreen2));
                break;
            case "Delivered":
                llMain.setBackgroundColor(ContextCompat.getColor(ctx, R.color.lightGreen2));
                break;
            case "Cancelled":
                llMain.setBackgroundColor(ContextCompat.getColor(ctx, R.color.lightRed2));
                break;

        }

        if (orderId != null)
            tOrderId.setText("ORDER ID: " + orderId);

        if (count != null) {
            if (count == 1) {
                tCount.setText(count.toString() + " Item");

            } else {
                tCount.setText(count.toString() + " Items");

            }

        } else {
            tCount.setText("loading...");

        }

    }


}
