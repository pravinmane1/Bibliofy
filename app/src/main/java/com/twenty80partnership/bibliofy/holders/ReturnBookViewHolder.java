package com.twenty80partnership.bibliofy.holders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofy.R;


public class ReturnBookViewHolder extends RecyclerView.ViewHolder {

    private View mView;


    public ReturnBookViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name,String publication) {
        TextView tvPublication = mView.findViewById(R.id.tv_publication);
        TextView tvSubject = mView.findViewById(R.id.tv_subject);

        if (name!=null)
        tvSubject.setText(name);

        if (publication!=null)
        tvPublication.setText(publication);
    }

}
