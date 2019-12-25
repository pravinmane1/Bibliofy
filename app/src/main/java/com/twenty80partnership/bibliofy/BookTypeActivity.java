package com.twenty80partnership.bibliofy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.twenty80partnership.bibliofy.holders.ItemViewHolder;
import com.twenty80partnership.bibliofy.models.Item;

public class BookTypeActivity extends AppCompatActivity {

    RecyclerView itemList;
    private String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_type);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        //LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        //itemList.setLayoutManager(mLayoutManager);
        itemList.setLayoutManager(new GridLayoutManager(this,2));


        course = getIntent().getStringExtra("course");

        DatabaseReference SPPUbooksListingRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing").child(course)
                .child("category");
        Query query = SPPUbooksListingRef.orderByChild("priority");

        firebaseSearch(query);
    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(
                Item.class, R.layout.item_row, ItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final ItemViewHolder viewHolder, final Item model, final int position) {

                viewHolder.setDetails(model.getName(), model.getPic(), getApplicationContext());

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent intent = new Intent(BookTypeActivity.this,AvlCategoryActivity.class);

                        intent.putExtra("course",course);
                        intent.putExtra("bookType",model.getId());
                        startActivity(intent);

                    }
                });

            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }
}
