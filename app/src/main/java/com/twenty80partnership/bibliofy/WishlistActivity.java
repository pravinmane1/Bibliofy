package com.twenty80partnership.bibliofy;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.holders.WishlistViewHolder;
import com.twenty80partnership.bibliofy.models.Book;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.ItemMeta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WishlistActivity extends AppCompatActivity {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private RecyclerView bookList;
    private DatabaseReference cartRequestRef;
    private FirebaseAuth mAuth;
    private Date currentTime;
    private String uid;
    private DatabaseReference rootRef;
    private FirebaseRecyclerAdapter<ItemMeta, WishlistViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getCurrentUser().getUid());

        DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(mAuth.getCurrentUser().getUid());
        rootRef = FirebaseDatabase.getInstance().getReference();

        firebaseSearch(wishlistRef);


        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("wishlistlog", snapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void firebaseSearch(final DatabaseReference ref) {

        bookList = findViewById(R.id.recycler_view);
        bookList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        FirebaseRecyclerOptions<ItemMeta> options = new FirebaseRecyclerOptions.Builder<ItemMeta>()
                .setQuery(ref.orderByChild("timeAdded"), ItemMeta.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ItemMeta, WishlistViewHolder>(options) {
            @NonNull
            @Override
            public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.wishlist_row, parent, false);

                return new WishlistViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull WishlistViewHolder viewHolder, int position, @NonNull ItemMeta itemMeta) {

                Log.d("locationerror", "l: " + itemMeta.getItemLocation() + " -> " + itemMeta.getItemId());


                rootRef.child(itemMeta.getItemLocation()).child(itemMeta.getItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (itemMeta.getType() != null && itemMeta.getType().equals("books")) {


                            if (dataSnapshot.getValue() != null) {

                                Book model = dataSnapshot.getValue(Book.class);

                                //reset the viewholder before getting data from the countdata
                                viewHolder.removeItem.setVisibility(View.GONE);
                                viewHolder.add.setText("ADD");
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                                if (model.getAvailability()) {
                                    viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                } else {
                                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                                }


                                //set the count data to the viewholder
                                cartRequestRef.child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        //If user previously added the item
                                        if (dataSnapshot.exists()) {

                                            //if available set added
                                            if (model.getAvailability()) {

                                                viewHolder.removeItem.setVisibility(View.VISIBLE);
                                                viewHolder.add.setText("ADDED");
                                            }
                                            //if added item no longer available
                                            else {
                                                String id = model.getId();
                                                Toast.makeText(getApplicationContext(), "We're sorry your item is not available at moment", Toast.LENGTH_SHORT).show();
                                                cartRequestRef.child(id).removeValue();
                                            }


                                        }

                                        //If is user never changed the basic values of card UI
                                        else {

                                            viewHolder.removeItem.setVisibility(View.GONE);

                                            viewHolder.add.setText("ADD");
                                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                            //if book is available
                                            if (model.getAvailability()) {
                                                viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                            }
                                            // if book is unavailable
                                            else {
                                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));

                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(WishlistActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                                //set details of book to card
                                viewHolder.setDetails(model.getName(), model.getAuthor(), model.getPublication(), model.getImg(),
                                        model.getOriginalPrice(), model.getDiscountedPrice(), model.getDiscount(),
                                        model.getAvailability(), getApplicationContext(), "book", model.getVisibility(), 1);


                                //add button
                                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //check if available
                                        if (model.getAvailability()) {
                                            String id = model.getId();


                                            //get the time to add to item added time
                                            Date currentTime = Calendar.getInstance().getTime();
                                            String date = dateFormat.format(currentTime);

                                            //add to cart
                                            CartItem cartItem = new CartItem();
                                            cartItem.setTimeAdded(Long.parseLong(date));
                                            cartItem.setItemId(id);
                                            cartItem.setItemLocation(itemMeta.getItemLocation());
                                            cartItem.setQuantity(1);
                                            cartItem.setType(itemMeta.getType());

                                            cartRequestRef.child(id).setValue(cartItem);

                                            viewHolder.add.setText("ADDED");
                                            viewHolder.removeItem.setVisibility(View.VISIBLE);
                                        } else {
                                            Toast.makeText(WishlistActivity.this, "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                                viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String id = model.getId();


                                        cartRequestRef.child(id).removeValue();

                                        viewHolder.removeItem.setVisibility(View.GONE);


                                        viewHolder.add.setText("ADD");
                                        viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                        //if book is available
                                        if (model.getAvailability()) {
                                            viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                        }
                                        // if book is unavailable
                                        else {
                                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                                        }

                                    }
                                });


                            } else {
                                rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(itemMeta.getItemId()).removeValue();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(WishlistActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };


        bookList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onDestroy() {
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.stopListening();

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
