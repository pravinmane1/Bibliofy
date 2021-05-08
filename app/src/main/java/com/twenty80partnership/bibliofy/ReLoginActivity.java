package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.models.Banner;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.User;
import com.google.firebase.perf.FirebasePerformance;

import com.google.firebase.perf.metrics.Trace;
import com.twenty80partnership.bibliofy.sql.DatabaseHelper;
import com.twenty80partnership.bibliofy.sql.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReLoginActivity extends AppCompatActivity {

    private ProgressDialog pd;

    private FirebaseAuth mAuth;

    private DatabaseReference userRef,checkRef;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private ValueEventListener setUserData;
    Trace myTrace;

    private String personName="",personEmail="",photo="";
    private Date currentTime;
    private SignInButton google;
    private CardView email;
    private FirebaseDatabase db;
    private DatabaseHelper databaseHelper;
    private AuthCredential credential;
    private DatabaseReference cartRequestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_re_login);

        setViews();

        db = FirebaseDatabase.getInstance();

        databaseHelper = new DatabaseHelper(this);
        Cursor c = databaseHelper.deleteAllData3();

        //progrress dialogue for loading
        setProgressDialog();

        setGoogleAuth();

        setClickListeners();

        checkRef = db.getReference("CheckId");
        userRef = db.getReference("Users");



        //called after the sign in task is successful to check if existing user or new by Checkref
        setUserData= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    currentTime=Calendar.getInstance().getTime();
                    final Long date=Long.parseLong(dateFormat.format(currentTime));

                    DatabaseReference loginInfoRef = FirebaseDatabase.getInstance().getReference("LoginInfo").child(mAuth.getCurrentUser().getUid())
                            .push().child("time");
                    loginInfoRef.setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
