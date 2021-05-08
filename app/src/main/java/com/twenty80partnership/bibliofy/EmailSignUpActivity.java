package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.models.Branch;
import com.twenty80partnership.bibliofy.models.Course;
import com.twenty80partnership.bibliofy.models.User;
import com.twenty80partnership.bibliofy.models.UserCourse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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
    private CardView imageView;
    ImageView imgMale,imgFemale,photo;
    private RadioButton maleSelect,femaleSelect;
    private RadioGroup rg1,rg2;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_up);

        //set toolbar as actionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        //imageView = findViewById(R.id.imageView);
        //imageView.bringToFront();

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
        maleSelect = findViewById(R.id.male_select);
        femaleSelect = findViewById(R.id.female_select);
        rg1 = findViewById(R.id.rg1);
        rg2 = findViewById(R.id.rg2);
        imgFemale = findViewById(R.id.img_female);
        imgMale = findViewById(R.id.img_male);
        photo = findViewById(R.id.edit_photo);


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


        if (getIntent().hasExtra("newEmail")){
            email.setText(getIntent().getStringExtra("newEmail"));
            password.setText(getIntent().getStringExtra("newPassword"));
        }

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

        setListeners();

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                courses.clear();
                years.clear();
                courses.add("SELECT COURSE");
                years.add("SELECT YEAR");

                Course currentCourse;

                for (DataSnapshot courseSnapshot:dataSnapshot.getChildren()){

                    //fetch all courses , add it to currentCourse which will at the end be added to courseList
                    // and added to courses which is attached to adapter
                    currentCourse = courseSnapshot.getValue(Course.class);

                    ArrayList<Branch> branchList = new ArrayList<>();

                    //if branches exists add it to branchlist and add to currentCourse
                    if (courseSnapshot.child("branch").exists()){

                        for (DataSnapshot branchSnapshot:courseSnapshot.child("branch").getChildren()){
                            branchList.add(branchSnapshot.getValue(Branch.class));
                        }

                        Collections.sort(branchList);
                        currentCourse.setBranchList(branchList);
                    }



                    courseList.add(currentCourse);

                }

                Collections.sort(courseList);

                for (Course c:courseList){
                    courses.add(c.getName());
                }
                //courses are applied
                courseAdapter.notifyDataSetChanged();
                yearAdapter.notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"courseRef: "+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setListeners() {
        //after selection of course set years and branch if exists
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

                //indicates selected course is found in courselist
                boolean found = false;
                //get selected course for searching of year and branch
                String courseSelected = courseSpinner.getSelectedItem().toString();

                //match selected course name to the course in courselist
                for (Course currentCourse:courseList){

                    if (courseSelected.equals(currentCourse.getName())){

                        found=true;
                        //selectedData can be further used to retain selected course
                        selectedData = currentCourse;
                        courseCode = currentCourse.getId();
                        courseName = currentCourse.getName();

                        //add number of years to the years array which is attached to the yearspinner
                        for (Integer i=1;i<=currentCourse.getYear();i++){
                            years.add(i.toString());
                        }

                        yearAdapter.notifyDataSetChanged();

                        //if branches exits the make branchSpinner visible,and add branches from branchlist which is present in currentCourse
                        //to the branches which is attached to spinner
                        if (currentCourse.getBranchList()!=null) {

                            branchSpinner.setVisibility(View.VISIBLE);
                            for (Branch currentBranch:currentCourse.getBranchList()){
                                branches.add(currentBranch.getName());
                            }

                            branchAdapter.notifyDataSetChanged();
                        }
                        //if no braches in selected course
                        else {
                            branchSpinner.setVisibility(View.INVISIBLE);
                        }

                        break;
                    }

                }

                //found will be false in case of "Select course" for that branch spinner should be invisible by default
                if (!found){
                    branchSpinner.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //this listener is to obtain branch code for selected branch and generate finalCode
        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String branchSelected = branchSpinner.getSelectedItem().toString();
                if (!branchSelected.equals("SELECT BRANCH")){

                    for(Branch currentBranch:selectedData.getBranchList()){

                        if (currentBranch.getName().equals(branchSelected)){
                            branchCode=currentBranch.getCode();
                            branchName = currentBranch.getName();
                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //this listener is to obtain year code for selected year and generate finalCode
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String yearSelected = yearSpinner.getSelectedItem().toString();
                if (!yearSelected.equals("SELECT YEAR")){
                    yearCode="0"+yearSelected;
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
        else if(!(maleSelect.isChecked()||femaleSelect.isChecked())){
            pd.dismiss();
            Toast.makeText(EmailSignUpActivity.this,"Please select your gender",Toast.LENGTH_SHORT).show();
        }
        else if ( sEmail.equals("") ){
            email.setError("Email can't be empty");
            pd.dismiss();
        }
        else if (branchSpinner.getVisibility()==View.VISIBLE && branchSpinner.getSelectedItem().toString().equals("SELECT BRANCH")){

            pd.dismiss();
            Toast.makeText(EmailSignUpActivity.this,"Select Branch",Toast.LENGTH_SHORT).show();

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

                        if (maleSelect.isChecked()){
                            gender = "male";
                        }
                        else {
                            gender= "female";
                        }


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

                                                        //adding uid to checkref
                                                        checkRef.child( mAuth.getCurrentUser().getUid() ).child("registerDate").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {


                                                                    UserCourse userCourse = new UserCourse(courseCode,courseName,branchCode,branchName,yearCode);


                                                                    //after additon of check ref create user obj and add to userRef
                                                                    User user = new User(sName,
                                                                            sEmail,
                                                                            null,
                                                                            null,
                                                                            0,
                                                                            date,
                                                                            mAuth.getCurrentUser().getUid(),
                                                                            sName.toLowerCase(),
                                                                            gender,
                                                                            userCourse);
                                                                    userRef.child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            //after adding values to checkRef and UserRef go to phone auth activity
                                                                           /// startActivity(new Intent(EmailSignUpActivity.this, CommonActivity.class));
                                                                            Toast.makeText(EmailSignUpActivity.this, "SignUp Successful", Toast.LENGTH_LONG).show();
                                                                           // Intent rIntent = new Intent();
                                                                           // setResult(RESULT_OK, rIntent);
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
//                                startActivity(new Intent(EmailSignUpActivity.this, CommonActivity.class));
//                                finish();
//                            }
//                        });

                    }
                }
            });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode==RESULT_OK){
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
