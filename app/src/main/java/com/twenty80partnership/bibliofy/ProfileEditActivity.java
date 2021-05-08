package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.models.Address;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileEditActivity extends AppCompatActivity {

    ImageView imgMale,imgFemale,photo;
    Button save;
    EditText editName;
    TextView email,phoneStatus;
    RelativeLayout changePassword;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String oldName="";
    ProgressDialog pd;
    private TextView userPhone;
    FirebaseUser firebaseUser;
    private ProgressDialog pdSave;
    String sGender;
    private Handler h;
    private RadioButton maleSelect,femaleSelect;
    private RadioGroup rg1,rg2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());;

        oldName = mAuth.getCurrentUser().getDisplayName();

        save = findViewById(R.id.save);
        userPhone = findViewById(R.id.user_phone);
        phoneStatus = findViewById(R.id.phone_status);
        editName = findViewById(R.id.edit_name);
        email = findViewById(R.id.user_email);
        photo = findViewById(R.id.edit_photo);
        TextView changeEmail = findViewById(R.id.change_email);
        changePassword = findViewById(R.id.change_password);
        maleSelect = findViewById(R.id.male_select);
        femaleSelect = findViewById(R.id.female_select);
        rg1 = findViewById(R.id.rg1);
        rg2 = findViewById(R.id.rg2);
        imgFemale = findViewById(R.id.img_female);
        imgMale = findViewById(R.id.img_male);

        pd = new ProgressDialog(ProfileEditActivity.this);
        pd.setMessage("Removing...");
        pd.setCancelable(false);


        pdSave = new ProgressDialog(ProfileEditActivity.this);
        pdSave.setMessage("Saving...");
        pdSave.setCancelable(false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
        builder.setMessage("Once phone number is removed , It can't be undone. You have to reverify to add the same or different number. ");
        builder.setCancelable(true);

        builder.setPositiveButton("Remove",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        pd.show();

                       // reauthenticate();

                        mAuth.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                   updatePhone();
                                   Toast.makeText(ProfileEditActivity.this,"Phone number removed successfully: ",Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(ProfileEditActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
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

        maleSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rg2.clearCheck();
                femaleSelect.setChecked(false);
                maleSelect.setChecked(true);
                Picasso.get().load(R.drawable.profile_boy).into(photo);
            }
        });

        femaleSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rg1.clearCheck();
                maleSelect.setChecked(false);
                femaleSelect.setChecked(true);
                Picasso.get().load(R.drawable.profile_girl).into(photo);
            }
        });

        imgMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rg2.clearCheck();
                maleSelect.setChecked(true);
                femaleSelect.setChecked(false);
                Picasso.get().load(R.drawable.profile_boy).into(photo);
            }
        });

        imgFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rg1.clearCheck();
                maleSelect.setChecked(false);
                femaleSelect.setChecked(true);
                Picasso.get().load(R.drawable.profile_girl).into(photo);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pdSave.show();

                String nameS = editName.getText().toString();

                //if ( !nameS.equals(firebaseUser.getDisplayName()) ) {

                    updateName(nameS);
                //}
                //else {
                //    pdSave.dismiss();
                 //   finish();
                //}
