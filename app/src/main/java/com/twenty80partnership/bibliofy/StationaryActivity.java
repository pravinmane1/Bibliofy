package com.twenty80partnership.bibliofy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.twenty80partnership.bibliofy.holders.StationaryListViewHolder;
import com.twenty80partnership.bibliofy.models.Stationary;

public class StationaryActivity extends AppCompatActivity {

    RecyclerView stationaryList;
    DatabaseReference stationaryListingRef;
    long count=0;
    Query q;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        stationaryListingRef = FirebaseDatabase.getInstance().getReference("SPPUstationaryListing");

        stationaryList = findViewById(R.id.recycler_view);

        stationaryList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
       // GridLayout gridLayout = new GridLayout(this);
        stationaryList.setLayoutManager(mLayoutManager);

        q = stationaryListingRef.orderByChild("priority");

        firebaseSearch(q);

    }

    private void firebaseSearch(Query query) {


        FirebaseRecyclerAdapter<Stationary, StationaryListViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Stationary, StationaryListViewHolder>(
                Stationary.class, R.layout.stationary_row, StationaryListViewHolder.class, query
        ) {

            @Override
            protected void populateViewHolder(final StationaryListViewHolder viewHolder, final Stationary model, final int position) {



                viewHolder.setDetails(model.getName(),model.getImg(),getApplicationContext(),model.getLocation());
                viewHolder.stationaryListCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(StationaryActivity.this,StationaryItemsActivity.class);
                        intent.putExtra("location",model.getLocation());

                        startActivity(intent);
                    }
                });







            }


        };


        stationaryList.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        firebaseSearch(q);
    }
}
