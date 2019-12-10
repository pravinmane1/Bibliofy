package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.modules.Branch;
import com.twenty80partnership.bibliofy.modules.Course;
import com.twenty80partnership.bibliofy.modules.User;

import java.util.ArrayList;

public class CourseActivity extends AppCompatActivity {
    private Spinner courseSpinner, yearSpinner,branchSpinner;
    ArrayAdapter<String> branchAdapter,courseAdapter,yearAdapter;
    ArrayList<String> branches;
    ArrayList<String>courses;
    ArrayList<String>years;
    ArrayList<Course>courseList;
    FirebaseAuth mAuth;
    ProgressDialog pd;
    DatabaseReference coursesRef,userRef;
    String courseCode="",yearCode="",branchCode="",finalCode="";
    String courseName="",branchName="";
    private Course selectedData;
    Button select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        //set toolbar as actionBar
        setToolBar();

        setProgressBar();

        setViews();

        mAuth = FirebaseAuth.getInstance();



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
                    courses.add(currentCourse.getName());

                    ArrayList<Branch> branchList = new ArrayList<>();

                    //if branches exists add it to branchlist and add to currentCourse
                    if (courseSnapshot.child("branches").exists()){

                        for (DataSnapshot branchSnapshot:courseSnapshot.child("branches").getChildren()){
                            branchList.add(branchSnapshot.getValue(Branch.class));
                        }

                        currentCourse.setBranchList(branchList);
                    }



                    courseList.add(currentCourse);

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
                        courseCode = currentCourse.getCode();
                        courseName = currentCourse.getName();

                        finalCode = courseCode+branchCode+yearCode;

                        //add number of years to the years array which is attached to the yearspinner
                        for (Integer i=1;i<=currentCourse.getYears();i++){
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

                            finalCode=courseCode+branchCode+yearCode;

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
                    yearCode=yearSelected;
                    finalCode=courseCode+branchCode+yearCode;

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();

                if (branchSpinner.getVisibility()==View.VISIBLE && branchSpinner.getSelectedItem().toString().equals("SELECT BRANCH")){

                    pd.dismiss();
                    Toast.makeText(CourseActivity.this,"Select Branch",Toast.LENGTH_SHORT).show();

                }

                else if (courseSpinner.getSelectedItem().toString().equals("SELECT COURSE")){
                    pd.dismiss();
                    Toast.makeText(CourseActivity.this,"Select Course",Toast.LENGTH_SHORT).show();
                }
                else if (yearSpinner.getSelectedItem().toString().equals("SELECT YEAR")){
                    pd.dismiss();
                    Toast.makeText(CourseActivity.this,"Select Year",Toast.LENGTH_SHORT).show();
                }
                else {

                    userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            user.setCourseCode(courseCode);
                            if (branchSpinner.getVisibility()==View.VISIBLE){
                                user.setBranchCode(branchCode);
                            }
                            else {
                                user.setBranchCode(null);
                            }

                            user.setYearCode(yearCode);
                            user.setCourse(branchName+" "+courseName);

                            userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        pd.dismiss();
                                        Toast.makeText(getApplicationContext(),"User Course Updated",Toast.LENGTH_SHORT).show();

                                        Intent courseIntent = getIntent();

                                            if (courseIntent.getStringExtra("loginFlow").equals("yes")){

                                                Intent phoneNumberIntent = new Intent(CourseActivity.this,PhoneNumberActivity.class);
                                                phoneNumberIntent.putExtra("loginFlow","yes");
                                                startActivity(phoneNumberIntent);
                                            }

                                        else {
                                            finish();
                                        }


                                    }
                                    else {
                                        pd.dismiss();
                                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(),"userref: "+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    private void setViews() {
        courseSpinner = findViewById(R.id.course_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        branchSpinner = findViewById(R.id.branch_spinner);
        select = findViewById(R.id.select);
    }

    private void setProgressBar() {
        pd = new ProgressDialog(CourseActivity.this);
        pd.setMessage("loading...");
        pd.setCancelable(false);
        pd.show();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(getIntent().getStringExtra("loginFlow").equals("no")){
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent courseIntent = getIntent();

            if (courseIntent.getStringExtra("loginFlow").equals("yes")){

                startActivity(new Intent(CourseActivity.this,DashboardActivity.class));
                finish();
            }
        else {
            finish();
        }

    }
}
