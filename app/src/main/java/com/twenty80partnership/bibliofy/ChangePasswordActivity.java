package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChangePasswordActivity extends AppCompatActivity {
    TextView oldPassword,newPassword1,newPassword2;
    Button change;
    ProgressDialog pd;
    private String email;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


//set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(ChangePasswordActivity.this);
        pd.setMessage("verifying account");
        pd.setCancelable(false);

        oldPassword = findViewById(R.id.old_password);
        newPassword1 = findViewById(R.id.new_password1);
        newPassword2 = findViewById(R.id.new_password2);
        change = findViewById(R.id.change);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                if (newPassword1.getText().toString().equals(newPassword2.getText().toString())){
                    reauthenticate();
                }
                else {
                    pd.dismiss();
                    Toast.makeText(ChangePasswordActivity.this,"password mismatch",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void reauthenticate() {

        user = FirebaseAuth.getInstance().getCurrentUser();

        String email="";
        for (UserInfo profile:user.getProviderData()){

            if (profile.getProviderId().equals("password")){
                email = profile.getEmail();
            }

        }

        pd.setMessage("verifying Password");
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, oldPassword.getText().toString());


        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            pd.setMessage("setting up new password...");

                            user.updatePassword(newPassword2.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        pd.dismiss();
                                       Toast.makeText(ChangePasswordActivity.this,"password updated",Toast.LENGTH_SHORT).show();
                                       finish();
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(ChangePasswordActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
// [END reauthenticate]
    }


}
