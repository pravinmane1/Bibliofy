package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.twenty80partnership.bibliofy.holders.ItemViewHolder;
import com.twenty80partnership.bibliofy.models.Item;

public class StationaryActivity extends AppCompatActivity {

    RecyclerView itemList;
    DatabaseReference SPPUstationaryListingRef;

    private FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView add = findViewById(R.id.add);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this, 4));


        SPPUstationaryListingRef = FirebaseDatabase.getInstance().getReference("SPPUstationaryListing");
        Query query = SPPUstationaryListingRef.orderByChild("priority");
        firebaseCategorySearch(query);


    }

    public void firebaseCategorySearch(Query q) {

        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(q,Item.class)
                .build();

        firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(options) {

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_row,parent,false);

                return new ItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position, @NonNull Item model) {


                viewHolder.setDetails(model.getName(), model.getPic());

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(StationaryActivity.this, StationaryItemsActivity.class);
                        intent.putExtra("stationaryId", model.getId());
                        intent.putExtra("categoryName", model.getName());
                        startActivity(intent);


                    }
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();
        super.onDestroy();
    }
}
