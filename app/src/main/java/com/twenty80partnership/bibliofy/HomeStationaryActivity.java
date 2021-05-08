package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.holders.MenuItemViewHolder;
import com.twenty80partnership.bibliofy.models.MenuItem1;

public class HomeStationaryActivity extends AppCompatActivity {
    RecyclerView itemList;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_courses);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference homeMenuRef = FirebaseDatabase.getInstance().getReference("HomeStationaryList")
                .child("SPPU");

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(HomeStationaryActivity.this, 4));

        Log.d("debugLoad", "grid layout set");


        Query query = homeMenuRef.orderByChild("priority");

        firebaseSearch(query);

    }

    public void firebaseSearch(Query q) {


        FirebaseRecyclerOptions<MenuItem1> options = new FirebaseRecyclerOptions.Builder<MenuItem1>()
                .setQuery(q,MenuItem1.class)
                .build();

        FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder>(options) {

            @NonNull
            @Override
            public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item_row,parent,false);

                return new MenuItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuItemViewHolder viewHolder, int position, @NonNull MenuItem1 model) {
                Log.d("debugLoad", "populateViewHolder called for " + model.getId());
                viewHolder.mView.setAnimation(AnimationUtils.loadAnimation(HomeStationaryActivity.this, R.anim.fade_transition_animation));


                viewHolder.setDetails(model.getTop(), model.getImg(), model.getTitle());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(HomeStationaryActivity.this, model.getTitle(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getType().equals("books")) {
                            Intent bookIntent = new Intent(HomeStationaryActivity.this, AvlCategoryActivity.class);
                            bookIntent.putExtra("course", model.getCourse());
                            bookIntent.putExtra("bookType", model.getCategory());
                            bookIntent.putExtra("sem", model.getSem());
                            startActivity(bookIntent);
                        } else if (model.getType().equals("stationary")) {
                            Intent stationaryIntent = new Intent(HomeStationaryActivity.this, StationaryItemsActivity.class);
                            stationaryIntent.putExtra("stationaryId", model.getCategory());
                            stationaryIntent.putExtra("categoryName", model.getTitle());
                            startActivity(stationaryIntent);
                        }
                    }
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        Log.d("debugLoad", "itemList setadapter is called");

    }

}