package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.twenty80partnership.bibliofy.models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    Trace myTrace;
    private ProgressDialog pd;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, checkRef;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private ValueEventListener setUserData;
    private String personName = "", personEmail = "", photo = "";
    private Date currentTime;
    private RelativeLayout google;
    private EditText email, password;
    private Button login;
    private TextView signUp;
    private FirebaseDatabase db;
    private LottieAnimationView lottieAnimationView;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setViews();

        db = FirebaseDatabase.getInstance();

        //login trace
        myTrace = FirebasePerformance.getInstance().newTrace("login_trace");
        myTrace.start();

        //progrress dialogue for loading
        setProgressDialog();

        setGoogleAuth();

        setClickListeners();

        checkRef = db.getReference("CheckId");
        userRef = db.getReference("Users");


        //called after the sign in task is successful to check if existing user or new by Checkref
        setUserData = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                final String uId = mAuth.getCurrentUser().getUid();

                //If checkref doesn't contain the user id the create the new entry in checkref and in users
                // and call phone auth verification
                if (!dataSnapshot.child(uId).exists()) {

                    Log.d("updateui", "database entry doesn't exits so creating and verifying");

                    //taking system time
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    currentTime = Calendar.getInstance().getTime();
                    final Long date = Long.parseLong(dateFormat.format(currentTime));

                    //adding uid to checkID
                    checkRef.child(uId).child("registerDate").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {


                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
//                                //remove to prevent listening same calls again
//                                checkRef.removeEventListener(setUserData);
                                //after addition of check ref create user obj and add to userRef
                                User user = new User(personName, personEmail, photo, 0, date, uId, personName.toLowerCase());

                                userRef.child(uId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            pd.dismiss();
                                            //after adding values to checkRef and UserRef go to phone auth activity


                                            Intent courseIntent = new Intent(LoginActivity.this, CourseActivity.class);
                                            courseIntent.putExtra("loginFlow", "yes");
                                            startActivity(courseIntent);
                                            finish();


                                        } else {
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), "name email pic task: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), "registerdate task: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                    //entry exists for the user in CheckRef, so simply call updateUI to board onto dashboard
                } else {

                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    currentTime = Calendar.getInstance().getTime();
                    Long date = Long.parseLong(dateFormat.format(currentTime));

                    DatabaseReference loginInfoRef = FirebaseDatabase.getInstance().getReference("LoginInfo").child(mAuth.getCurrentUser().getUid())
                            .push().child("time");
                    loginInfoRef.setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("updateui", "existing user calling updateui for transition");
                                updateUI();
                            } else {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), "logininfo error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Toast.makeText(LoginActivity.this, "checkref: " + databaseError.toException().toString(), Toast.LENGTH_LONG).show();

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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, EmailSignUpActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PasswordActivity.class));
            }
        });
    }

    private void startLogin() {
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);

        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        if (email.getText().toString().equals("")) {
            email.setError("Email can't be empty");
            pd.dismiss();
        } else if (password.getText().toString().equals("")) {
            password.setError("Password Can't be empty");
            pd.dismiss();
        } else {

            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        //taking system time
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        Date currentTime = Calendar.getInstance().getTime();
                        Long date = Long.parseLong(dateFormat.format(currentTime));

                        DatabaseReference loginInfoRef = FirebaseDatabase.getInstance().getReference("LoginInfo").child(mAuth.getCurrentUser().getUid())
                                .push().child("time");
                        loginInfoRef.setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    pd.dismiss();
                                    updateUI();
                                } else {
                                    pd.dismiss();
                                    updateUI();
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    } else {
                        pd.dismiss();

                        Log.d("loginexcep", task.getException().toString());
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            //wrong userid / new user
                            Intent newUserIntent = new Intent(LoginActivity.this, EmailSignUpActivity.class);
                            newUserIntent.putExtra("newEmail", email.getText().toString());
                            newUserIntent.putExtra("newPassword", password.getText().toString());
                            startActivity(newUserIntent);
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            //invalid password or no password
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            //new error
                        }

                    }
                }
            });
        }
    }

    private void setViews() {
        signUp = findViewById(R.id.sign_up);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        google = findViewById(R.id.google_layout);
        lottieAnimationView = findViewById(R.id.delivery);
        lottieAnimationView.bringToFront();
        // lottieAnimationView.setAnimation("data.json");
        forgotPassword = findViewById(R.id.forgot_password);
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
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("updateui", "onstart calling updateui for transition");

        updateUI();
    }


    void signIn() {
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
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //firebase sign in
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("updateui", "auth complete listener is being called");

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Bibliofy Authentication Failed.", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            mGoogleSignInClient.signOut();
                        } else {
                            Log.d("updateui", "Authentication task done with success");
                            checkRef.addListenerForSingleValueEvent(setUserData);
                        }

                    }
                });
    }


    void anonymousSignIn() {
        pd.show();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            checkRef.addListenerForSingleValueEvent(setUserData);
                        } else {
                            // If sign in fails, display a message to the user.
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "Sorry!!! Cant skip this step right now.", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    //for existing users
    void updateUI() {

        //if firebase auth is not null goto dashboard
        if (mAuth.getCurrentUser() != null) {

            Log.d("updateui", "mauth exists updateUI is getting called");

            if (pd.isShowing())
                pd.dismiss();
            startActivity(new Intent(LoginActivity.this, CommonActivity.class));
            finish();

        } else {
            Log.d("updateui", "mauth is null updateUI is getting called");
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    Log.d("focus", "touchevent");
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
