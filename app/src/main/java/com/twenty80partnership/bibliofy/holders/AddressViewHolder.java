package com.twenty80partnership.bibliofy.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.twenty80partnership.bibliofy.R;

import androidx.recyclerview.widget.RecyclerView;

public class AddressViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public ImageView edit,remove;
    public RadioButton radioButton;

    public AddressViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        edit = mView.findViewById(R.id.edit);
        remove = mView.findViewById(R.id.remove);
        radioButton = mView.findViewById(R.id.select);
    }

    public void setDetails(String name,String number,String type,String address){


        TextView mName = mView.findViewById(R.id.name);
        TextView mNumber = mView.findViewById(R.id.number);
        TextView mType = mView.findViewById(R.id.type);
        TextView mAddress = mView.findViewById(R.id.address);

        mName.setText(name);
        mNumber.setText(number);
        mType.setText(type);
        mAddress.setText(address);

    }
}