//                                //remove to prevent listening same calls again
//                                checkRef.removeEventListener(setUserData);
                                //after addition of check ref create user obj and add to userRef
                                String uId = mAuth.getCurrentUser().getUid();

                                User user = new User(personName, personEmail,photo,0,date,uId,personName.toLowerCase());


                                userRef.child( uId ).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                          //  pd.dismiss();
                                            //after adding values to checkRef and UserRef go to phone auth activity
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(personName)
                                                    .build();

                                            mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    updateUI();
                                                }
                                            });

                                        }
                                        else {
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(),"name email pic task: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                            else{
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(),"registerdate task: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (pd.isShowing()){
                    pd.dismiss();
                }
                Toast.makeText(ReLoginActivity.this,"checkref: "+databaseError.toException().toString(),Toast.LENGTH_LONG).show();

            }
        };
    }

    private void setClickListeners() {
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReLoginActivity.this,EmailLoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

    }

    private void setViews() {
        email = findViewById(R.id.email);
        google = findViewById(R.id.google);

    }

    private void setGoogleAuth() {
        //google sign in options for signInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //google sign in client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        //account holds the google account, this value can be used if user is previously signed in
        account = GoogleSignIn.getLastSignedInAccount(this);

    }

    private void setProgressDialog() {
        pd = new ProgressDialog(ReLoginActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);
    }


    void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
        pd.show();

    }


    //when user selects his google account
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        pd.dismiss();
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 9001) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("abc", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        personName = acct.getDisplayName();
        //  String personGivenName = acct.getGivenName();
        //   String personFamilyName = acct.getFamilyName();
        personEmail = acct.getEmail();
        Uri photoUri = acct.getPhotoUrl();

        photo = photoUri.toString();

        Log.d("abc", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        pd.show();

        //generate credentials form the Google sign in account for firebase sign in with credentials
        credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //firebase sign in
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("updateui","auth complete listener is being called");

                        if (!task.isSuccessful()) {
                            try{
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                pd.dismiss();
                                mGoogleSignInClient.signOut();
                                Toast.makeText(ReLoginActivity.this, task.getException().getMessage()+" Bibliofy Authentication Failed.", Toast.LENGTH_SHORT).show();

                            } catch (FirebaseAuthUserCollisionException e){
                                pd.setMessage("Existing user...");
                                existingUserSignIn();
                            } catch (Exception e) {
                                Toast.makeText(ReLoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }


                        }
                        else{
                            Log.d("updateui","Authentication task done with success");
                            checkRef.addListenerForSingleValueEvent(setUserData);
                        }

                    }
                });
    }

    private void existingUserSignIn() {
        ///store cartdata to sqlite and delete anonymoususer sign in with credentials and transfer data from sqlite
        pd.show();
        saveCart();
    }

    private void saveCart() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(mAuth.getCurrentUser().getUid());



         //access cart data from anonymous user account to add it to sqlite database
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                pd.setMessage("Updating Local database...");

                if (dataSnapshot.exists()){
                    for (DataSnapshot i:dataSnapshot.getChildren()){
                        String type = i.getKey();
                        for (DataSnapshot j:i.getChildren()){
                            String id = j.child("itemId").getValue(String.class);
                            String location = j.child("itemLocation").getValue(String.class);
                            Integer quantity = j.child("quantity").getValue(Integer.class);
                            String  timeAdded= j.child("timeAdded").getValue(Long.class).toString();

                            boolean success = databaseHelper.insertData3(id,location,type,quantity,timeAdded);

                            if (!success){
                                Toast.makeText(ReLoginActivity.this,id+" failed",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(ReLoginActivity.this,id+" success",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    removeAndAdd();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeAndAdd() {


        final Cursor cursor = databaseHelper.getAllData();

        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getCurrentUser().getUid());

        //remove cart data from anonymous user
        while (cursor.moveToNext()){
            cartRequestRef.child(cursor.getString(2))
                    .child(cursor.getString(0)).removeValue();

            Log.d("Reloginme","item "+cursor.getString(0)+" is removed from old auth");

        }

        //delete anonymous user profile and account
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mAuth.getCurrentUser().delete();
                mAuth.signOut();
            }
        });


        //sign in with old user
        pd.setMessage("Signing In");
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getCurrentUser().getUid());

                    cartRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //if old user cart has anything then remove it first.
                            if (dataSnapshot.exists()){

                                cartRequestRef.removeValue();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
//                                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
//
//                                    String type = dataSnapshot1.getKey();
//
//                                    for (DataSnapshot dataSnapshot2:dataSnapshot1.getChildren()){
//
//                                        CartItem item = dataSnapshot2.getValue(CartItem.class);
//
//                                        cartRequestRef.child(type).child(item.getItemId()).removeValue();
//                                    }
//                                }
                            }


                            //after removig old cart items add items form sqlite database to existing user's cart
                            final Cursor cursor1 = databaseHelper.getAllData();

                            while (cursor1.moveToNext()){

                                CartItem cartItem = new CartItem();
                                cartItem.setItemId(cursor1.getString(0));
                                cartItem.setItemLocation(cursor1.getString(1));
                                cartItem.setQuantity(Integer.parseInt(cursor1.getString(3)));
                                cartItem.setTimeAdded(Long.parseLong(cursor1.getString(4)));


                                cartRequestRef.child(cursor1.getString(2)).child(cursor1.getString(0))
                                        .setValue(cartItem);
                                int result = databaseHelper.deleteData3(cursor1.getString(0));
                                        //.addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        //after adding sqlite data to online database delete it from offline database.
//
//                                    }
//                                });

                                Log.d("Reloginme","item "+cursor1.getString(0)+" is added to new auth");
                            }

                            //now cart items are added to old user's cart update ui
                            updateUI();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(ReLoginActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                //sign in failed after deleting anonymous
                else {

                    mAuth.signInAnonymously();
                }
            }
        });

    }


    //for existing users
    void updateUI(){
//
       int count = 0;

        FirebaseUser user = mAuth.getCurrentUser();


        for(UserInfo profile:user.getProviderData()){
            count++;
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();
            // UID specific to the provider
            String uid = profile.getUid();

            // Name, email address, and profile photo Url
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
        }

        if (count!=1){
            if (pd.isShowing())
                pd.dismiss();

            setResult(RESULT_OK);
            finish();
        }

    }

}

