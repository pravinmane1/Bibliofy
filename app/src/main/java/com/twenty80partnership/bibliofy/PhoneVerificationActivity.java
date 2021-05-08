package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.philio.pinentry.PinEntryView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
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
    Boolean autoVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        firebaseAuth=  FirebaseAuth.getInstance();

        Log.d("verificationLog","reaching");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

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

        enterOtp.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(enterOtp, InputMethodManager.SHOW_IMPLICIT);

        resend.setEnabled(false);
        timer.setVisibility(View.GONE);
        simple_p_b.setVisibility(View.GONE);

        resend.setOnClickListener(v -> {
            resend.setEnabled(false);
            sendVerificationCode(number);
        });

        verify.setOnClickListener(v -> {
            if (enterOtp.getText().toString().length()!=0)
            verifyCode(enterOtp.getText().toString());
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

            firebaseAuth.getCurrentUser().updatePhoneNumber(credential).addOnCompleteListener(this, task -> {
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


                    try {
                        throw task.getException();
                    } catch (FirebaseAuthUserCollisionException e) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(PhoneVerificationActivity.this);
                        alert.setTitle("Number Exists");
                        alert.setMessage("This number already exists with different user account. Please use a different number");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Okay", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();

                    } catch (Exception e) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(PhoneVerificationActivity.this);
                        alert.setTitle("Error Occured");
                        alert.setMessage(e.getMessage());
                        alert.setCancelable(false);
                        alert.setPositiveButton("Okay", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                    }
                    pd.dismiss();
                }

            });
        }
        else{
            pd.dismiss();
            Toast.makeText(PhoneVerificationActivity.this,"Error Occured", Toast.LENGTH_LONG).show();
        }

    }


    private void  sendVerificationCode(String s) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(s)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

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

            if (code!=null && !autoVerified) {
                autoVerified = true;
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

            try {
                throw e;
            } catch (FirebaseException firebaseException) {

                AlertDialog.Builder alert = new AlertDialog.Builder(PhoneVerificationActivity.this);
                alert.setTitle("Error occured");
                alert.setMessage(firebaseException.getMessage());
                alert.setCancelable(false);
                alert.setPositiveButton("Okay", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
