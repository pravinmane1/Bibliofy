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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EmailLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        mAuth = FirebaseAuth.getInstance();

        TextView signup = findViewById(R.id.sign_up);

        TextView forgotPassword = findViewById(R.id.forgot_password);

        Button login = (Button) findViewById(R.id.login);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(EmailLoginActivity.this,EmailSignUpActivity.class);
                startActivityForResult(signUp,1);
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailLoginActivity.this,PasswordActivity.class)    );
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();

            }
        });


    }

    private void startLogin() {
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);

        pd = new ProgressDialog(EmailLoginActivity.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        if (email.getText().toString().equals("")){
            email.setError("Email can't be empty");
            pd.dismiss();
        }
        else if (password.getText().toString().equals("")){
            password.setError("Password Can't be empty");
            pd.dismiss();
        }
        else {

            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        //taking system time
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        Date currentTime= Calendar.getInstance().getTime();
                        Long date=Long.parseLong(dateFormat.format(currentTime));

                        DatabaseReference loginInfoRef = FirebaseDatabase.getInstance().getReference("LoginInfo").child(mAuth.getCurrentUser().getUid())
                                .push().child("time");
                        loginInfoRef.setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    pd.dismiss();
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                }
                                else{
                                    finish();

                                    Toast.makeText(EmailLoginActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }
                    else {
                        pd.dismiss();
                        Log.d("excepdetail",task.getException().toString()+" exception");
                        Toast.makeText(EmailLoginActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode==RESULT_OK){
            finish();
        }
    }
}
