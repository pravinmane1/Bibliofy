package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.modules.Order;
import com.twenty80partnership.bibliofy.modules.OrderRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PayActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton radioButton;
    TextView placeOrder;
    TextView payableAmount;
    FirebaseAuth mAuth;
    DatabaseReference totalAmountRef;
    private String addressId;
    private ProgressDialog pd;
    private Intent rIntent;
    private Query q;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Log.d("debugOrder","acrt");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(PayActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Hold On... Placing Your Order...");

        radioGroup = findViewById(R.id.radio_group);
        placeOrder = findViewById(R.id.place_order);
        payableAmount = findViewById(R.id.payable_amount);


        mAuth = FirebaseAuth.getInstance();
        totalAmountRef = FirebaseDatabase.getInstance().getReference("PriceDetails").child(mAuth.getCurrentUser().getUid()).child("amountDiscounted");

        totalAmountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null){
                    payableAmount.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();
                if (intent.getStringExtra("id")!=null){
                    addressId = intent.getStringExtra("id");
                }
                else {
                    finish();
                }

                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);
                if (selectedId == -1){
                    Toast.makeText(getApplicationContext(),"Please select payment method",Toast.LENGTH_SHORT).show();
                }
                else {
                    switch (selectedId){
                        case R.id.cod:
                            makeOrderRequest(addressId,"cod");
                            Log.d("debugOrder","makeOrderCalled");
                            break;
                        case R.id.paytm:
                            Toast.makeText(getApplicationContext(),"paytm",Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.gpay:
                            Toast.makeText(getApplicationContext(),"google pay",Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            }
        });





    }

    private void makeOrderRequest(String addressId, String method) {
        Log.d("debugOrder","makeOrderCalled inside");

        pd.show();

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        Date currentDate = Calendar.getInstance().getTime();

        String date = dateFormat.format(currentDate);

        DatabaseReference orderRequestRef = FirebaseDatabase.getInstance().getReference("OrderReq").child(mAuth.getCurrentUser().getUid());

        final OrderRequest order = new OrderRequest();
        order.setAddressId(addressId);
        order.setMethod(method);
        order.setUserTimeAdded(Long.valueOf(date));

        final String tempOrderId = orderRequestRef.push().getKey();

                orderRequestRef.child(tempOrderId).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("debugOrder","request task completed");

                        rIntent = new Intent();

                        if (!task.isSuccessful()){
                            Log.d("debugOrder","task is not successful");

                            setResult(RESULT_CANCELED, rIntent);
                            Toast.makeText(PayActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        else {

                            Log.d("debugOrder","task is successful");

                            DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(mAuth.getCurrentUser().getUid());
                            q = userOrdersRef.orderByChild("tOid").equalTo(tempOrderId);


                            q.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                                        if (dataSnapshot.getValue()!=null){
                                            Log.d("debugOrder",dataSnapshot1.toString());

                                            String orderId = dataSnapshot1.child("orderId").getValue(String.class);
                                            Log.d("debugOrder","orderId "+orderId);

                                            q.removeEventListener(this);
                                            Order order = dataSnapshot1.getValue(Order.class);
                                            Log.d("debugOrder","orderId in object cheack: "+order.getOrderId());

                                            rIntent.putExtra("order",order);
                                            rIntent.putExtra("source","orderFlow");
                                            pd.dismiss();

                                            setResult(RESULT_OK, rIntent);
                                            finish();
                                        }
                                    }




                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("debugOrder","database error");
                                    pd.dismiss();
                                    Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
