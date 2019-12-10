package com.twenty80partnership.bibliofy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PhoneNumberActivity extends AppCompatActivity {

    EditText number;
    Button sendOtp;
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        setToolBar();
        setViews();
        setClickListeners();



    }

    private void setClickListeners() {

        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (number.getText().toString().length()==10 ) {

                    if(count<10){
                        //sendOtp.setEnabled(false);
                        Intent verificationIntent = new Intent(PhoneNumberActivity.this, PhoneVerificationActivity.class);
                        verificationIntent.putExtra("number", "+91" + number.getText().toString());

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        DatabaseReference phoneRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

                        phoneRef.child("phone").setValue(number.getText().toString());
                        phoneRef.child("isPhoneVerified").setValue(false);

                        startActivityForResult(verificationIntent,2);
                    }
                    else {
                        Toast.makeText(PhoneNumberActivity.this,"Suspicious!!! you have exceeded your try limit",Toast.LENGTH_LONG).show();
                    }

                }
                else {
                    number.setError("Phone number length should be 10 digits");
                    number.requestFocus();
                }
            }
        });
    }

    private void setViews() {
        number = findViewById(R.id.number);
        sendOtp = findViewById(R.id.send_otp);
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        count++;
        if(requestCode==2){

            if (resultCode==RESULT_OK){
                Intent rIntent = new Intent();
                setResult(RESULT_OK, rIntent);

                Intent phoneNumberIntent = getIntent();


                    if (phoneNumberIntent.getStringExtra("loginFlow").equals("yes")){

                        startActivity(new Intent(PhoneNumberActivity.this,DashboardActivity.class));
                        finish();
                    }
                    else {
                    finish();
                }

            }
            else if(resultCode==RESULT_CANCELED){
                number.setText("");
            }

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
