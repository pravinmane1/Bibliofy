package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.adapters.MySpinnerAdapter;
import com.twenty80partnership.bibliofy.holders.BookViewHolder;
import com.twenty80partnership.bibliofy.models.Book;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.ApplicableTerm;
import com.twenty80partnership.bibliofy.models.Item;
import com.twenty80partnership.bibliofy.models.UserCourse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AvlCategoryActivity extends AppCompatActivity {

    private String searchCode = " ",oneCode = "",twoCode = "",threeCode = "",fourCode = "";

    private ShimmerFrameLayout mShimmerViewContainer;
    private Spinner spinner1, spinner2, spinner3, spinner4;
    private TextView search;
    private ImageView initial;
    private TextView showCartCount;
    private RelativeLayout bottom;
    private RecyclerView bookList;

    private ValueEventListener bottomUpdate;
    private DatabaseReference codeRef, booksRef, cartRequestRef, currentSemRef,termsApplicableRef,SPPUtermsListingRef;

    private ArrayList <Item> OneList;
    private ArrayList<Item> TwoList;
    private ArrayList<Item> ThreeList;
    private ArrayList<Item> FourList;

    private ArrayList<ApplicableTerm> applicableTermList;
    private ProgressDialog pd;


    DataSnapshot oneData;
    DataSnapshot twoData;// = dataSnapshot.child("EnggYear");
    DataSnapshot threeData;// = dataSnapshot.child("EnggSem");
    DataSnapshot fourData ;

    private MySpinnerAdapter oneAdapter,twoAdapter,threeAdapter,fourAdapter ;
    private ArrayList<String> first, second, third, fourth;
    private Date currentTime;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private boolean directCall=false;
    private String branchName;
    String bookType="",course="";

    private LottieAnimationView noItems;
    private Boolean post =false;
    private String courseCode="",branchCode="",yearCode="",semCode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avl_category);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        setViews();

        //set toolbar as actionbar and setting title according to bookType
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        //these will store direct data from database as a object containing codes to search
        OneList = new ArrayList<Item>();
        TwoList = new ArrayList<Item>();
        ThreeList = new ArrayList<Item>();
        FourList = new ArrayList<Item>();

        //these will be used to show the data in spinners which is obtained by above lists
        first = new ArrayList<String>();
        second = new ArrayList<String>();
        third = new ArrayList<String>();
        fourth = new ArrayList<String>();

        pd = new ProgressDialog(this);
        pd.setMessage("Getting your books...");
        pd.setCancelable(false);


        //itent from regular online and theory
        Intent intent = getIntent();
        //Toast.makeText(AvlCategoryActivity.this,intent.getStringExtra("bookType"),Toast.LENGTH_SHORT).show();

        if (intent.hasExtra("course")) {
            course = intent.getStringExtra("course");
            bookType = intent.getStringExtra("bookType");
            semCode = intent.getStringExtra("sem");

            Toast.makeText(AvlCategoryActivity.this,bookType,Toast.LENGTH_SHORT).show();
        }
        else if(intent.hasExtra("userCourse")){

            bookType = intent.getStringExtra("bookType");


            pd.show();

            DatabaseReference userCourseRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid())
                    .child("userCourse");

            userCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final UserCourse userCourse = dataSnapshot.getValue(UserCourse.class);


                    if(userCourse!=null && userCourse.getCourseCode()!=null){
                        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Courses")
                                .child("SPPU").child(userCourse.getCourseCode());

                        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null){


                                    branchCode = userCourse.getBranchCode();
                                    yearCode = userCourse.getYearCode();

                                    DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Courses")
                                            .child("SPPU").child(userCourse.getCourseCode());

                                    bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                            courseCode = dataSnapshot.child("bookCourseReference").getValue(String.class);
                                            course = courseCode;

                                            semCode = dataSnapshot.child("currentSem").getValue(String.class);


                                            Log.d("datafetched",course+branchCode+yearCode+semCode);
                                            SPPUtermsListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing").child(course);

                                            booksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(bookType);

                                            //applicable terms and according codes are fetched and set to spinners
                                            setApplicableTermsAndCodes();


                                            //dependency on setApplicable terms and codes as it compares them.
                                            setSpinnersSelectedListener();



                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            pd.dismiss();
                                        }
                                    });

                                }
                                else{
                                    //go to select user course.
                                    pd.dismiss();
                                    Toast.makeText(AvlCategoryActivity.this, "Please choose your course", Toast.LENGTH_SHORT).show();

                                    Intent courseIntent = new Intent(AvlCategoryActivity.this, CourseActivity.class);
                                    courseIntent.putExtra("loginFlow", "no");
                                    startActivityForResult(courseIntent,111);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else{
                        //go to select user course.
                        pd.dismiss();
                        Toast.makeText(AvlCategoryActivity.this, "Please choose your course", Toast.LENGTH_SHORT).show();

                        Intent courseIntent = new Intent(AvlCategoryActivity.this, CourseActivity.class);
                        courseIntent.putExtra("loginFlow", "no");
                        startActivityForResult(courseIntent,111);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else  {
            finish();
            Toast.makeText(AvlCategoryActivity.this,"course and booktype not defined",Toast.LENGTH_SHORT).show();
        }



        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getCurrentUser().getUid());

        //to update the bottom
       setBottomUpdate();

       //adpters to spinner added providing first second third fourth array
      // setAdaptersToSpinners();


        if (!getIntent().hasExtra("userCourse")) {
            SPPUtermsListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing").child(course);

            booksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(bookType);
            termsApplicableRef = FirebaseDatabase.getInstance()
                    .getReference("SPPUbooksListing").child("category").child(course).child(bookType).child("applicableTerms");


            //applicable terms and according codes are fetched and set to spinners
            setApplicableTermsAndCodes();


            //dependency on setApplicable terms and codes as it compares them.
            setSpinnersSelectedListener();
        }



        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AvlCategoryActivity.this, CartActivity.class));
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(AvlCategoryActivity.this,"search= "+searchCode,Toast.LENGTH_SHORT).show();


                Log.d("indexsearch","search code search.setonclicklistener "+searchCode);

                //show shimmer
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();
                bookList.setVisibility(View.GONE);

                //query for search and ui config
                Query q = booksRef.orderByChild("code").startAt(searchCode).endAt(searchCode + "\uf8ff");

                //directcall is affecting regular calls, it should be handled properly
                //directCall = false;

                //listener for setting visibility of bookList and Shimmerview
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            Log.d("animationde","child found");
                            bookList.setVisibility(View.VISIBLE);
                            noItems.setVisibility(View.GONE);
                        }
                        else {
                            Log.d("animationde","animation for no child");
                            noItems.setVisibility(View.VISIBLE);
                            noItems.setProgress(0);
                            noItems.playAnimation();
                        }

                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.startShimmerAnimation();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AvlCategoryActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });

                //search for showing data through adapter
                firebaseSearch(q);

            }
        });
    }

    private void setViews() {
        spinner1 = findViewById(R.id.branch_spinner);
        spinner2 = findViewById(R.id.year_spinner);
        spinner3 = findViewById(R.id.sem_spinner);
        spinner4 = findViewById(R.id.publication_spinner);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        noItems = findViewById(R.id.no_items);
        bottom = findViewById(R.id.bottom_layout);
        showCartCount = findViewById(R.id.show_cart_count);
        bookList = findViewById(R.id.recycler_view);
        search = findViewById(R.id.search);
    }



    private void setBottomUpdate() {

        bottomUpdate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long count=0L;

                if (dataSnapshot.exists()){

                    count = dataSnapshot.getChildrenCount();

                }

                if (dataSnapshot.child("stationary").exists()){

                    count = count + dataSnapshot.child("stationary").getChildrenCount();
                }

                if (count == 1) {
                    showCartCount.setText(count + " item in Cart");
                }
                else if(count>1){
                    showCartCount.setText(count + " items in Cart");
                }

                if (count>0)
                    bottom.setVisibility(View.VISIBLE);
                else
                    bottom.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AvlCategoryActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        };

        cartRequestRef.addValueEventListener(bottomUpdate);
    }

    private void setSpinnersSelectedListener() {

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //obtain the  selected branch from spinner
                String branchSelected = spinner1.getSelectedItem().toString();

                boolean codeFoundForSelectedString = false;

                //loop applied on arrayList to find the code for selected branch from objects arrayList
                for (int i = 0; i < OneList.size(); i++) {

                    if (OneList.get(i).getName().equals(branchSelected)) {

                        oneCode = OneList.get(i).getCode();
                        codeFoundForSelectedString = true;
                        break;
                    }

                }

                if (!codeFoundForSelectedString && !getIntent().hasExtra("userCourse")) {
                    oneCode = " ";
                }


                if (twoCode==null)
                    Log.d("twocodedata","twocode is null");
                else {
                    Log.d("twocodedata","twocode is not null"+twoCode);
                }

                if (twoCode.equals("01")
                        && course.equals("Engineering")) {
                    oneCode = "XX";
                }

                searchCode = oneCode + twoCode + threeCode + fourCode;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String yearSelected = spinner2.getSelectedItem().toString();
                boolean codeFoundForSelectedString = false;

                for (int i = 0; i < TwoList.size(); i++) {
                    if (TwoList.get(i).getName().equals(yearSelected)) {
                        twoCode = TwoList.get(i).getCode();
                        codeFoundForSelectedString = true;
                        break;
                    }
                }

                if (!codeFoundForSelectedString && !getIntent().hasExtra("userCourse")) {
                    twoCode = " ";
                }

                if (twoCode.equals("01") && course.equals("Engineering")) {
                    oneCode = "XX";
                }
                else{
                    for (int i = 0; i < OneList.size(); i++) {

                        String branchSelected = spinner1.getSelectedItem().toString();

                        if (OneList.get(i).getName().equals(branchSelected)) {

                            oneCode = OneList.get(i).getCode();
                            break;
                        }

                    }
                }


                searchCode = oneCode + twoCode + threeCode + fourCode;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String semSelected = spinner3.getSelectedItem().toString();

                if (semSelected.equals("SEMESTER") && !directCall){
                    threeCode = "";
                    searchCode = oneCode + twoCode + threeCode + fourCode;
                    return;
                }

                for (int i = 0; i < ThreeList.size(); i++) {
                    if (ThreeList.get(i).getName().equals(semSelected)) {
                        threeCode = ThreeList.get(i).getCode();
                        break;
                    }
                }


                searchCode = oneCode + twoCode + threeCode + fourCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String publicationSelected = spinner4.getSelectedItem().toString();

                if (publicationSelected.equals("PUBLICATION")){
                    fourCode = "";
                    searchCode = oneCode + twoCode + threeCode + fourCode;
                    return;
                }

                for (int i = 0; i < FourList.size(); i++) {
                    if (FourList.get(i).getName().equals(publicationSelected)) {
                        fourCode = FourList.get(i).getCode();
                        break;
                    }
                }

                searchCode = oneCode + twoCode + threeCode + fourCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(AvlCategoryActivity.this,"nothing selected",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setApplicableTermsAndCodes() {



        termsApplicableRef = FirebaseDatabase.getInstance()
                .getReference("SPPUbooksListing").child("category").child(course).child(bookType).child("applicableTerms");

        termsApplicableRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.getChildrenCount()==3){
                    spinner4.setVisibility(View.INVISIBLE);
                }
                else if (dataSnapshot.getChildrenCount()==2){
                    spinner3.setVisibility(View.INVISIBLE);
                    spinner4.setVisibility(View.INVISIBLE);

                }
                else if (dataSnapshot.getChildrenCount()==1){
                    spinner2.setVisibility(View.INVISIBLE);
                    spinner3.setVisibility(View.INVISIBLE);
                    spinner4.setVisibility(View.INVISIBLE);
                }

                //ensuring that no data present in arraylists
                OneList.clear();
                TwoList.clear();
                ThreeList.clear();
                FourList.clear();

                first.clear();
                second.clear();
                third.clear();
                fourth.clear();

                applicableTermList = new ArrayList<ApplicableTerm>();

                if (dataSnapshot.getChildrenCount()==0){
                    Toast.makeText(AvlCategoryActivity.this,"No Search Data Found for this Course",Toast.LENGTH_SHORT).show();
                }
                else {

                    int i = 1;

                    Log.d("valuesdebug","line 589");


                    for (DataSnapshot currentApplicable:dataSnapshot.getChildren()){



                        ApplicableTerm codesApplicable = currentApplicable.getValue(ApplicableTerm.class);
                        applicableTermList.add(codesApplicable);

                        switch (i){
                            case 1:
                               switch (codesApplicable.getTermId()){
                                   case "branch":
                                       oneCode = branchCode;
                                       break;
                                   case "year":
                                       oneCode = yearCode;
                                       break;
                                   case "sem":
                                       oneCode = semCode;
                                      break;
                               }

                               break;

                            case 2:
                                switch (codesApplicable.getTermId()){
                                    case "branch":
                                        twoCode = branchCode;
                                        break;
                                    case "year":
                                        twoCode = yearCode;
                                        break;
                                    case "sem":
                                        twoCode = semCode;
                                        break;
                                }

                                break;

                            case 3:
                                switch (codesApplicable.getTermId()){
                                    case "branch":
                                        threeCode = branchCode;
                                        break;
                                    case "year":
                                        threeCode = yearCode;
                                        break;
                                    case "sem":
                                        threeCode = semCode;
                                        break;
                                }
                                break;

                            case 4:
                                switch (codesApplicable.getTermId()){
                                    case "branch":
                                        fourCode = branchCode;
                                        break;
                                    case "year":
                                        fourCode = yearCode;
                                        break;
                                    case "sem":
                                        fourCode = semCode;
                                        break;
                                }
                                break;

                        }

                        i++;
                    }



                }

                SPPUtermsListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String termId="";

                        Log.d("valuesdebug","line 672 termslisting "+dataSnapshot.getValue());

                        //for setting topics of term from termsListingRef
                        for (int i=0;i<applicableTermList.size();i++){
                            termId  = applicableTermList.get(i).getTermId();

                            ApplicableTerm temp;
                            temp = applicableTermList.get(i);
                            temp.setTopic(dataSnapshot.child(termId).child("topic").getValue(String.class));
                            applicableTermList.set(i,temp);
                        }

                        //for setting spinners with the adapters
                        for (int i=0;i<applicableTermList.size();i++){
                            switch (i){
                                case 0:
                                    Log.d("valuesdebug","case 0 "+applicableTermList.get(i).getTopic());

                                    first.add(applicableTermList.get(i).getTopic());
                                    oneAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, first);
                                    Log.d("valuesdebug","adapter is ready ");

                                    spinner1.setAdapter(oneAdapter);
                                    Log.d("valuesdebug","adapter is set successfully ");

                                    break;
                                case 1:
                                    second.add(applicableTermList.get(i).getTopic());
                                    twoAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, second);
                                    spinner2.setAdapter(twoAdapter);
                                    break;
                                case 2:
                                    third.add(applicableTermList.get(i).getTopic());
                                    threeAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, third);
                                    spinner3.setAdapter(threeAdapter);
                                    break;
                                case 3:
                                    fourth.add(applicableTermList.get(i).getTopic());
                                    fourAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, fourth);
                                    spinner4.setAdapter(fourAdapter);
                                    break;

                                default:
                                    Toast.makeText(AvlCategoryActivity.this,"more than limited parameters",Toast.LENGTH_SHORT).show();
                            }
                        }

                        Log.d("codeDebug","before codedebug");
                        codeRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing").child("codes").child(course);

                        codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d("codeDebug","in coderef");

                                Log.d("codeDebug",dataSnapshot.getValue().toString());
                                //show initial image,and booklist invisible
