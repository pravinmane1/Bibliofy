package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.models.Address;

public class ProfileActivity extends AppCompatActivity {


    private TextView college;
    private TextView course;
    private TextView addresses;

    TextView phone_status,email_status;

    ProgressDialog pd;
    FirebaseAuth mAuth;
    private boolean addressPresent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Sending verification email to "+mAuth.getCurrentUser().getEmail() );


        ImageView cancel = findViewById(R.id.cancel);
        ImageView edit = findViewById(R.id.edit);
        addresses = findViewById(R.id.addresses);

       phone_status = findViewById(R.id.phone_status);
       email_status = findViewById(R.id.email_status);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ProfileActivity.this,ProfileEditActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        phone_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone_status.getText().toString().equals("add")){

                    startActivity(new Intent(ProfileActivity.this,PhoneNumberActivity.class));

                }

            }
        });


        email_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email_status.getText().toString().equals("verify")) {
                    pd.show();
                    sendEmailVerification();
                }
            }
        });


        addresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressPresent){
                    startActivity(new Intent(ProfileActivity.this,AddressesActivity.class));
                }
                else {
                    //startActivity(new Intent(ProfileActivity.this,AddressesActivity.class));
                    startActivityForResult(new Intent(ProfileActivity.this,AddAddressActivity.class),1);
                }
            }
        });

       // updateEmail();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserData();
        getProviderData();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setUserData();
                getProviderData();
            }
        });

    }

    public void sendEmailVerification() {
        // [START send_email_verification]

        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            pd.dismiss();
                            final AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this);
                            builder1.setMessage("Verification e-mail is sent to " + mAuth.getCurrentUser().getEmail() );
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    });

                            AlertDialog alert = builder1.create();
                            alert.show();
                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
        // [END send_email_verification]
    }

    private void setUserData() {

        TextView name = findViewById(R.id.user_name);
        TextView email = findViewById(R.id.user_email);
        ImageView photo = findViewById(R.id.user_photo);
        college = findViewById(R.id.user_college);
        course = findViewById(R.id.user_course);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        boolean emailVerified = firebaseUser.isEmailVerified();
        if (emailVerified){
            //Toast.makeText(ProfileActivity.this,"verified",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(ProfileActivity.this,"not verified",Toast.LENGTH_LONG).show();
        }

        Picasso.get()
                .load(firebaseUser.getPhotoUrl())
                .placeholder(R.drawable.userdisplay)
                .into(photo);

        name.setText(firebaseUser.getDisplayName());
        email.setText(firebaseUser.getEmail());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String sCollege = dataSnapshot.child("college").getValue(String.class);
                String sCourse = dataSnapshot.child("course").getValue(String.class);

                if (sCollege != null){
                    college.setText(sCollege);
                }
                if (sCourse != null){
                    course.setText(sCourse);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference addressesRef = FirebaseDatabase.getInstance().getReference("Addresses").child(mAuth.getCurrentUser().getUid());

        Query q = addressesRef.orderByChild("timeAdded");

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    Address model = new Address();

                    for(DataSnapshot addressSnapshot:dataSnapshot.getChildren()){
                        model = addressSnapshot.getValue(Address.class);
                    }
                    String completeAddress = model.getBuildingNameNumber() +" "+
                            model.getAreaRoad()  +" "+
                            model.getCity()  +" "+
                            model.getState() + "-"+
                            model.getPincode();

                    addresses.setText(completeAddress);
                    addresses.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_action_forward_arrow,0);
                }
                else {
                    addressPresent = false;
                    addresses.setText("Add Address");
                    addresses.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_action_add,0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getProviderData() {
        // [START get_provider_data]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("userdata","uid: "+user.getUid());
        Log.d("userdata","user is not nll");



        if (user.isEmailVerified()){
            email_status.setText("verified");
            email_status.setTextColor( getResources().getColor(R.color.green) );
        }
        else {
            email_status.setText("verify");
            email_status.setTextColor( getResources().getColor(R.color.red) );
        }


        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            TextView userPhone = findViewById(R.id.user_phone);

            if(providerId.equals("phone")){

                String phone = profile.getPhoneNumber();

                userPhone.setText(phone);
                phone_status.setText("verified");
                phone_status.setTextColor(getResources().getColor(R.color.green));
                break;
            }
            else {
                userPhone.setText("Phone Number");
                phone_status.setText("add");
                phone_status.setTextColor(getResources().getColor(R.color.red));
            }
            // UID specific to the provider
            String uid = profile.getUid();

            // Name, email address, and profile photo Url
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
        }
        // [END get_provider_data]
    }

    public void updatePassword() {
        // [START update_password]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = "Pravin@1234";

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("userdata", "User password updated.");
                        }
                    }
                });
        // [END update_password]
    }

    public void updateEmail() {
        // [START update_email]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail("twenty80partnershi@gmail.com")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("userdata", "User email address updated.");
                        }
                    }
                });
        // [END update_email]
    }




    public void reauthenticate() {
        // [START reauthenticate]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential("user@example.com", "password1234");

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("showdata", "User re-authenticated.");
                    }
                });
// [END reauthenticate]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1){
            if (resultCode==RESULT_OK){
                addressPresent = true;
                startActivity(new Intent(ProfileActivity.this,AddressesActivity.class));
            }

        }
    }
}
