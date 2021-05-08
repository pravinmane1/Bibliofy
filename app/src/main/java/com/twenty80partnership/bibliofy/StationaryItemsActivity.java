package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofy.holders.StationaryItemViewHolder;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.CountData;
import com.twenty80partnership.bibliofy.models.SearchIndex;
import com.twenty80partnership.bibliofy.models.StationaryItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StationaryItemsActivity extends AppCompatActivity {
    RecyclerView stationaryList;
    DatabaseReference cartReqRef,selectCountRef;
    Date currentTime;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    ValueEventListener bottomUpdate;
    TextView showCartCount;
    RelativeLayout bottom;

    String type,typeName;
    RecyclerView itemList;
    EditText id;
    private FloatingActionButton mFloatingActionButton;
    MaterialSearchBar materialSearchBar;
    private Query query;
    String source;

    private FirebaseRecyclerAdapter<StationaryItem, StationaryItemViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_items);

        type = getIntent().getStringExtra("stationaryId");
        typeName = getIntent().getStringExtra("categoryName");
        source = getIntent().getStringExtra("source");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (type==null || type.equals("")){
            finish();
        }

        final DatabaseReference SPPUstationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary").child(type);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(typeName);



         showCartCount = findViewById(R.id.show_cart_count);
         bottom = findViewById(R.id.bottom_layout);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        cartReqRef=FirebaseDatabase.getInstance().getReference("CartReq").child( mAuth.getCurrentUser().getUid() );
        selectCountRef=FirebaseDatabase.getInstance().getReference("CountData").child( mAuth.getCurrentUser().getUid() ).child("stationary").child(type);


        //to update the bottom
        bottomUpdate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    long count = dataSnapshot.getChildrenCount();


                if (count == 1) {
                    showCartCount.setText(count + " item in Cart");
                }
                else if(count>1){
                    showCartCount.setText(count + " items in Cart");
                }

                if (count>0)
                    bottom.setVisibility(View.VISIBLE);
                else
                    bottom.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StationaryItemsActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        };

        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (source!=null && source.equals("cart")){
                    finish();
                }
            }
        });

        cartReqRef.addValueEventListener(bottomUpdate);

        itemList = findViewById(R.id.recycler_view);
        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this,2));


        firebaseSearch(SPPUstationaryRef);

    }

    public void firebaseSearch(Query q) {

        FirebaseRecyclerOptions<StationaryItem> options = new FirebaseRecyclerOptions.Builder<StationaryItem>()
                .setQuery(q, StationaryItem.class)
                .build();

        firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<StationaryItem, StationaryItemViewHolder>(options) {

            @NonNull
            @Override
            public StationaryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.stationary_item_row,parent,false);

                return new StationaryItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StationaryItemViewHolder viewHolder, int position, @NonNull StationaryItem model) {
                //reset the viewholder before getting data from the countdata
                model.setQuantityFlag("notAdded");
                model.setQuantity(1);
                viewHolder.removeItem.setVisibility(View.GONE);

                viewHolder.mQuantity.setText("1");

                viewHolder.add.setText("ADD");
                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);


                viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                //set the count data to the viewholder
                selectCountRef.child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //If user previously changed the value
                        if (dataSnapshot.exists()) {

                            //set quantity irrespective of the status
                            Integer addedQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                            model.setQuantity(addedQuantity);
                            viewHolder.mQuantity.setText(addedQuantity.toString());


                            //UI for already added item
                            if (dataSnapshot.child("status").getValue().equals(true) ) {

                                //if available set added


                                viewHolder.removeItem.setVisibility(View.VISIBLE);
                                model.setQuantityFlag("added");

                                viewHolder.add.setText("ADDED");
                                //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.neongreen));
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);

                                viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                                viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);

                                viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                                viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);

                            }

                            //UI for valued but non added item
                            else {

                                viewHolder.removeItem.setVisibility(View.GONE);
                                model.setQuantityFlag("notAdded");

                                viewHolder.add.setText("ADD");
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                                viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                                viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                                viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                            }
                        }

                        //If is user never changed the basic values of card UI
                        else {

                            viewHolder.removeItem.setVisibility(View.GONE);

                            model.setQuantity(1);
                            viewHolder.mQuantity.setText("1");
                            model.setQuantityFlag("notAdded");

                            viewHolder.add.setText("ADD");
                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);

                            viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                            viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                            viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                            viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



                //set details of book to card

                viewHolder.setDetails(model.getName(),model.getImg(),model.getAtr1(),model.getAtr2(),model.getAtr3(),
                        model.getAtr4(),model.getDiscount(),model.getDiscountedPrice()
                        ,model.getOriginalPrice(),model.getAvailability());


                //add button
                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String id=model.getId();

                        //set the added value to the database of count
                        CountData countData=new CountData();
                        countData.setStatus(true);
                        countData.setQuantity(model.getQuantity());
                        selectCountRef.child(id).setValue(countData);

                        //get the time to add to item added time
                        currentTime= Calendar.getInstance().getTime();
                        String date=dateFormat.format(currentTime);

                        //add to cart
                        CartItem cartItem=new CartItem();
                        cartItem.setTimeAdded(Long.parseLong(date));
                        cartItem.setQuantity(model.getQuantity());
                        cartItem.setItemId(id);
                        cartItem.setType("stationary");
                        cartItem.setItemLocation("SPPUstationary/"+type);

                        cartReqRef.child(id).setValue(cartItem);

                        //   if (model.getQuantityFlag().equals("notAdded")) {

                        viewHolder.add.setText("ADDED");
                        //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.neongreen));
                        viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);

                        model.setQuantityFlag("added");

                        viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                        viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);

                        viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                        viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);

                        viewHolder.removeItem.setVisibility(View.VISIBLE);


                    }
                });

                viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getQuantity()<100 ) {
                            if (model.getQuantityFlag().equals("added")){
                                viewHolder.add.setText("UPDATE");
                                //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                            }

                            model.setQuantity(model.getQuantity() + 1);
                            //set countdata to database
                            CountData countData=new CountData();
                            countData.setStatus(false);
                            countData.setQuantity(model.getQuantity());
                            selectCountRef.child(model.getId()).setValue(countData);

                            viewHolder.mQuantity.setText(model.getQuantity().toString());
                        }

                    }
                });

                viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getQuantity()>1) {
                            if (model.getQuantityFlag().equals("added")){
                                viewHolder.add.setText("UPDATE");
                                //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                            }
                            model.setQuantity(model.getQuantity() - 1);
                            //set countdata to database
                            CountData countData=new CountData();
                            countData.setStatus(false);
                            countData.setQuantity(model.getQuantity());
                            selectCountRef.child(model.getId()).setValue(countData);

                            viewHolder.mQuantity.setText(model.getQuantity().toString());
                        }
                    }
                });

                viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id=model.getId();


                        cartReqRef.child(id).removeValue();
                        selectCountRef.child(id).removeValue();

                        viewHolder.removeItem.setVisibility(View.GONE);

                        model.setQuantity(1);
                        viewHolder.mQuantity.setText("1");
                        model.setQuantityFlag("notAdded");

                        viewHolder.add.setText("ADD");
                        viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);

                        viewHolder.plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                        viewHolder.plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                        viewHolder.minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.original_height);
                        viewHolder.minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.original_width);

                    }
                });












//
//                viewHolder.setDetails(model.getBrand(),model.getImg(),getApplicationContext(),model.getAtr1(),model.getAtr2(),model.getAtr3(),
//                        model.getAtr4(),model.getDiscount(),model.getDiscountedPrice()
//                        ,model.getOriginalPrice(),model.getAvailability());

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