//                                if (!directCall){
//                                    showempty();
//                                }

                                Item currentOne;
                                Item currentTwo;
                                Item currentThree;
                                Item currentFour;

                                //children of term which contain further code names
                                for (int i=0;i<applicableTermList.size();i++){
                                    switch (i){
                                        case 0:
                                            oneData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot branch : oneData.getChildren()) {

                                                currentOne = branch.getValue(Item.class);
                                                OneList.add(currentOne);


                                            }

                                            //sort according to priority
                                            Collections.sort(OneList);

                                            //first will be used to display names.
                                            for (Item item:OneList){
                                                first.add(item.getName());
                                            }
                                            oneAdapter.notifyDataSetChanged();
                                            break;


                                        case 1:
                                            twoData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot year : twoData.getChildren()) {

                                                currentTwo = year.getValue(Item.class);
                                                TwoList.add(currentTwo);

                                            }

                                            //sort according to priority
                                            Collections.sort(TwoList);

                                            //first will be used to display names.
                                            for (Item item:TwoList){
                                                second.add(item.getName());
                                            }

                                            twoAdapter.notifyDataSetChanged();

                                            break;
                                        case 2:
                                            threeData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot sem : threeData.getChildren()) {

                                                currentThree = sem.getValue(Item.class);
                                                ThreeList.add(currentThree);

                                            }
                                            //sort according to priority
                                            Collections.sort(ThreeList);

                                            //first will be used to display names.
                                            for (Item item:ThreeList){
                                                third.add(item.getName());
                                            }

                                            threeAdapter.notifyDataSetChanged();
                                            break;

                                        case 3:
                                            fourData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot publication : fourData.getChildren()) {

                                                currentFour = publication.getValue(Item.class);
                                                FourList.add(currentFour);
                                            }

                                            //sort according to priority
                                            Collections.sort(FourList);

                                            //first will be used to display names.
                                            for (Item item:FourList){
                                                fourth.add(item.getName());
                                            }

                                            fourAdapter.notifyDataSetChanged();
                                            break;
                                    }
                                }


                                if (getIntent().hasExtra("userCourse")){
                                    int i=0;
                                    for (Item currentB:OneList){
                                        Log.d("branchdebug","currentB:"+currentB.getCode()+".... branchName:"+branchCode);
                                        if (currentB.getCode().equals(branchCode)){
                                            spinner1.setSelection(i+1);
                                            break;
                                        }
                                        i++;
                                    }

                                    i=0;
                                    for (Item currentS:ThreeList){
                                        Log.d("branchdebug","currentS:"+currentS.getCode()+".... SName:"+threeCode);
                                        if (currentS.getCode().equals(threeCode)){
                                            spinner3.setSelection(i+1);
                                            break;
                                        }
                                        i++;
                                    }

                                    i=0;
                                    for (Item currentY:TwoList){
                                        Log.d("branchdebug","currentY:"+currentY.getCode()+".... YName:"+twoCode);
                                        if (currentY.getCode().equals(twoCode)){
                                            spinner2.setSelection(i+1);
                                            break;
                                        }
                                        i++;
                                    }

                                    pd.dismiss();

                                    search.performClick();

                                }

                                //for call with sem value
                                else if (getIntent().hasExtra("sem")){

                                    int j=0;

                                    for (int i=0;i<applicableTermList.size();i++){

                                        if (applicableTermList.get(i).getTermId().equals("sem")){

                                            switch (i){
                                                case 0:
                                                    j=0;
                                                    for (Item current:OneList){

                                                         if (current.getCode().equals(getIntent().getStringExtra("sem"))){
                                                            spinner1.setSelection(j+1);
                                                            break;
                                                        }
                                                        j++;
                                                    }
                                                    break;

                                                case 1:
                                                    j=0;
                                                    for (Item current:TwoList){

                                                        if (current.getCode().equals(getIntent().getStringExtra("sem"))){
                                                            spinner2.setSelection(j+1);
                                                            break;
                                                        }
                                                        j++;
                                                    }
                                                    break;

                                                case 2:
                                                    j=0;
                                                    for (Item current:ThreeList){

                                                        if (current.getCode().equals(getIntent().getStringExtra("sem"))){
                                                            spinner3.setSelection(j+1);
                                                            break;
                                                        }
                                                        j++;
                                                    }
                                                    break;

                                                case 3:
                                                    j=0;
                                                    for (Item current:FourList){

                                                        if (current.getCode().equals(getIntent().getStringExtra("sem"))){
                                                            spinner4.setSelection(j+1);
                                                            break;
                                                        }
                                                        j++;
                                                    }
                                                    break;
                                            }
                                        }
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AvlCategoryActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AvlCategoryActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showempty() {

        initial = findViewById(R.id.initial);
        initial.setVisibility(View.VISIBLE);
        bookList.setVisibility(View.GONE);

    }


    //OnCreate ends
//
    private void firebaseSearch(Query query) {

        initial = findViewById(R.id.initial);
        bookList = findViewById(R.id.recycler_view);
        bookList.setVisibility(View.VISIBLE);
        initial.setVisibility(View.GONE);

        FirebaseRecyclerOptions<Book> options = new FirebaseRecyclerOptions.Builder<Book>()
                .setQuery(query,Book.class)
                .build();

        FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>(
              options
        ) {


            @NonNull
            @Override
            public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_row,parent,false);

                return new BookViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BookViewHolder viewHolder, int position, @NonNull Book model) {

                //reset the viewholder before getting data from the countdata
                viewHolder.removeItem.setVisibility(View.GONE);
                viewHolder.add.setText("ADD");
                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                if (model.getAvailability()) {
                    //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                } else {
                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                }


                //set the count data to the viewholder
                cartRequestRef.child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //If user previously added the item
                        if (dataSnapshot.exists()) {

                            //if available set added
                            if (model.getAvailability()) {

                                viewHolder.removeItem.setVisibility(View.VISIBLE);
                                viewHolder.add.setText("ADDED");
                                // viewHolder.add.setTypeface(viewHolder.add.getTypeface(), Typeface.BOLD);

                            }
                            //if added item no longer available
                            else {
                                String id = model.getId();
                                Toast.makeText(AvlCategoryActivity.this,"We're sorry your item is not available at moment",Toast.LENGTH_SHORT).show();
                                cartRequestRef.child(id).removeValue();
                            }


                        }

                        //If is user never changed the basic values of card UI
                        else {

                            viewHolder.removeItem.setVisibility(View.GONE);

                            viewHolder.add.setText("ADD");
                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                            //if book is available
                            if (model.getAvailability()) {
                                viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                            }
                            // if book is unavailable
                            else {
                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AvlCategoryActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });


                //set details of book to card
                viewHolder.setDetails(model.getName(), model.getAuthor(), model.getPublication(), model.getImg(),
                        model.getOriginalPrice(), model.getDiscountedPrice(), model.getDiscount(),
                        model.getAvailability(), getApplicationContext(),"books", model.getVisibility(),model.getCount());


                //add button
                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //check if available
                        if (model.getAvailability()) {
                            String id = model.getId();


                            //get the time to add to item added time
                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);

                            //add to cart
                            CartItem cartItem = new CartItem();
                            cartItem.setTimeAdded(Long.parseLong(date));
                            cartItem.setItemId(id);
                            cartItem.setItemLocation("SPPUbooks/"+course+"/"+bookType);
                            cartItem.setQuantity(1);
                            cartItem.setType("books");
                            //cartItem.setItemDiscount(model.getDiscount());
                            //cartItem.setItemOriginalPrice(model.getOriginalPrice());
                            //cartItem.setItemDiscountedPrice(model.getDiscountedPrice());
                            cartRequestRef.child(id).setValue(cartItem);

                            viewHolder.add.setText("ADDED");
                            //viewHolder.add.setTypeface(viewHolder.add.getTypeface(), Typeface.BOLD_ITALIC);
                            viewHolder.removeItem.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(AvlCategoryActivity.this, "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = model.getId();


                        cartRequestRef.child(id).removeValue();

                        viewHolder.removeItem.setVisibility(View.GONE);


                        viewHolder.add.setText("ADD");
                        viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                        //if book is available
                        if (model.getAvailability()) {
                            //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                            viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                        }
                        // if book is unavailable
                        else {
                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));

                        }

                    }
                });


            }
        };


        bookList.setAdapter(firebaseRecyclerAdapter);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu; this adds items to the action bar if it present
        getMenuInflater().inflate(R.menu.avl_menu, menu);

        MenuItem item = menu.findItem(R.id.search);
        MenuItem cart = menu.findItem(R.id.cart);


        // cart.setIcon(R.drawable.search_icon);

        cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(AvlCategoryActivity.this, CartActivity.class));
                return false;
            }
        });




        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search by Name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query!=null && !query.equals("")){
                    //remove spaces and build query
                    booksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).
                            child(bookType);

                    Query q = booksRef.orderByChild("searchName").startAt(query.toLowerCase().replaceAll("//s+", ""))
                            .endAt(query.toLowerCase().replaceAll("//s+", "") + "\uf8ff");

                    firebaseSearch(q);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter as u type
                if (newText!=null && !newText.equals("")){
                    //remove spaces and build query
                    booksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).
                            child(bookType);

                    Query q = booksRef.orderByChild("searchName").startAt(newText.toLowerCase().replaceAll("//s+", ""))
                            .endAt(newText.toLowerCase().replaceAll("//s+", "") + "\uf8ff");

                    firebaseSearch(q);
                }
                return false;
            }


        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                //Toast.makeText(AvlCategoryActivity.this, "onMenuItemActionExpand called", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                //  Toast.makeText(AvlCategoryActivity.this, "onMenuItemActionCollapse called "+searchCode, Toast.LENGTH_SHORT).show();
                search.performClick();
                Log.d("searchclick","933 menujitemActioncollapse");


                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!post){
            post = true;
        }
        else {
            // To update the count in card if user goes to cart and changes the quantity.
            search.performClick();
            Log.d("searchclick","955 onpostresume");
        }
    }

    @Override
    public void onBackPressed() {

        Intent rIntent = new Intent();
        setResult(RESULT_OK, rIntent);
        finish();
    }

    @Override
    protected void onDestroy() {

        cartRequestRef.removeEventListener(bottomUpdate);
        Log.d("showing", "value event listener is removed");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==111 && resultCode==RESULT_OK){
            Toast.makeText(this, "Course selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AvlCategoryActivity.this,AvlCategoryActivity.class));
            finish();
        }
        else{
            finish();
        }
    }
}
