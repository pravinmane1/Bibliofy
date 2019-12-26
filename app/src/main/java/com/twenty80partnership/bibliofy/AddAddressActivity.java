package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.models.Address;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddAddressActivity extends AppCompatActivity {
    ProgressDialog pd;
    Button update;
    EditText pincode,buildingNameNumber,areaRoad,city,state,name,number,altNumber;
    RadioGroup radioGroup;
    RadioButton radioButton;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private Date currentTime;
    private boolean editCall=false;
    FirebaseAuth mAuth;
    DatabaseReference addressesRef;
    String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        mAuth = FirebaseAuth.getInstance();
        addressesRef = FirebaseDatabase.getInstance().getReference("Addresses").child(mAuth.getCurrentUser().getUid());



        pd = new ProgressDialog(AddAddressActivity.this);
        pd.setMessage("Loading");
        pd.setCancelable(false);


        pincode = findViewById(R.id.pincode);
        buildingNameNumber = findViewById(R.id.building_name_number);
        areaRoad = findViewById(R.id.area_road);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        altNumber = findViewById(R.id.alt_number);
        radioGroup = findViewById(R.id.type_group);
        update = findViewById(R.id.update);

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if (intent.getStringExtra("id")!=null){
            editCall = true;
            editId = intent.getStringExtra("id");
            setData(editId);
            Log.d("recycle debug",editId);

        }
        else {
            addBasicDetails();
        }




        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validate()){
                    addAddress();
                }

            }
        });


    }

    private void addBasicDetails() {

        FirebaseUser user = mAuth.getCurrentUser();

        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if(providerId.equals("firebase")){

                String phone = profile.getPhoneNumber();
                phone = phone.substring(3);
                String name1 = profile.getDisplayName();

                number.setText(phone);
                name.setText(name1);
                break;
            }

        }
    }

    private void setData(String id) {

        addressesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("recycle debug",dataSnapshot.toString());
                Address currentAddress = dataSnapshot.getValue(Address.class);

                pincode.setText(currentAddress.getPincode().toString());
                buildingNameNumber.setText(currentAddress.getBuildingNameNumber());
                areaRoad.setText(currentAddress.getAreaRoad());
                city.setText(currentAddress.getCity());
                state.setText(currentAddress.getState());
                name.setText(currentAddress.getName());
                number.setText( currentAddress.getNumber());

                if (currentAddress.getAltNumber()!=null){
                    altNumber.setText(currentAddress.getAltNumber());
                }

                switch (currentAddress.getType()){
                    case "HOME":
                        radioGroup.check(R.id.home);
                        break;
                    case "WORK":
                        radioGroup.check(R.id.work);
                        break;
                    case "OTHERS":
                        radioGroup.check(R.id.others);
                        break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validate(){

        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(selectedId);

        if (pincode.getText().toString().length()!=6){

            if (pincode.getText().toString().length()==0){

                Toast.makeText(AddAddressActivity.this,"Pincode is missing",Toast.LENGTH_SHORT).show();
            }
            else {

                Toast.makeText(AddAddressActivity.this,"Pincode length should be 6",Toast.LENGTH_SHORT).show();
            }

            pincode.requestFocus();
        }

        else if (buildingNameNumber.getText().toString().length()==0){

            Toast.makeText(AddAddressActivity.this,"Fill the Building name/number",Toast.LENGTH_SHORT).show();
            buildingNameNumber.requestFocus();
        }

        else if (areaRoad.getText().toString().length()==0){

            Toast.makeText(AddAddressActivity.this,"Fill area/Road",Toast.LENGTH_SHORT).show();
            areaRoad.requestFocus();
        }

        else if (city.getText().toString().length()==0){

            Toast.makeText(AddAddressActivity.this,"Fill City",Toast.LENGTH_SHORT).show();
            city.requestFocus();

        }

        else if (state.getText().toString().length()==0){

            Toast.makeText(AddAddressActivity.this,"Fill State",Toast.LENGTH_SHORT).show();
            state.requestFocus();
        }

        else if (name.getText().toString().length()==0){

            Toast.makeText(AddAddressActivity.this,"Enter your name",Toast.LENGTH_SHORT).show();
            name.requestFocus();
        }

        else if (number.getText().toString().length()!=10){


            //number.setError("Length should be 10");
            if (number.getText().toString().length()==0){

                Toast.makeText(AddAddressActivity.this,"enter your phone number",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(AddAddressActivity.this,"Phone digits should be 10",Toast.LENGTH_SHORT).show();
            }

            number.requestFocus();
        }

        else if (altNumber.getText().toString().length()!=10 && altNumber.getText().toString().length()!=0){


            Toast.makeText(AddAddressActivity.this,"Phone digits should be 10",Toast.LENGTH_SHORT).show();

            altNumber.requestFocus();
        }

        else if (selectedId==-1){

                Toast.makeText(AddAddressActivity.this,"Select address type",Toast.LENGTH_SHORT).show();
        }

        else {
            return true;
        }

        return false;

    }

    public void addAddress(){

        pd.show();

        currentTime = Calendar.getInstance().getTime();
        String date = dateFormat.format(currentTime);

        Address address = new Address();

        address.setPincode(Integer.parseInt(pincode.getText().toString()));
        address.setBuildingNameNumber(buildingNameNumber.getText().toString());
        address.setAreaRoad(areaRoad.getText().toString());
        address.setCity(city.getText().toString());
        address.setState(state.getText().toString());
        address.setName(name.getText().toString());
        address.setNumber(number.getText().toString());
        address.setTimeAdded(Long.parseLong(date));

        if (altNumber.getText().toString().length()==10){
            address.setAltNumber(altNumber.getText().toString());
        }

        address.setType(radioButton.getText().toString());

        String id;

        if (!editCall){
            id=addressesRef.push().getKey();
        }
        else {
            id = editId;
        }

        address.setId(id);

        addressesRef.child(id).setValue(address).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent rIntent = new Intent();

                if (task.isSuccessful()){
                    pd.dismiss();
                    setResult(RESULT_OK, rIntent);
                    Toast.makeText(getApplicationContext(),"Added",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    pd.dismiss();
                    setResult(RESULT_CANCELED,rIntent);
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
