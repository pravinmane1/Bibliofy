package com.twenty80partnership.bibliofy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setToolBar();
        setViews();
        setClickListeners();



    }

    private void setClickListeners() {

        number.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(number, InputMethodManager.SHOW_IMPLICIT);

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
                    finish();

            }
            else if(resultCode==RESULT_CANCELED){
                number.setText("");
            }

        }
    }

}