//                if (!course.getText().toString().equals("")){
//
//                        userRef.child("course").setValue(course.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//
//                                if (task.isSuccessful()){
//                                }
//                                else {
//                                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//                }
            }
        });

        phoneStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneStatus.getText().toString().equals("Remove")){
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else if (phoneStatus.getText().toString().equals("Add")){
                    Intent phoneIntent = new Intent(ProfileEditActivity.this,PhoneNumberActivity.class);
                    phoneIntent.putExtra("loginFlow","no");
                    startActivityForResult(phoneIntent,3);
                }
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileEditActivity.this,ChangeEmailActivity.class));
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(ProfileEditActivity.this,ChangePasswordActivity.class));
            }
        });

        setUserData();  // photo name email
    }

    @Override
    protected void onStart() {
        super.onStart();

        updatePhone();  // set phone details with ui
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePhone();
        setUserData();
    }
    private void updateName(String name) {

        for (UserInfo profile : firebaseUser.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if (providerId.equals("firebase")) {

                Log.d("userstatus", "firebase found");



                    Log.d("userstatus", "oldname:" + profile.getDisplayName() + " changing to: " + name.trim());

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build();

                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {


                                String name = firebaseUser.getDisplayName();

                                editName.setText(name);

                                userRef.child("name").setValue(name);
                                userRef.child("searchName").setValue(name.toLowerCase());

                                if(maleSelect.isChecked()){
                                    userRef.child("gender").setValue("male").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            pdSave.dismiss();
                                            finish();
                                        }
                                    });
                                }
                                else{
                                    userRef.child("gender").setValue("female").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            pdSave.dismiss();
                                            finish();
                                        }
                                    });
                                }

                            //    Toast.makeText(ProfileEditActivity.this, name, Toast.LENGTH_LONG).show();
                                Log.d("userstatus", "updated");


                            } else {
                                Toast.makeText(ProfileEditActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("userstatus", task.getException().getMessage());
                                pdSave.dismiss();
                                finish();
                            }

                        }
                    });

                break;
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==3){
            if (resultCode == RESULT_OK){
                updatePhone();
                Log.d("userdata","updating phone");
            }
        }
    }

    private void setUserData() {


        editName.setText(firebaseUser.getDisplayName());
        email.setText(firebaseUser.getEmail());

        boolean passwordUser = false;

        for (UserInfo profile:firebaseUser.getProviderData()){
            if (profile.getProviderId().equals("password")){
                changePassword.setVisibility(View.VISIBLE);
                passwordUser = true;
                break;
            }
        }

        if (!passwordUser){
            changePassword.setVisibility(View.GONE);
        }

        userRef.child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    sGender = dataSnapshot.getValue(String.class);


                    if (sGender!=null && !sGender.equals("")){

                        if(sGender.equals("male")){
                            femaleSelect.setChecked(false);
                            maleSelect.setChecked(true);
                            Picasso.get().load(R.drawable.profile_boy).into(photo);
                        }
                        else {
                            femaleSelect.setChecked(true);
                            maleSelect.setChecked(false);
                            Picasso.get().load(R.drawable.profile_girl).into(photo);
                        }
                    }
                }
                else{
                    Toast.makeText(ProfileEditActivity.this,"no gender set for user",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePhone() {
        Log.d("userdata","setPhoneProvider is called from requestcode 3");

        boolean isPhone=false;

        for (UserInfo profile : firebaseUser.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();


            if(providerId.equals("phone")){
                isPhone=true;
                String phone = profile.getPhoneNumber();
                userPhone.setText(phone);
                phoneStatus.setText("Remove");
                phoneStatus.setTextColor(getResources().getColor(R.color.red));
                break;
            }


            // UID specific to the provider
            String uid = profile.getUid();

            // Name, email address, and profile photo Url
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
        }

        if (!isPhone){
            userPhone.setText("Phone Number");
            pd.dismiss();
            phoneStatus.setText("Add");
            phoneStatus.setTextColor(getResources().getColor(R.color.green));
        }
        // [END get_provider_data]
    }


//    public void reauthenticate() {
//        // [START reauthenticate]
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        // Get auth credentials from the user for re-authentication. The example below shows
//        // email and password credentials but there are multiple possible providers,
//        // such as GoogleAuthProvider or FacebookAuthProvider.
//
//
//
//        AuthCredential credential = getGoogleCredentials();
//        // Prompt the user to re-provide their sign-in credentials
//        user.reauthenticate(credential)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(ProfileEditActivity.this,"reauthenticated google",Toast.LENGTH_LONG).show();
//                    }
//                });
//// [END reauthenticate]
//    }
//
//    public AuthCredential getGoogleCredentials() {
//
//         GoogleSignInAccount account;
//        account = GoogleSignIn.getLastSignedInAccount(this);
//        String googleIdToken = account.getIdToken();
//        // [START auth_google_cred]
//        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
//        // [END auth_google_cred]
//        return credential;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
