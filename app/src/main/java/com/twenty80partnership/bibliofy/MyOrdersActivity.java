package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.twenty80partnership.bibliofy.holders.AddressViewHolder;
import com.twenty80partnership.bibliofy.holders.OrderViewHolder;
import com.twenty80partnership.bibliofy.modules.Address;
import com.twenty80partnership.bibliofy.modules.Order;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyOrdersActivity extends AppCompatActivity {
    RecyclerView itemList;
    FirebaseAuth mAuth;
    DatabaseReference userOrdersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.scrollToPositionWithOffset(0, 0);
        itemList.setLayoutManager(mLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        userOrdersRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(mAuth.getCurrentUser().getUid());
        Query query = userOrdersRef.orderByChild("timeAdded");

        firebaseSearch(query);

    }


    public void firebaseSearch(Query q){

        Log.d("recycle debug","firebasesearch");

        final FirebaseRecyclerAdapter<Order, OrderViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Order, OrderViewHolder>(
                Order.class, R.layout.order_row, OrderViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final Order model, final int position) {

                String key = getRef(position).getKey();

                viewHolder.setDetails(key,model.getPriceDetails().getCount(),model.getStatus(),getApplicationContext());

                //remove button listener
                viewHolder.orderCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MyOrdersActivity.this,OrderDetailsActivity.class);
                        intent.putExtra("order",model);
                        intent.putExtra("source","myOrders");
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
}
