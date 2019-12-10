package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twenty80partnership.bibliofy.R;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView orderCard;

    public OrderViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        orderCard = mView.findViewById(R.id.order_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String orderId,Integer daysForDelivery,Long userTimeAdded, Integer count, String status, Context ctx) {

        TextView tOrderId =mView.findViewById(R.id.order_id);
        TextView tCount = mView.findViewById(R.id.count);
        TextView toStatus = mView.findViewById(R.id.order_status);
        TextView tDate = mView.findViewById(R.id.date);
        TextView tDeliveryDate = mView.findViewById(R.id.delivery_date);

        userTimeAdded = userTimeAdded/1000000000L;
        Long day = userTimeAdded  % 100;

        userTimeAdded = userTimeAdded/100;

        Long month = userTimeAdded%100;

        Long year = userTimeAdded/100;




        final String[] states = {"Placed", "Confirmed", "Dispatched", "Delivered","Cancelled"};


        status = status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase();

        toStatus.setText("Order "+status);

        toStatus.setTextColor(ContextCompat.getColor(ctx,R.color.dheader));

        tDate.setText(day+"-"+month+"-"+year);


        //for 1 week after date
        ArrayList<Integer> months = new ArrayList<>();

        int incr = daysForDelivery;

        int newDay = day.intValue();
        int newMonth = month.intValue();
        int newYear = year.intValue();



        months.add(31);
        months.add(29);
        months.add(31);
        months.add(30);
        months.add(31);
        months.add(30);
        months.add(31);
        months.add(31);
        months.add(30);
        months.add(31);
        months.add(30);
        months.add(31);

        if ( (day + incr) > months.get(month.intValue()-1)){

            if (month==12){
                newYear = year.intValue()+1;
                newMonth = 1;

                newDay = day.intValue() - (31 - incr);
            }
            else {
                newYear = year.intValue();
                newMonth = month.intValue()+1;
                newDay = incr -  (months.get(month.intValue()-1) - day.intValue()) ;
            }
        }
        else{
            newDay = day.intValue() + incr;
            newMonth = month.intValue();
            newYear = year.intValue();
        }

        tDeliveryDate.setText("Delivery expected by "+newDay+"-"+newMonth+"-"+newYear);

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

        tOrderId.setText("Order ID: "+orderId.substring(orderId.length() - 6));
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


    private Long createAbsoluteDate(Long year, Long month, Long day) {
        year = year*10000;
        month = month*100;

        return year+month+day;
    }

}
