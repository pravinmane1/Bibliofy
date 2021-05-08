package com.twenty80partnership.bibliofy.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofy.R;

public class AddressViewHolder extends RecyclerView.ViewHolder {

    public ImageView edit, remove;
    public RadioButton radioButton;
    private final View mView;

    public AddressViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        edit = mView.findViewById(R.id.edit);
        remove = mView.findViewById(R.id.remove);
        radioButton = mView.findViewById(R.id.select);
    }

    public void setDetails(String name, String number, String type, String address) {


        TextView mName = mView.findViewById(R.id.name);
        TextView mNumber = mView.findViewById(R.id.number);
        TextView mType = mView.findViewById(R.id.type);
        TextView mAddress = mView.findViewById(R.id.address);

        if (name != null && !name.isEmpty())
            mName.setText(name);

        if (number != null && !number.isEmpty())
            mNumber.setText(number);

        if (type != null && !type.isEmpty())
            mType.setText(type);

        if (address != null && !address.isEmpty())
            mAddress.setText(address);

    }
}
