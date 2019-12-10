package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.holders.AddressViewHolder;
import com.twenty80partnership.bibliofy.holders.CartItemViewHolder;
import com.twenty80partnership.bibliofy.modules.Address;
import com.twenty80partnership.bibliofy.modules.Book;
import com.twenty80partnership.bibliofy.modules.CartItem;

import java.util.ArrayList;
import java.util.Calendar;

public class AddressesActivity extends AppCompatActivity {
    CardView addAddress;
    RecyclerView itemList;
    FirebaseAuth mAuth;
    DatabaseReference addressesRef;
    boolean isSelectMode = false, isEdited = false;
    private String defaultAddressId;
    private String ediId;
    Intent returnIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addAddress = findViewById(R.id.add_new_address);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);


        Intent intent = getIntent();

        if (intent.getStringExtra("mode")!=null){

            if (intent.getStringExtra("mode").equals("select")){
                isSelectMode = true;
                defaultAddressId = intent.getStringExtra("defaultAddressId");
                returnIntent.putExtra("addressId",defaultAddressId);

            }

        }


        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddressesActivity.this,AddAddressActivity.class));
            }
        });

        Log.d("recycle debug","oncreate");

        mAuth = FirebaseAuth.getInstance();
        addressesRef = FirebaseDatabase.getInstance().getReference("Addresses").child(mAuth.getCurrentUser().getUid());

        Query query = addressesRef.orderByChild("timeAdded");

        if (isSelectMode){
            selectSearch(query);
        }
        else {
            firebaseSearch(query);
        }

    }

    private void selectSearch(Query query) {

        FirebaseRecyclerAdapter<Address, AddressViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Address, AddressViewHolder>(
                Address.class, R.layout.address_row, AddressViewHolder.class, query
        ) {

            @Override
            protected void populateViewHolder(final AddressViewHolder viewHolder, final Address model, final int position) {

                if (defaultAddressId.equals(model.getId())){
                    viewHolder.radioButton.setChecked(true);
                }
                else {
                    viewHolder.radioButton.setChecked(false);
                }

                viewHolder.remove.setVisibility(View.GONE);

                String combinedAddress = model.getBuildingNameNumber() +" "+
                        model.getAreaRoad()  +" "+
                        model.getCity()  +" "+
                        model.getState() + "-"+
                        model.getPincode();

                viewHolder.setDetails(model.getName(),model.getNumber(),model.getType(),combinedAddress);
                viewHolder.radioButton.setVisibility(View.VISIBLE);

                Log.d("recycle debug",model.getName()+model.getPincode());
                //remove button listener


                //edit button listener
                viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent editIntent = new Intent(AddressesActivity.this,AddAddressActivity.class);
                        editIntent.putExtra("id",model.getId());
                        startActivity(editIntent);

                        isEdited = true;
                        Log.d("resultIntent","is edited made true");

                        ediId = model.getId();
                    }
                });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.radioButton.setChecked(true);

                        setResult(RESULT_OK, returnIntent);
                        returnIntent.putExtra("addressId",model.getId());
                        finish();
                    }
                });

                viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.radioButton.setChecked(true);
                        setResult(RESULT_OK, returnIntent);
                        returnIntent.putExtra("addressId",model.getId());
                        finish();
                    }
                });

            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    public void firebaseSearch(Query q){

        Log.d("recycle debug","firebasesearch");

        FirebaseRecyclerAdapter<Address, AddressViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Address, AddressViewHolder>(
                Address.class, R.layout.address_row, AddressViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final AddressViewHolder viewHolder, final Address model, final int position) {


               String combinedAddress = model.getBuildingNameNumber() +" "+
                       model.getAreaRoad()  +" "+
                       model.getCity()  +" "+
                       model.getState() + "-"+
                       model.getPincode();

                viewHolder.setDetails(model.getName(),model.getNumber(),model.getType(),combinedAddress);

                Log.d("recycle debug",model.getName()+model.getPincode());
                //remove button listener
                viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(AddressesActivity.this);
                        builder.setMessage("Are you sure to remove this address?");
                        builder.setCancelable(true);

                        builder.setPositiveButton("remove",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String id  = model.getId();
                                        addressesRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(getApplicationContext(),"Successfully Removed",Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                });

                        builder.setNegativeButton("keep", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });

                        AlertDialog alert = builder.create();
                         alert.show();
                    }
                });

                //edit button listener
                viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent editIntent = new Intent(AddressesActivity.this,AddAddressActivity.class);
                        editIntent.putExtra("id",model.getId());
                        startActivity(editIntent);

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
    public void onBackPressed() {

        if (isEdited && defaultAddressId.equals(ediId)){
            Log.d("resultIntent","is edited satisfied");

            Intent returnIntent = new Intent();
            returnIntent.putExtra("addressId",defaultAddressId);
            setResult(RESULT_OK,returnIntent);
        }

        super.onBackPressed();
    }

}
