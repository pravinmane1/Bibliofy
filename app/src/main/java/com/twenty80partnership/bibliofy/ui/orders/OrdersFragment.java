package com.twenty80partnership.bibliofy.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.OrderDetailsActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.holders.OrderViewHolder;
import com.twenty80partnership.bibliofy.models.Order;
import com.twenty80partnership.bibliofy.models.OrderThumb;

public class OrdersFragment extends Fragment {
    private RecyclerView itemList;
    private TextView tvNoOrders;
    private LottieAnimationView lottieAnimationView;
    private FirebaseRecyclerAdapter<OrderThumb, OrderViewHolder> firebaseRecyclerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_orders, container, false);


        itemList = root.findViewById(R.id.recycler_view);
        tvNoOrders = root.findViewById(R.id.tv_no_orders);
        lottieAnimationView = root.findViewById(R.id.la_no_orders);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.scrollToPositionWithOffset(0, 0);
        itemList.setLayoutManager(mLayoutManager);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference userOrdersThumbRef = FirebaseDatabase.getInstance().getReference("UserOrdersThumb").child(mAuth.getCurrentUser().getUid());
        Query query = userOrdersThumbRef.orderByChild("userTimeAdded");

        firebaseSearch(query);

        return root;
    }


    public void firebaseSearch(Query q) {

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    itemList.setVisibility(View.GONE);
                    tvNoOrders.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.VISIBLE);
                }
                else{
                    itemList.setVisibility(View.VISIBLE);
                    tvNoOrders.setVisibility(View.GONE);
                    lottieAnimationView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<OrderThumb> options = new FirebaseRecyclerOptions.Builder<OrderThumb>()
                .setQuery(q,OrderThumb.class)
                .build();

        firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<OrderThumb, OrderViewHolder>(options) {

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_row,parent,false);

                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull OrderThumb model) {

                viewHolder.setDetails(model.getOrderId(), model.getDaysForDelivery(), model.getUserTimeAdded(), model.getTotalItems(), model.getOrderStatus(), getContext());

                //remove button listener
                viewHolder.orderCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("orderId", model.getOrderId());
                        intent.putExtra("source", "myOrders");

                        startActivity(intent);

                    }
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();
    }

    //    @Override
//    public void onStart() {
//        super.onStart();
//
//        if (firebaseRecyclerAdapter!=null)
//            firebaseRecyclerAdapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        if (firebaseRecyclerAdapter!=null)
//            firebaseRecyclerAdapter.stopListening();
//    }
}