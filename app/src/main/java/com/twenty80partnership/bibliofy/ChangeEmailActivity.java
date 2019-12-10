package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class ChangeEmailActivity extends AppCompatActivity {

    EditText newEmail;
    ProgressDialog pd,pdChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        Button change = findViewById(R.id.change);
        newEmail = findViewById(R.id.new_email);

        pd = new ProgressDialog(ChangeEmailActivity.this);
        pd.setMessage("Reauthenticating...");
        pd.setCancelable(false);

        pdChange = new ProgressDialog(ChangeEmailActivity.this);
        pdChange.setMessage("Changing...");
        pdChange.setCancelable(false);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!newEmail.getText().toString().equals("")){
                    pd.show();
                    reauthenticate();
                }

            }
        });
    }

       public void reauthenticate() {
        // [START reauthenticate]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.



        AuthCredential credential = getGoogleCredentials();
        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(ChangeEmailActivity.this, "reauthenticated google success", Toast.LENGTH_LONG).show();
                            updateEmail(newEmail.getText().toString().trim());
                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(ChangeEmailActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
// [END reauthenticate]
    }

    public AuthCredential getGoogleCredentials() {

         GoogleSignInAccount account;
        account = GoogleSignIn.getLastSignedInAccount(this);
        String googleIdToken = account.getIdToken();
        // [START auth_google_cred]
        // [END auth_google_cred]
        return GoogleAuthProvider.getCredential(googleIdToken, null);
    }

    public void updateEmail(String nEmail) {
        // [START update_email]
        pdChange.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(nEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            pdChange.dismiss();
                           Toast.makeText(ChangeEmailActivity.this,"change success",Toast.LENGTH_LONG).show();
                           finish();
                        }
                        else {
                            pdChange.dismiss();
                            Toast.makeText(ChangeEmailActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
        // [END update_email]
    }
}
