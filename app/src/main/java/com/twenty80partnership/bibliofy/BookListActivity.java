package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.adapters.MySpinnerAdapter;
import com.twenty80partnership.bibliofy.holders.BookTemplateViewHolder;
import com.twenty80partnership.bibliofy.holders.BookViewHolder;
import com.twenty80partnership.bibliofy.models.ApplicableTerm;
import com.twenty80partnership.bibliofy.models.Book;
import com.twenty80partnership.bibliofy.models.BookTemplate;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.Item;
import com.twenty80partnership.bibliofy.models.SearchIndex;
import com.twenty80partnership.bibliofy.models.UserCourse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class BookListActivity extends AppCompatActivity {

    private final int CARTACTIVITY = 20;
    Boolean year1common = false;
    DataSnapshot oneData;
    DataSnapshot twoData;// = dataSnapshot.child("EnggYear");
    DataSnapshot threeData;// = dataSnapshot.child("EnggSem");
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String category = "", course = "";
    private String combination = "______", oneCode = "__", twoCode = "__", threeCode = "__";
    private ShimmerFrameLayout mShimmerViewContainer;
    private Spinner spinner1, spinner2, spinner3;
    private RelativeLayout rlSpinner1,rlSpinner2,rlSpinner3;
    private TextView search;
    private ImageView initial;
    private TextView showCartCount;
    private RelativeLayout bottom;
    private RecyclerView bookList;
    private ValueEventListener bottomUpdate;
    private DatabaseReference codeRef, templatesRef, cartRequestRef, termsApplicableRef, SPPUtermsListingRef, booksRef;
    private ArrayList<Item> OneList;
    private ArrayList<Item> TwoList;
    private ArrayList<Item> ThreeList;
    private ArrayList<ApplicableTerm> applicableTermList;
    private ProgressDialog pd;
    private MySpinnerAdapter oneAdapter, twoAdapter, threeAdapter;
    private ArrayList<String> first, second, third;
    private Date currentTime;
    private boolean directCall = false;
    private String branchName;
    private LottieAnimationView noItems;
    private Boolean post = false;
    private String branchCode = "__", yearCode = "__", semCode = "__";
    private FirebaseDatabase db;
    private String uid;
    private String bookId;
    private DatabaseReference countDataRef;
    private String oldOneCode;
    private Boolean publicationSystem;
    private SearchView searchView;
    private Toolbar toolbar;
    private  FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<BookTemplate, BookTemplateViewHolder> firebaseRecyclerAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        uid = mAuth.getUid();

        setViews();

        //set toolbar as actionbar and setting title according to bookType
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Books");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        //these will store direct data from database as a object containing codes to search
        OneList = new ArrayList<Item>();
        TwoList = new ArrayList<Item>();
        ThreeList = new ArrayList<Item>();

        //these will be used to show the data in spinners which is obtained by above lists
        first = new ArrayList<String>();
        second = new ArrayList<String>();
        third = new ArrayList<String>();

        pd = new ProgressDialog(this);
        pd.setMessage("Getting your books...");
        pd.setCancelable(false);


        //itent from regular online and theory
        Intent intent = getIntent();
        //Toast.makeText(BookListActivity.this,intent.getStringExtra("bookType"),Toast.LENGTH_SHORT).show();

        if (intent.hasExtra("course")) {
            course = intent.getStringExtra("course");
            category = intent.getStringExtra("category");
            semCode = intent.getStringExtra("sem");


            Toast.makeText(BookListActivity.this, category, Toast.LENGTH_SHORT).show();
        } else if (intent.hasExtra("userCourse")) {

            category = intent.getStringExtra("category");


            pd.show();

            DatabaseReference userCourseRef = db.getReference("Users").child(mAuth.getCurrentUser().getUid())
                    .child("userCourse");

            userCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final UserCourse userCourse = dataSnapshot.getValue(UserCourse.class);

                    if (userCourse != null && userCourse.getCourseCode() != null) {

                        course = userCourse.getCourseCode();

                        if (userCourse.getCourseName()!=null && !userCourse.getCourseName().isEmpty())
                            toolbar.setTitle(userCourse.getCourseName()+" Books");

                        if (userCourse.getBranchCode() != null && !userCourse.getBranchCode().isEmpty())
                            branchCode = userCourse.getBranchCode();

                        if (userCourse.getYearCode() != null && !userCourse.getYearCode().isEmpty())
                            yearCode = userCourse.getYearCode();

                        SPPUtermsListingRef = db.getReference("SPPUtermsListing").child(course);
                        templatesRef = db.getReference("SPPUbooksTemplates").child(course).child(category);
                        booksRef = db.getReference("SPPUbooks").child(course).child(category);

                        DatabaseReference courseDetailsRef = db.getReference("SPPUbooksListing")

                                .child("details").child(userCourse.getCourseCode());

                        courseDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("publicationSystem").exists()){
                                    publicationSystem = dataSnapshot.child("publicationSystem").getValue(Boolean.class);
                                }
                                else{
                                    publicationSystem = null;
                                }
                                if (dataSnapshot.child("currentSem").exists()) {
                                    semCode = dataSnapshot.child("currentSem").getValue(String.class);
                                    //applicable terms and according codes are fetched and set to spinners
                                    setApplicableTermsAndCodes();
                                    //dependency on setApplicable terms and codes as it compares them.
                                    setSpinnersSelectedListener();
                                } else {
                                    setApplicableTermsAndCodes();
                                    setSpinnersSelectedListener();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BookListActivity.this, "bookref access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        //go to select user course.
                        pd.dismiss();
                        Toast.makeText(BookListActivity.this, "Please choose your course", Toast.LENGTH_SHORT).show();

                        Intent courseIntent = new Intent(BookListActivity.this, CourseActivity.class);
                        courseIntent.putExtra("loginFlow", "no");
                        startActivityForResult(courseIntent, 111);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(BookListActivity.this, "usercourseref access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        } else {
            finish();
            Toast.makeText(BookListActivity.this, "course and booktype - (activity parameters) not defined", Toast.LENGTH_SHORT).show();
        }


        cartRequestRef = db.getReference("CartReq").child(mAuth.getCurrentUser().getUid());

        //to update the bottom
        setBottomUpdate();

        //adpters to spinner added providing first second third fourth array
        // setAdaptersToSpinners();


        if (!getIntent().hasExtra("userCourse")) {
            SPPUtermsListingRef = db.getReference("SPPUtermsListing").child(course);

            templatesRef = db.getReference("SPPUbooksTemplates").child(course).child(category);
            termsApplicableRef = db.getReference("SPPUbooksListing")
                    .child("category").child(course).child(category).child("applicableTerms");


            //applicable terms and according codes are fetched and set to spinners
            setApplicableTermsAndCodes();


            //dependency on setApplicable terms and codes as it compares them.
            setSpinnersSelectedListener();
        }


        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(BookListActivity.this, CartActivity.class), CARTACTIVITY);
            }
        });


        search.setOnClickListener(v -> {

            if (publicationSystem!=null && publicationSystem){

                Toast.makeText(BookListActivity.this, "search= " + combination, Toast.LENGTH_SHORT).show();

                Log.d("indexsearch", "search code search.setonclicklistener " + combination);

                //show shimmer
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();
                bookList.setVisibility(View.GONE);

                //query for search and ui config
                Query q = templatesRef.child(combination);//.orderByChild("code").startAt(searchCode).endAt(searchCode + "\uf8ff");

                //listener for setting visibility of bookList and Shimmerview
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            Log.d("animationde", "child found");
                            bookList.setVisibility(View.VISIBLE);
                            noItems.setVisibility(View.GONE);
                        } else {
                            Log.d("animationde", "animation for no child");
                            noItems.setVisibility(View.VISIBLE);
                            bookList.setVisibility(View.GONE);
                            noItems.setProgress(0);
                            noItems.playAnimation();
                        }

                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.startShimmerAnimation();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(BookListActivity.this, "q access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

                firebaseSearchPublicationSystem(q);

            }
            else{
                Toast.makeText(BookListActivity.this, "search= " + combination, Toast.LENGTH_SHORT).show();

                Log.d("indexsearch", "search code search.setonclicklistener " + combination);

                //show shimmer
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();
                bookList.setVisibility(View.GONE);

                //query for search and ui config

                if (combination!=null && !combination.isEmpty()){
                    Query q = booksRef.child(combination);

                    //listener for setting visibility of bookList and Shimmerview
                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() != 0) {
                                Log.d("animationde", "child found");
                                bookList.setVisibility(View.VISIBLE);
                                noItems.setVisibility(View.GONE);
                            } else {
                                Log.d("animationde", "animation for no child");
                                noItems.setVisibility(View.VISIBLE);
                                bookList.setVisibility(View.GONE);
                                noItems.setProgress(0);
                                noItems.playAnimation();
                            }

                            mShimmerViewContainer.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(BookListActivity.this, "q access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    firebaseSearchNoPublicationSystem(q);
                }
                else{
                    Toast.makeText(this, "Combination is null", Toast.LENGTH_SHORT).show();
                }

            }


        });
    }

    private void firebaseSearchNoPublicationSystem(Query q) {

        if (firebaseRecyclerAdapter2!=null)
            firebaseRecyclerAdapter2.stopListening();

        firebaseRecyclerAdapter2 = null;

        FirebaseRecyclerOptions<Book> options = new FirebaseRecyclerOptions.Builder<Book>()
                .setQuery(q,Book.class)
                .build();

       firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>(options) {


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
                    viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                } else {
                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(BookListActivity.this, R.color.dark_gray));
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
                            }
                            //if added item no longer available
                            else {
                                String id = model.getId();
                                Toast.makeText(BookListActivity.this, "We're sorry your item is not available at moment", Toast.LENGTH_SHORT).show();
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
                            }
                            // if book is unavailable
                            else {
                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(BookListActivity.this, R.color.dark_gray));

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(BookListActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });


                //set details of book to card
                viewHolder.setDetails(model.getName(), model.getAuthor(), model.getPublication(), model.getImg(),
                        model.getOriginalPrice(), model.getDiscountedPrice(), model.getDiscount(),
                        model.getAvailability(), BookListActivity.this, "book", model.getVisibility(),model.getCount());


                //add button
                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //check if available
                        if (model.getAvailability()) {
                            String id = model.getId();


                            //get the time to add to item added time
                            Date currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);

                            //add to cart
                            CartItem cartItem = new CartItem();
                            cartItem.setTimeAdded(Long.parseLong(date));
                            cartItem.setItemId(id);
                            cartItem.setItemLocation("SPPUbooks/" + course + "/" +category+"/" + combination);
                            cartItem.setQuantity(1);
                            cartItem.setType("books");

                            cartRequestRef.child(id).setValue(cartItem);

                            viewHolder.add.setText("ADDED");
                            viewHolder.removeItem.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(BookListActivity.this, "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
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
                            viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                        }
                        // if book is unavailable
                        else {
                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(BookListActivity.this, R.color.dark_gray));
                        }

                    }
                });
            }
        };


        bookList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void setViews() {
        spinner1 = findViewById(R.id.branch_spinner);
        spinner2 = findViewById(R.id.year_spinner);
        spinner3 = findViewById(R.id.sem_spinner);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        noItems = findViewById(R.id.no_items);
        bottom = findViewById(R.id.bottom_layout);
        showCartCount = findViewById(R.id.show_cart_count);
        bookList = findViewById(R.id.recycler_view);
        search = findViewById(R.id.search);

        rlSpinner1 = findViewById(R.id.rl_spinner1);
        rlSpinner2 = findViewById(R.id.rl_spinner2);
        rlSpinner3 = findViewById(R.id.rl_spinner3);
    }


    private void setBottomUpdate() {

        bottomUpdate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long count = 0L;

                if (dataSnapshot.exists()) {

                    count = dataSnapshot.getChildrenCount();

                }

                if (dataSnapshot.child("stationary").exists()) {

                    count = count + dataSnapshot.child("stationary").getChildrenCount();
                }

                if (count == 1) {
                    showCartCount.setText(count + " item in Cart");
                } else if (count > 1) {
                    showCartCount.setText(count + " items in Cart");
                }

                if (count > 0)
                    bottom.setVisibility(View.VISIBLE);
                else
                    bottom.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BookListActivity.this, "bottomupdate access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
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
                    oneCode = "__";
                }

                if (twoCode.equals("01") && codeFoundForSelectedString && year1common) {
                    oldOneCode = oneCode;
                    Log.d("year1common", "oldonecode: " + oldOneCode);

                    oneCode = "XX";
                }

                combination = oneCode + twoCode + threeCode;

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
                    twoCode = "__";
                }

                if (twoCode.equals("01") && year1common) {

                    if (!oneCode.equals("XX")) {
                        oldOneCode = oneCode;
                    }
                    oneCode = "XX";
                } else if (oneCode.equals("XX")) {
                    oneCode = oldOneCode;
                }


                combination = oneCode + twoCode + threeCode;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String semSelected = spinner3.getSelectedItem().toString();

                if (semSelected.equals("SEMESTER") && !directCall) {
                    threeCode = "__";
                    combination = oneCode + twoCode + threeCode;
                    return;
                }

                for (int i = 0; i < ThreeList.size(); i++) {
                    if (ThreeList.get(i).getName().equals(semSelected)) {
                        threeCode = ThreeList.get(i).getCode();
                        break;
                    }
                }


                combination = oneCode + twoCode + threeCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setApplicableTermsAndCodes() {

        db.getReference("SPPUbooksListing")
                .child("details").child(course).child("year1common").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    year1common = true;
                } else {
                    year1common = false;
                }


                termsApplicableRef = db.getReference("SPPUbooksListing")
                        .child("category").child(course).child(category).child("applicableTerms");

                termsApplicableRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.getChildrenCount() == 3) {

                        } else if (dataSnapshot.getChildrenCount() == 2) {
                            spinner3.setVisibility(View.INVISIBLE);
                            rlSpinner3.setVisibility(View.INVISIBLE);

                        } else if (dataSnapshot.getChildrenCount() == 1) {
                            spinner2.setVisibility(View.INVISIBLE);
                            rlSpinner2.setVisibility(View.INVISIBLE);

                            spinner3.setVisibility(View.INVISIBLE);
                            rlSpinner3.setVisibility(View.INVISIBLE);
                        } else if (dataSnapshot.getChildrenCount() == 0){

                            spinner1.setVisibility(View.INVISIBLE);
                            rlSpinner1.setVisibility(View.GONE);

                            spinner2.setVisibility(View.INVISIBLE);
                            rlSpinner2.setVisibility(View.GONE);

                            spinner3.setVisibility(View.INVISIBLE);
                            rlSpinner3.setVisibility(View.GONE);

                            search.setVisibility(View.GONE);
                        }

                        //ensuring that no data present in arraylists
                        OneList.clear();
                        TwoList.clear();
                        ThreeList.clear();

                        first.clear();
                        second.clear();
                        third.clear();

                        applicableTermList = new ArrayList<ApplicableTerm>();

                        if (dataSnapshot.getChildrenCount() == 0) {
                            Toast.makeText(BookListActivity.this, "No Search Data Found for this Course", Toast.LENGTH_SHORT).show();
                        } else {

                            int i = 1;

                            Log.d("valuesdebug", "line 589");


                            for (DataSnapshot currentApplicable : dataSnapshot.getChildren()) {


                                ApplicableTerm applicableTerm = currentApplicable.getValue(ApplicableTerm.class);
                                applicableTermList.add(applicableTerm);

                                switch (i) {
                                    case 1:
                                        switch (applicableTerm.getTermId()) {
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
                                        switch (applicableTerm.getTermId()) {
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
                                        switch (applicableTerm.getTermId()) {
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
                                }

                                i++;
                            }

                            combination = oneCode+twoCode+threeCode;

                            //todo if publication system then remove countdata

                            countDataRef = db.getReference("CountData").child(uid).child(course).child(category).child(combination);


                            countDataRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(BookListActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(BookListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }

                        SPPUtermsListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String termId = "";

                                Log.d("valuesdebug", "line 672 termslisting " + dataSnapshot.getValue());

                                //for setting topics of term from termsListingRef
                                for (int i = 0; i < applicableTermList.size(); i++) {
                                    termId = applicableTermList.get(i).getTermId();

                                    ApplicableTerm temp;
                                    temp = applicableTermList.get(i);
                                    temp.setTopic(dataSnapshot.child(termId).child("topic").getValue(String.class));
                                    applicableTermList.set(i, temp);
                                }

                                //for setting spinners with the adapters
                                for (int i = 0; i < applicableTermList.size(); i++) {
                                    switch (i) {
                                        case 0:
                                            Log.d("valuesdebug", "case 0 " + applicableTermList.get(i).getTopic());

                                            first.add(applicableTermList.get(i).getTopic());
                                            oneAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, first);
                                            Log.d("valuesdebug", "adapter is ready ");

                                            spinner1.setAdapter(oneAdapter);
                                            Log.d("valuesdebug", "adapter is set successfully ");

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

                                        default:
                                            Toast.makeText(BookListActivity.this, "more than limited parameters", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                Log.d("codeDebug", "before codedebug");
                                codeRef = db.getReference("SPPUbooksListing").child("codes").child(course);

                                codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d("codeDebug", "in coderef");

                                        //show initial image,and booklist invisible
//                                if (!directCall){
//                                    showempty();
//                                }

                                        Item currentOne;
                                        Item currentTwo;
                                        Item currentThree;
                                        Item currentFour;

                                        //children of term which contain further code names
                                        for (int i = 0; i < applicableTermList.size(); i++) {
                                            switch (i) {
                                                case 0:
                                                    oneData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                                    for (DataSnapshot branch : oneData.getChildren()) {

                                                        currentOne = branch.getValue(Item.class);
                                                        OneList.add(currentOne);


                                                    }

                                                    //sort according to priority
                                                    Collections.sort(OneList);

                                                    //first will be used to display names.
                                                    for (Item item : OneList) {
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
                                                    for (Item item : TwoList) {
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
                                                    for (Item item : ThreeList) {
                                                        third.add(item.getName());
                                                    }

                                                    threeAdapter.notifyDataSetChanged();
                                                    break;
                                            }
                                        }


                                        if (getIntent().hasExtra("userCourse")) {
                                            int i = 0;
                                            for (Item currentB : OneList) {
                                                Log.d("branchdebug", "currentB:" + currentB.getCode() + ".... branchName:" + branchCode);
                                                if (currentB.getCode().equals(oneCode)) {
                                                    spinner1.setSelection(i + 1);
                                                    break;
                                                }
                                                i++;
                                            }

                                            i = 0;
                                            for (Item currentS : ThreeList) {
                                                Log.d("branchdebug", "currentS:" + currentS.getCode() + ".... SName:" + threeCode);
                                                if (currentS.getCode().equals(threeCode)) {
                                                    spinner3.setSelection(i + 1);
                                                    break;
                                                }
                                                i++;
                                            }

                                            i = 0;
                                            for (Item currentY : TwoList) {
                                                Log.d("branchdebug", "currentY:" + currentY.getCode() + ".... YName:" + twoCode);
                                                if (currentY.getCode().equals(twoCode)) {
                                                    spinner2.setSelection(i + 1);
                                                    break;
                                                }
                                                i++;
                                            }

                                            pd.dismiss();

                                            search.performClick();

                                        }

                                        //for call with sem value
                                        else if (getIntent().hasExtra("sem")) {

                                            int j = 0;

                                            for (int i = 0; i < applicableTermList.size(); i++) {

                                                if (applicableTermList.get(i).getTermId().equals("sem")) {

                                                    switch (i) {
                                                        case 0:
                                                            j = 0;
                                                            for (Item current : OneList) {

                                                                if (current.getCode().equals(getIntent().getStringExtra("sem"))) {
                                                                    spinner1.setSelection(j + 1);
                                                                    break;
                                                                }
                                                                j++;
                                                            }
                                                            break;

                                                        case 1:
                                                            j = 0;
                                                            for (Item current : TwoList) {

                                                                if (current.getCode().equals(getIntent().getStringExtra("sem"))) {
                                                                    spinner2.setSelection(j + 1);
                                                                    break;
                                                                }
                                                                j++;
                                                            }
                                                            break;

                                                        case 2:
                                                            j = 0;
                                                            for (Item current : ThreeList) {

                                                                if (current.getCode().equals(getIntent().getStringExtra("sem"))) {
                                                                    spinner3.setSelection(j + 1);
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
                                        Toast.makeText(BookListActivity.this, "coderef access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BookListActivity.this, "Terms listing access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(BookListActivity.this, "terms applicable access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    private void firebaseSearchPublicationSystem(Query query) {

        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();

        firebaseRecyclerAdapter = null;

        initial = findViewById(R.id.initial);
        bookList = findViewById(R.id.recycler_view);
        bookList.setVisibility(View.VISIBLE);
        initial.setVisibility(View.GONE);

        FirebaseRecyclerOptions<BookTemplate> options = new FirebaseRecyclerOptions.Builder<BookTemplate>()
                .setQuery(query ,BookTemplate.class)
                .build();


        firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<BookTemplate, BookTemplateViewHolder>(options) {

                    @NonNull
                    @Override
                    public BookTemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.book_template_row,parent,false);


                        return new BookTemplateViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull BookTemplateViewHolder viewHolder, int position, @NonNull BookTemplate bookTemplate) {

                        bookId = "";
                        final int[] previousSpinnerIndex = {-1};

                        if (bookTemplate.getName()!=null)
                            viewHolder.tvName.setText(bookTemplate.getName());

                        if (bookTemplate.getDefaultBookId() != null && !bookTemplate.getDefaultBookId().equals("")) {
                            viewHolder.updateHideStatus(true);
                        } else {
                            viewHolder.updateHideStatus(false);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewHolder.spPublication.performClick();
                            }
                        });


                        db.getReference("CountData").child(uid).child(course)
                                .child(category).child(combination)
                                .child(bookTemplate.getTemplateId())
                                .child("selectedBookId").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final boolean defaultBook;
                                if (dataSnapshot.exists()) {
                                    defaultBook = false;
                                    bookId = dataSnapshot.getValue(String.class);
                                } else {
                                    defaultBook = true;
                                    if (bookTemplate.getDefaultBookId() != null && !bookTemplate.getDefaultBookId().equals("")) {
                                        bookId = bookTemplate.getDefaultBookId();
                                    } else {
                                        bookId = "nonEmpty";
                                    }
                                }

                                booksRef.child(combination).child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            final Book book = dataSnapshot.getValue(Book.class);

                                            //////////////////////////////////////////////////////////////////////////////////////
                                            //these will store direct data from database as a object containing codes to search
                                            final ArrayList<Item> publicationDataList = new ArrayList<Item>();

                                            //these will be used to show the data in spinners which is obtained by above lists
                                            final ArrayList<String> publicationDisplayList = new ArrayList<String>();

                                            codeRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing").child("codes").child(course);


                                            publicationDisplayList.add("-Change Publication-");
                                            final MySpinnerAdapter publicationAdapter = new MySpinnerAdapter(getApplicationContext(), R.layout.spinner_item, publicationDisplayList);
                                            viewHolder.spPublication.setAdapter(publicationAdapter);


                                            codeRef.child("publication").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Item currentPublication;

                                                    //children of term which contain further codes


                                                    for (DataSnapshot publication : dataSnapshot.getChildren()) {

                                                        currentPublication = publication.getValue(Item.class);
                                                        publicationDataList.add(currentPublication);
                                                        publicationDisplayList.add(currentPublication.getName());

                                                    }
                                                    publicationAdapter.notifyDataSetChanged();


                                                    viewHolder.spPublication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parent, final View view, int position, long id) {

                                                            //obtain the  selected branch from spinner
                                                            String publicationSelected = viewHolder.spPublication.getSelectedItem().toString();

                                                            boolean codeFoundForSelectedString = false;

                                                            if (viewHolder.spPublication.getSelectedItemPosition() != 0) {

                                                                //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                                for (int i = 0; i < publicationDataList.size(); i++) {

                                                                    if (publicationDataList.get(i).getName().equals(publicationSelected)) {

                                                                        final int j = i;

                                                                        booksRef.child(combination).
                                                                                child(bookTemplate.getTemplateId() + publicationDataList.get(i).getCode() + category).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                                bookId = bookTemplate.getTemplateId() + publicationDataList.get(j).getCode() + category;

                                                                                if (dataSnapshot.exists()) {

                                                                                    db.getReference("CountData").child(uid).child(course)
                                                                                            .child(category).child(combination)
                                                                                            .child(bookTemplate.getTemplateId())
                                                                                            .child("selectedBookId").setValue(bookId);

                                                                                    final Book book = dataSnapshot.getValue(Book.class);

                                                                                    //reset the viewholder before getting data from the countdata

                                                                                    viewHolder.removeItem.setVisibility(View.GONE);
                                                                                    viewHolder.add.setText("ADD");
                                                                                    viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                                                                    if (book.getCount() == 0) {
                                                                                        viewHolder.tvOutOfStock.setVisibility(View.VISIBLE);
                                                                                        viewHolder.removeItem.setVisibility(View.GONE);
                                                                                        viewHolder.add.setVisibility(View.GONE);
                                                                                    } else {
                                                                                        viewHolder.tvOutOfStock.setVisibility(View.GONE);
                                                                                        viewHolder.add.setVisibility(View.VISIBLE);
                                                                                    }


                                                                                    if (book.getAvailability()) {
                                                                                        //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                                                                                        viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                                                                    } else {
                                                                                        viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                                                                                    }


                                                                                    //set the count data to the viewholder
                                                                                    cartRequestRef.child(book.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                                            //If user previously added the item
                                                                                            if (dataSnapshot.exists()) {

                                                                                                //if available set added
                                                                                                if (book.getAvailability()) {


                                                                                                    if (book.getCount() == 0) {
                                                                                                        viewHolder.tvOutOfStock.setVisibility(View.VISIBLE);
                                                                                                        viewHolder.removeItem.setVisibility(View.GONE);
                                                                                                        viewHolder.add.setVisibility(View.GONE);
                                                                                                    } else {
                                                                                                        viewHolder.tvOutOfStock.setVisibility(View.GONE);
                                                                                                        viewHolder.add.setVisibility(View.VISIBLE);

                                                                                                        viewHolder.removeItem.setVisibility(View.VISIBLE);
                                                                                                        viewHolder.add.setText("ADDED");
                                                                                                    }


                                                                                                    // viewHolder.add.setTypeface(viewHolder.add.getTypeface(), Typeface.BOLD);

                                                                                                }
                                                                                                //if added item no longer available
                                                                                                else {
                                                                                                    String id = book.getId();
                                                                                                    Toast.makeText(BookListActivity.this, "We're sorry your item is not available at moment", Toast.LENGTH_SHORT).show();
                                                                                                    cartRequestRef.child(id).removeValue();
                                                                                                }


                                                                                            }

                                                                                            //If is user never changed the basic values of card UI
                                                                                            else {

                                                                                                viewHolder.removeItem.setVisibility(View.GONE);

                                                                                                viewHolder.add.setText("ADD");
                                                                                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                                                                                if (book.getCount() == 0) {
                                                                                                    viewHolder.tvOutOfStock.setVisibility(View.VISIBLE);
                                                                                                    viewHolder.removeItem.setVisibility(View.GONE);
                                                                                                    viewHolder.add.setVisibility(View.GONE);
                                                                                                } else {
                                                                                                    viewHolder.tvOutOfStock.setVisibility(View.GONE);
                                                                                                    viewHolder.add.setVisibility(View.VISIBLE);
                                                                                                }

                                                                                                //if book is available
                                                                                                if (book.getAvailability()) {
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
                                                                                            Toast.makeText(BookListActivity.this, "cartrequest access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });


                                                                                    //set details of book to card
                                                                                    viewHolder.setDetailsForTemplate(book.getAuthor(), book.getPublication(), book.getImg(),
                                                                                            book.getOriginalPrice(), book.getDiscountedPrice(), book.getDiscount());


                                                                                    //add button
                                                                                    viewHolder.add.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {
                                                                                            //check if available
                                                                                            if (book.getAvailability()) {
                                                                                                String id = book.getId();


                                                                                                //get the time to add to item added time
                                                                                                currentTime = Calendar.getInstance().getTime();
                                                                                                String date = dateFormat.format(currentTime);

                                                                                                //add to cart
                                                                                                CartItem cartItem = new CartItem();
                                                                                                cartItem.setTimeAdded(Long.parseLong(date));
                                                                                                cartItem.setItemId(id);
                                                                                                cartItem.setItemLocation("SPPUbooks/" + course + "/" + category + "/" + combination);
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
                                                                                                Toast.makeText(BookListActivity.this, "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
                                                                                            }

                                                                                        }
                                                                                    });


                                                                                    viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {
                                                                                            String id = book.getId();


                                                                                            cartRequestRef.child(id).removeValue();

                                                                                            viewHolder.removeItem.setVisibility(View.GONE);


                                                                                            viewHolder.add.setText("ADD");
                                                                                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                                                                            //if book is available
                                                                                            if (book.getAvailability()) {
                                                                                                //viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                                                                                                viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                                                                                            }
                                                                                            // if book is unavailable
                                                                                            else {
                                                                                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));

                                                                                            }

                                                                                        }
                                                                                    });

                                                                                    if (previousSpinnerIndex[0] == -1) {
                                                                                        previousSpinnerIndex[0] = 0;
                                                                                        //after 1st time load of publication spinner take it to 1st index
                                                                                        viewHolder.spPublication.setSelection(0);
                                                                                    }
                                                                                } else {
                                                                                    viewHolder.spPublication.setSelection(0);
                                                                                    viewHolder.spPublication.performClick();
                                                                                    Toast.makeText(BookListActivity.this, "selected book doesn't exist old is kept " + previousSpinnerIndex[0], Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                Toast.makeText(BookListActivity.this, "booksref access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });

                                                                        break;
                                                                    }
                                                                }

                                                            }


                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {

                                                        }
                                                    });


                                                    //adding data from code child to arrayLists
                                                    //after adding data to the arrayLists the adapter need to update
                                                    //for branch
                                                    //obtain the  selected branch from spinner
                                                    String comparePub;
                                                    if (defaultBook) {
                                                        comparePub = bookTemplate.getDefaultPublication();
                                                    } else {
                                                        comparePub = book.getPublication();
                                                    }

                                                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < publicationDataList.size(); i++) {


                                                        Log.d("bookListdebug", " comparePub:" + comparePub + " getfrom list:" + publicationDataList.get(i).getName());

                                                        if (publicationDataList.get(i).getName().equals(comparePub)) {

                                                            viewHolder.spPublication.setSelection(i + 1);
                                                            previousSpinnerIndex[0] = -1;

                                                            break;
                                                        }

                                                    }
                                                    Log.d("bookListdebug", " ");


                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                                        } else {
                                            Toast.makeText(BookListActivity.this, "selected book doesn't exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(BookListActivity.this, "booksref access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BookListActivity.this, "countData access " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }                };


        bookList.setAdapter(firebaseRecyclerAdapter2);
        firebaseRecyclerAdapter2.startListening();

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
                startActivityForResult(new Intent(BookListActivity.this, CartActivity.class), CARTACTIVITY);
                return false;
            }
        });


        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search by Name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query != null && !query.equals("")) {
                    //remove spaces and build query
                    DatabaseReference searchIndexRef = db.getReference("SearchIndex").child("SPPU");

                    Query q = searchIndexRef.orderByChild("searchName").startAt(query.toLowerCase().replaceAll("//s+", ""))
                            .endAt(query.toLowerCase().replaceAll("//s+", "") + "\uf8ff");

                    firebaseSearchNoPublicationSystem(q);

                    //listener for setting visibility of bookList and Shimmerview
                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() != 0) {
                                Log.d("animationde", "child found");
                                bookList.setVisibility(View.VISIBLE);
                                noItems.setVisibility(View.GONE);
                            } else {
                                Log.d("animationde", "animation for no child");
                                noItems.setVisibility(View.VISIBLE);
                                bookList.setVisibility(View.GONE);
                                noItems.setProgress(0);
                                noItems.playAnimation();
                            }

                            mShimmerViewContainer.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(BookListActivity.this, "q access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter as u type
                if (newText != null && !newText.equals("")) {

                    Log.d("bladebug",">"+newText+"<");
                    //remove spaces and build query
                    DatabaseReference searchIndexRef = db.getReference("SearchIndex").child("SPPU");

                    Query q = searchIndexRef.orderByChild("searchName").startAt(newText.toLowerCase().replaceAll("//s+", ""))
                            .endAt(newText.toLowerCase().replaceAll("//s+", "") + "\uf8ff");

                    firebaseSearchNoPublicationSystem(q);

                    //listener for setting visibility of bookList and Shimmerview
                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() != 0) {
                                Log.d("animationde", "child found");
                                bookList.setVisibility(View.VISIBLE);
                                noItems.setVisibility(View.GONE);
                            } else {
                                Log.d("animationde", "animation for no child");
                                noItems.setVisibility(View.VISIBLE);
                                bookList.setVisibility(View.GONE);
                                noItems.setProgress(0);
                                noItems.playAnimation();
                            }

                            mShimmerViewContainer.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(BookListActivity.this, "q access " + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                return false;
            }


        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                LinearLayout llSearch1 = findViewById(R.id.ll_search1);
                LinearLayout llSearch2 = findViewById(R.id.ll_search2);

                bookList.setAdapter(null);
                llSearch1.setVisibility(View.GONE);
                llSearch2.setVisibility(View.GONE);
                //Toast.makeText(BookListActivity.this, "onMenuItemActionExpand called", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                LinearLayout llSearch1 = findViewById(R.id.ll_search1);
                LinearLayout llSearch2 = findViewById(R.id.ll_search2);

                llSearch1.setVisibility(View.VISIBLE);
                llSearch2.setVisibility(View.VISIBLE);
                //  Toast.makeText(BookListActivity.this, "onMenuItemActionCollapse called "+searchCode, Toast.LENGTH_SHORT).show();
                search.performClick();
                Log.d("searchclick", "933 menujitemActioncollapse");


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

        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();

        if (firebaseRecyclerAdapter2!=null)
            firebaseRecyclerAdapter2.stopListening();
        Log.d("showing", "value event listener is removed");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {

                Toast.makeText(this, "Course selected", Toast.LENGTH_SHORT).show();
                Intent courseBookIntent = new Intent(BookListActivity.this, BookListActivity.class);

                courseBookIntent.putExtra("userCourse", "true");
                courseBookIntent.putExtra("category", "regular");

                finish();
                startActivity(courseBookIntent);
            } else {

                finish();
            }

        }

        if (requestCode == CARTACTIVITY) {
            if (resultCode == RESULT_OK) {
                finish();
            } else {

                toolbar.collapseActionView();

                LinearLayout llSearch1 = findViewById(R.id.ll_search1);
                LinearLayout llSearch2 = findViewById(R.id.ll_search2);

                llSearch1.setVisibility(View.VISIBLE);
                llSearch2.setVisibility(View.VISIBLE);
                search.performClick();
            }
        }
    }
}
