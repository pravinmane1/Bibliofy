package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class ProfileEditActivity extends AppCompatActivity {

    ImageView cancel,save,photo;
    EditText college,editName;
    TextView email,changePassword,course,changeCourse,addresses;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String oldName="";
    ProgressDialog pd;
    private TextView phoneStatus,userPhone;
    FirebaseUser firebaseUser;
    private ProgressDialog pdSave;
    String sCollege,sCourse;
    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());;

        oldName = mAuth.getCurrentUser().getDisplayName();

        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);
        phoneStatus = findViewById(R.id.phone_status);
        userPhone = findViewById(R.id.user_phone);
        editName = findViewById(R.id.edit_name);
        email = findViewById(R.id.user_email);
        photo = findViewById(R.id.edit_photo);
        college = findViewById(R.id.edit_college);
        course = findViewById(R.id.edit_course);
        TextView changeEmail = findViewById(R.id.change_email);
        changePassword = findViewById(R.id.change_password);
        changeCourse = findViewById(R.id.change_course);
        addresses = findViewById(R.id.addresses);

        pd = new ProgressDialog(ProfileEditActivity.this);
        pd.setMessage("Removing...");
        pd.setCancelable(false);


        pdSave = new ProgressDialog(ProfileEditActivity.this);
        pdSave.setMessage("Saving...");
        pdSave.setCancelable(false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
        builder.setMessage("Once phone number is removed , It can't be undone. You have to reverify to add the same or different number. ");
        builder.setCancelable(true);

        builder.setPositiveButton("remove",
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

        changeCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this,CourseActivity.class);
                intent.putExtra("loginFlow","no");
                startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pdSave.show();

                String nameS = editName.getText().toString();

                if ( !nameS.equals(firebaseUser.getDisplayName()) ) {

                    updateName(nameS);
                }

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


                if (!college.getText().toString().equals("")){

                        userRef.child("college").setValue(college.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    pdSave.dismiss();
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        phoneStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneStatus.getText().toString().equals("remove")){
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else if (phoneStatus.getText().toString().equals("add")){
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

        addresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileEditActivity.this,AddressesActivity.class));
            }
        });


        setUserData();  // photo name email college course address
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

                //null point exception todo
                if (!Objects.equals(profile.getDisplayName(), name)){

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

                            //    Toast.makeText(ProfileEditActivity.this, name, Toast.LENGTH_LONG).show();
                                Log.d("userstatus", "updated");


                            } else {
                                Toast.makeText(ProfileEditActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("userstatus", task.getException().getMessage());
                            }

                        }
                    });
                } else {
                    Log.d("userstatus", "oldname:" + profile.getDisplayName() + "not changing to: " + name);
                }

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

        Picasso.get()
                .load(firebaseUser.getPhotoUrl())
                .placeholder(R.drawable.userdisplay)
                .into(photo);

        editName.setText(firebaseUser.getDisplayName());
        email.setText(firebaseUser.getEmail());


        for (UserInfo profile:firebaseUser.getProviderData()){
            if (profile.getProviderId().equals("password")){
                changePassword.setVisibility(View.VISIBLE);
                break;
            }
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 sCollege = dataSnapshot.child("college").getValue(String.class);
                 sCourse = dataSnapshot.child("course").getValue(String.class);

                if (sCollege != null){
                    college.setText(sCollege);
                    college.setEnabled(true);
                }
                else{
                    college.setEnabled(true);
                }

                if (sCourse !=null){
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
                    addresses.setText("Add Address");
                    addresses.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_action_add,0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePhone() {
        Log.d("userdata","setPhoneProvider is called from requestcode 3");

        Boolean isPhone=false;

        for (UserInfo profile : firebaseUser.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();


            if(providerId.equals("phone")){
                isPhone=true;
                String phone = profile.getPhoneNumber();
                userPhone.setText(phone);
                phoneStatus.setText("remove");
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
            phoneStatus.setText("add");
            phoneStatus.setTextColor(getResources().getColor(R.color.red));
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
