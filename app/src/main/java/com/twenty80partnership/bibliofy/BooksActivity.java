package com.twenty80partnership.bibliofy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.holders.ItemViewHolder;
import com.twenty80partnership.bibliofy.modules.Book;
import com.twenty80partnership.bibliofy.modules.Item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BooksActivity extends AppCompatActivity {
    TextView engg,others;
    ImageView intro;
    DatabaseReference introRef;
    RecyclerView itemList;
    String url=" ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_books);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        //LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        //itemList.setLayoutManager(mLayoutManager);
        itemList.setLayoutManager(new GridLayoutManager(this,2));





        engg=findViewById(R.id.engg);
        others=findViewById(R.id.others);
        intro=findViewById(R.id.intro_img);

        introRef=FirebaseDatabase.getInstance().getReference("Intro/SPPU/books/header");
        introRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                url=dataSnapshot.getValue(String.class);
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.sample_intro)
                        .into(intro);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BooksActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        engg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(BooksActivity.this,AvlCategoryActivity.class);
                i.putExtra("bookType","Regular");
                startActivityForResult(i,1);
            }
        });
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BooksActivity.this,ExtraCategoryActivity.class));
            }
        });

        DatabaseReference SPPUbooksListingRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing");
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

                        Intent intent = new Intent(BooksActivity.this,BookTypeActivity.class);
                        intent.putExtra("course",model.getId());
                        startActivity(intent);


                    }
                });

            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it present
        getMenuInflater().inflate(R.menu.dashboard_menu,menu);
        MenuItem cart=menu.findItem(R.id.cart);
        // cart.setIcon(R.drawable.search_icon);
        cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(BooksActivity.this,CartActivity.class));
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (resultCode == RESULT_OK)
            finish();
        }
    }
}
