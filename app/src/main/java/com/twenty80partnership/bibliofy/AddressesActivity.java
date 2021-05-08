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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.holders.AddressViewHolder;
import com.twenty80partnership.bibliofy.models.Address;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddressesActivity extends AppCompatActivity {
    FloatingActionButton addAddress;
    RecyclerView itemList;
    FirebaseAuth mAuth;
    DatabaseReference addressesRef;
    FirebaseDatabase db;
    boolean isSelectMode = false, isEdited = false;
    private String defaultAddressId;
    private String ediId;
    Intent returnIntent = new Intent();
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private Date currentTime;
    FirebaseRecyclerAdapter<Address, AddressViewHolder> firebaseRecyclerAdapter,firebaseRecyclerAdapter2;

    private HashMap<String,Integer> deliveryPinMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);

        db = FirebaseDatabase.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Addresses");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addAddress = findViewById(R.id.fab_add_new_address);
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

                if (intent.getStringExtra("defaultAddressId")!=null)
                defaultAddressId = intent.getStringExtra("defaultAddressId");

                returnIntent.putExtra("addressId",defaultAddressId);

                toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle("Select Address for Delivery");

                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        final Query query = addressesRef.orderByChild("timeAdded");

        if (isSelectMode){

            deliveryPinMap = new HashMap<>();

            db.getReference("Delivery").child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            deliveryPinMap.put(ds.child("pin").getValue(String.class) , ds.child("deliveryCharges").getValue(Integer.class));
                        }
                    }

                    selectSearch(query);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            firebaseSearch(query);
        }

    }

    private void selectSearch(Query query) {

        firebaseRecyclerAdapter2 = null;

        FirebaseRecyclerOptions <Address> options = new FirebaseRecyclerOptions.Builder<Address>()
                .setQuery(query,Address.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Address, AddressViewHolder>(options) {

            @NonNull
            @Override
            public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.address_row, parent, false);

                return new AddressViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AddressViewHolder viewHolder, int position, @NonNull Address model) {


                if (defaultAddressId!=null && defaultAddressId.equals(model.getId())){
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

                viewHolder.setDetails(model.getName(),addPhone(model.getNumber()),model.getType(),combinedAddress);
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



                        if (deliveryPinMap.containsKey(model.getPincode())){
                            viewHolder.radioButton.setChecked(true);

                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);


                            addressesRef.child(model.getId()).child("timeAdded").setValue(Long.parseLong(date));
                            setResult(RESULT_OK, returnIntent);
                            returnIntent.putExtra("addressId",model.getId());
                            returnIntent.putExtra("address",model);
                            finish();
                        }
                        else{
                            Toast.makeText(AddressesActivity.this, "Selected address is unavailable for delivery", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (deliveryPinMap.containsKey(model.getPincode())){
                            viewHolder.radioButton.setChecked(true);

                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);


                            addressesRef.child(model.getId()).child("timeAdded").setValue(Long.parseLong(date));
                            setResult(RESULT_OK, returnIntent);
                            returnIntent.putExtra("addressId",model.getId());
                            returnIntent.putExtra("address",model);
                            finish();
                        }
                        else{
                            Toast.makeText(AddressesActivity.this, "Selected address is unavailable for delivery", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public void firebaseSearch(Query q){

        firebaseRecyclerAdapter = null;

        FirebaseRecyclerOptions<Address> options = new FirebaseRecyclerOptions.Builder<Address>()
                .setQuery(q,Address.class)
                .build();


         firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<Address, AddressViewHolder>(
               options
        ) {

            @NonNull
            @Override
            public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());


                return new AddressViewHolder(inflater.inflate(R.layout.address_row, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull AddressViewHolder viewHolder, int position, @NonNull Address model) {
                String combinedAddress = model.getBuildingNameNumber() +" "+
                        model.getAreaRoad()  +" "+
                        model.getCity()  +" "+
                        model.getState() + "-"+
                        model.getPincode();

                viewHolder.setDetails(model.getName(),addPhone(model.getNumber()),model.getType(),combinedAddress);

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

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editIntent = new Intent(AddressesActivity.this,AddAddressActivity.class);
                        editIntent.putExtra("id",model.getId());
                        startActivity(editIntent);
                    }
                });

            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter2);
        firebaseRecyclerAdapter2.startListening();
    }

    private String addPhone(String numberFromDatabase){

        FirebaseUser user = mAuth.getCurrentUser();

        Boolean found = false;
        String num = "";

        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if(providerId.equals("firebase")){

                String phone = profile.getPhoneNumber();
                if(phone!=null && phone.length()==13){
                    found = true;
                    phone = phone.substring(3);
                    num = phone;
                }
                break;
            }

        }

        if (!found){
            if (numberFromDatabase!=null && numberFromDatabase.length()==10){
                num = numberFromDatabase;
            }
        }

        return num;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

//        if (isEdited && defaultAddressId.equals(ediId)){
//            Log.d("resultIntent","is edited satisfied");
//
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("addressId",defaultAddressId);
//            setResult(RESULT_OK,returnIntent);
//        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();

        if (firebaseRecyclerAdapter2!=null)
            firebaseRecyclerAdapter2.stopListening();
        super.onDestroy();
    }
}
