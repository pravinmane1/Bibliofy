package com.twenty80partnership.bibliofy.holders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;

public class StationaryListViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    public CardView stationaryListCard;
    long count;
    TextView tData;
    public StationaryListViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        stationaryListCard = mView.findViewById(R.id.stationaryListCard);

    }

    public  void setDetails(String name, String img, Context ctx,String location){
        ImageView tImg = mView.findViewById(R.id.img);
        TextView tName = mView.findViewById(R.id.name);
        tData = mView.findViewById(R.id.data);

        Picasso.get()
                .load(img)
                .into(tImg);

        tName.setText(name);


         count=0L;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference countDataRef= FirebaseDatabase.getInstance().getReference("CountData").child(mAuth.getCurrentUser().getUid()).child("stationary").child(location);
        countDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            count = dataSnapshot.getChildrenCount();
                if (count==1){
                    tData.setText(count+" item added.");
                    tData.setVisibility(View.VISIBLE);
                }
                else if (count>1) {
                    tData.setText(count + " items added.");
                    tData.setVisibility(View.VISIBLE);
                }
                else {
                    tData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
