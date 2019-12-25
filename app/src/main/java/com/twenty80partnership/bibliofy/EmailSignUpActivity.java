package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.models.Branch;
import com.twenty80partnership.bibliofy.models.Course;
import com.twenty80partnership.bibliofy.models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EmailSignUpActivity extends AppCompatActivity {


    EditText name,phone,email,password;
    Button button;
    private Spinner courseSpinner, yearSpinner,branchSpinner;
    ArrayAdapter<String>branchAdapter,courseAdapter,yearAdapter;
    ArrayList<String>branches;
    ArrayList<String>courses;
    ArrayList<String>years;
    ArrayList<Course>courseList;
    FirebaseAuth mAuth;
    ProgressDialog pd;
    DatabaseReference checkRef,userRef,coursesRef;
    String courseCode="",yearCode="",branchCode="",finalCode="";
    String courseName="",branchName="";
    private Course selectedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_up);

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(EmailSignUpActivity.this);
        pd.setMessage("loading...");
        pd.setCancelable(false);
        pd.show();

        mAuth = FirebaseAuth.getInstance();

        courseSpinner = findViewById(R.id.course_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        branchSpinner = findViewById(R.id.branch_spinner);

        name=(EditText)findViewById(R.id.name);
        phone=(EditText) findViewById(R.id.number);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        button=(Button) findViewById(R.id.btn_submit);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        coursesRef = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU");

        courses = new ArrayList<>();
        branches = new ArrayList<>();
        years = new ArrayList<>();
        courseList=new ArrayList<Course>();

        courseAdapter = new ArrayAdapter(this,R.layout.spinner_item2,courses);
        branchAdapter = new ArrayAdapter(this,R.layout.spinner_item2,branches);
        yearAdapter = new ArrayAdapter(this,R.layout.spinner_item2,years);

        courseSpinner.setAdapter(courseAdapter);
        branchSpinner.setAdapter(branchAdapter);
        yearSpinner.setAdapter(yearAdapter);

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                courses.clear();
                years.clear();
                courses.add("SELECT COURSE");
                years.add("SELECT YEAR");

                Course currentCourse;

                for (DataSnapshot courseSnapshot:dataSnapshot.getChildren()){

                    currentCourse = courseSnapshot.getValue(Course.class);
                    courses.add(currentCourse.getName());

                    ArrayList<Branch> branchList = new ArrayList<>();

                    if (courseSnapshot.child("branches").exists()){

                        for (DataSnapshot branchSnapshot:courseSnapshot.child("branches").getChildren()){
                            branchList.add(branchSnapshot.getValue(Branch.class));
                        }

                        currentCourse.setBranchList(branchList);
                    }



                    courseList.add(currentCourse);

                }
                courseAdapter.notifyDataSetChanged();
                yearAdapter.notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                branches.clear();
                years.clear();
                yearAdapter.clear();
                branchAdapter.clear();
                years.add("SELECT YEAR");
                branches.add("SELECT BRANCH");
                yearAdapter.notifyDataSetChanged();
                branchAdapter.notifyDataSetChanged();

                boolean found = false;
                String courseSelected = courseSpinner.getSelectedItem().toString();

                for (Course currentCourse:courseList){

                    if (courseSelected.equals(currentCourse.getName())){

                        found=true;
                        selectedData = currentCourse;
                        courseCode = currentCourse.getCode();
                        courseName = currentCourse.getName();

                        finalCode = courseCode+branchCode+yearCode;

                        for (Integer i=1;i<=currentCourse.getYears();i++){
                            years.add(i.toString());
                        }

                        yearAdapter.notifyDataSetChanged();

                        if (currentCourse.getBranchList()!=null) {

                            branchSpinner.setVisibility(View.VISIBLE);
                            for (Branch currentBranch:currentCourse.getBranchList()){
                                branches.add(currentBranch.getName());
                            }
                            //branches.addAll(currentCourse.getBranchList());
                            branchAdapter.notifyDataSetChanged();
                        }
                        else {
                            branchSpinner.setVisibility(View.INVISIBLE);
                        }

                        break;
                    }

                }

                if (!found){
                    branchSpinner.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String branchSelected = branchSpinner.getSelectedItem().toString();
                if (!branchSelected.equals("SELECT BRANCH")){

                    for(Branch currentBranch:selectedData.getBranchList()){

                        if (currentBranch.getName().equals(branchSelected)){
                            branchCode=currentBranch.getCode();
                            branchName = currentBranch.getName();

                            finalCode=courseCode+branchCode+yearCode;
                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String yearSelected = yearSpinner.getSelectedItem().toString();
                if (!yearSelected.equals("SELECT YEAR")){
                    yearCode=yearSelected;
                    finalCode=courseCode+branchCode+yearCode;

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    void createAccount(){
        final String sEmail= email.getText().toString();
        final String sPass = password.getText().toString();
        final String sName=  name.getText().toString();

        pd.show();

        if (branchSpinner.getVisibility()==View.VISIBLE && branchSpinner.getSelectedItem().toString().equals("SELECT BRANCH")){

                pd.dismiss();
                Toast.makeText(EmailSignUpActivity.this,"Select Branch",Toast.LENGTH_SHORT).show();

        }

        else if ( sName.equals("") || !sName.matches(".*[a-zA-Z]+.*")){
            name.setError("Name can't be empty");
            pd.dismiss();
        }
        else if ( sEmail.equals("") ){
            email.setError("Email can't be empty");
            pd.dismiss();
        }
        else if (courseSpinner.getSelectedItem().toString().equals("SELECT COURSE")){
            pd.dismiss();
            Toast.makeText(EmailSignUpActivity.this,"Select Course",Toast.LENGTH_SHORT).show();
        }
        else if (yearSpinner.getSelectedItem().toString().equals("SELECT YEAR")){
            pd.dismiss();
            Toast.makeText(EmailSignUpActivity.this,"Select Year",Toast.LENGTH_SHORT).show();
        }

        else if ( sPass.equals("") ){
            password.setError("password can't be empty");
            pd.dismiss();
        }

        else {
            mAuth.createUserWithEmailAndPassword(sEmail, sPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if (!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            password.setError("weak password, minimum length: 6");
                            password.requestFocus();
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            email.setError("Invalid email");
                            email.requestFocus();
                        } catch(FirebaseAuthUserCollisionException e) {
                            email.setError("User Already Exists");
                            email.requestFocus();
                        } catch(Exception e) {
                            Log.e("showdata", e.getMessage());
                        }
                        pd.dismiss();
                    }
                    //task is successful
                    else {


                        //taking system time
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        Date currentTime= Calendar.getInstance().getTime();
                        final Long date=Long.parseLong(dateFormat.format(currentTime));



                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name.getText().toString().trim())
                                .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            checkRef = FirebaseDatabase.getInstance().getReference("CheckId");

                                            checkRef.addListenerForSingleValueEvent(new ValueEventListener() {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    // and call phone auth verification



                                                        //adding uid to checkref
                                                        checkRef.child( mAuth.getCurrentUser().getUid() ).child("registerDate").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    //after additon of check ref create user obj and add to userRef
                                                                    User user = new User(sName,
                                                                            sEmail,
                                                                            null,
                                                                            null,
                                                                            courseCode,
                                                                            branchCode,
                                                                            yearCode,
                                                                            courseCode+branchCode+yearCode,
                                                                            0,
                                                                            branchName+" "+courseName,date,mAuth.getCurrentUser().getUid(),
                                                                            sName.toLowerCase());
                                                                    userRef.child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            //after adding values to checkRef and UserRef go to phone auth activity
                                                                            startActivity(new Intent(EmailSignUpActivity.this, PhoneNumberActivity.class));
                                                                            Toast.makeText(EmailSignUpActivity.this, "SignUp Successful", Toast.LENGTH_LONG).show();
                                                                            Intent rIntent = new Intent();
                                                                            setResult(RESULT_OK, rIntent);
                                                                            pd.dismiss();
                                                                            finish();
                                                                        }
                                                                    });
                                                                }
                                                                else {
                                                                    Toast.makeText(EmailSignUpActivity.this,"checkref setting data failed",Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    if (pd.isShowing()){
                                                        pd.dismiss();
                                                    }
                                                    Toast.makeText(EmailSignUpActivity.this,databaseError.toException().toString(),Toast.LENGTH_LONG).show();

                                                }

                                            });
                                            //called after the sign in task is successful to check if existing user or new by Checkref

//                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
//                                            userRef.setValue(user)
//                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//
//
//
//                                                        }
//                                                    });
                                            Toast.makeText(EmailSignUpActivity.this, "User profile updated.",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(EmailSignUpActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

//                        mAuth.signInWithEmailAndPassword(sEmail,sPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                pd.dismiss();
//                                startActivity(new Intent(EmailSignUpActivity.this, DashboardActivity.class));
//                                finish();
//                            }
//                        });

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
}
