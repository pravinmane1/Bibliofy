package com.twenty80partnership.bibliofy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class PasswordActivity extends AppCompatActivity {

    Button submit;
    EditText email;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            boolean b = imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        submit = findViewById(R.id.submit);
        email = findViewById(R.id.email);

        email.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetLink();
            }
        });

    }

    private void sendResetLink() {

        final ProgressDialog pd = new ProgressDialog(PasswordActivity.this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        String emailAddress = email.getText().toString().trim();

        if (emailAddress.equals("")) {
            pd.dismiss();
            email.setError("Please enter your email");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();


        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Log.d("showdata", "Email sent.");

                            TextView info = findViewById(R.id.info);
                            hideKeyboard(PasswordActivity.this);
                            email.setVisibility(View.GONE);
                            submit.setVisibility(View.GONE);
                            info.setVisibility(View.VISIBLE);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                email.setError("Please enter valid email address");
                                email.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e) {
                                email.setError("User does not exists!!!");
                                email.requestFocus();
                            } catch (Exception e) {
                                Toast.makeText(PasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                Log.e("showdata", e.getMessage());
                            }
                            pd.dismiss();
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
