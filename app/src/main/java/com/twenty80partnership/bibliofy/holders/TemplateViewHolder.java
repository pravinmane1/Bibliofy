package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofy.R;


public class TemplateViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public Spinner spPublication;
    public TextView tvSubject;

    public TemplateViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        spPublication = mView.findViewById(R.id.sp_publication);
        tvSubject = mView.findViewById(R.id.tv_subject);
    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name) {
        TextView itemName = mView.findViewById(R.id.tv_subject);

        if (name!=null)
        itemName.setText(name);
    }

}
