package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.philio.pinentry.PinEntryView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {
    PinEntryView enterOtp;
    Button verify,resend;
    TextView timer;
    String verificationId;
    FirebaseAuth firebaseAuth;
    ProgressBar simple_p_b;
    String number;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        setToolBar();


        Intent phoneIntent = getIntent();

        if (phoneIntent!=null){
            TextView info = findViewById(R.id.info);
            number = phoneIntent.getStringExtra("number");
            info.setText("Enter the otp sent to number "+ number);
            sendVerificationCode(number);
        }
        else {
            finish();
        }

        setProgressDialog();
        setViews();

        firebaseAuth=  FirebaseAuth.getInstance();



        resend.setEnabled(false);
        timer.setVisibility(View.GONE);
        simple_p_b.setVisibility(View.GONE);

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resend.setEnabled(false);
                sendVerificationCode(number);
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterOtp.getText().toString().length()!=0)
                verifyCode(enterOtp.getText().toString());
            }
        });


    }

    private void setViews() {
        enterOtp=findViewById(R.id.enter_otp);
        verify=findViewById(R.id.sign_in);
        resend = findViewById(R.id.resend);
        timer = findViewById(R.id.timer);
        simple_p_b = findViewById(R.id.simple_p_b);
    }

    private void setProgressDialog() {
        pd = new ProgressDialog(this);
        pd.setTitle("Verifying...");
        pd.setCancelable(false);
    }

    private void setToolBar() {
        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void verifyCode(String code) {
        pd.show();
        Toast.makeText(PhoneVerificationActivity.this,"verifycode is being called" ,Toast.LENGTH_LONG).show();

        if(verificationId!=null){

            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);

            firebaseAuth.getCurrentUser().updatePhoneNumber(credential).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("isPhoneVerified");
                        userDataRef.setValue(true);
                        pd.dismiss();
                        Toast.makeText(PhoneVerificationActivity.this,"number added successfully after verification",Toast.LENGTH_LONG).show();
                        Intent rIntent = new Intent();
                        setResult(RESULT_OK, rIntent);
                        finish();
                    }
                    else {
                        pd.dismiss();
                        Intent rIntent = new Intent();
                        setResult(RESULT_CANCELED, rIntent);
                        finish();
                        Toast.makeText(PhoneVerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        else{
            pd.dismiss();
            Toast.makeText(PhoneVerificationActivity.this,"Error Occured", Toast.LENGTH_LONG).show();
        }

    }


    private void  sendVerificationCode(String s) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                s,
                60,
                TimeUnit.SECONDS,
                this,
                    mCallbacks

        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(PhoneVerificationActivity.this,"code sent on number",Toast.LENGTH_LONG).show();
            verificationId=s;

            simple_p_b.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);

            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    timer.setText("Resend After\n" + millisUntilFinished / 1000+" seconds");
                }

                public void onFinish() {
                   resend.setEnabled(true);
                   timer.setVisibility(View.GONE);
                   simple_p_b.setVisibility(View.GONE);

                }
            }.start();

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
         //   Toast.makeText(PhoneVerificationActivity.this,"onverification completed "+code,Toast.LENGTH_LONG).show();

            if (code!=null) {
                enterOtp.setText(code);
                timer.setVisibility(View.GONE);
                simple_p_b.setVisibility(View.GONE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            timer.setVisibility(View.GONE);
            simple_p_b.setVisibility(View.GONE);
            Toast.makeText(PhoneVerificationActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
