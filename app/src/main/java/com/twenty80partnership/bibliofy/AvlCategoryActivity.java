package com.twenty80partnership.bibliofy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.holders.BookViewHolder;
import com.twenty80partnership.bibliofy.modules.Book;
import com.twenty80partnership.bibliofy.modules.Branch;
import com.twenty80partnership.bibliofy.modules.CartItem;
import com.twenty80partnership.bibliofy.modules.CodesApplicable;
import com.twenty80partnership.bibliofy.modules.Item;
import com.twenty80partnership.bibliofy.modules.Publication;
import com.twenty80partnership.bibliofy.modules.Sem;
import com.twenty80partnership.bibliofy.modules.Year;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AvlCategoryActivity extends AppCompatActivity {


    private ShimmerFrameLayout mShimmerViewContainer;
    private Spinner spinner1, spinner2, spinner3, spinner4;
    private TextView search;
    private ImageView initial;
    private TextView showCartCount;
    private LinearLayout bottom;
    private RecyclerView bookList;

    private ValueEventListener bottomUpdate;
    private DatabaseReference codeRef, booksRef, cartRequestRef, currentSemRef,codesApplicableRef,SPPUcodesListingRef;

    private ArrayList <Item> OneList;
    private ArrayList<Item> TwoList;
    private ArrayList<Item> ThreeList;
    private ArrayList<Item> FourList;

    private ArrayList<CodesApplicable> codesApplicableList;


    DataSnapshot oneData;
    DataSnapshot twoData;// = dataSnapshot.child("EnggYear");
    DataSnapshot threeData;// = dataSnapshot.child("EnggSem");
    DataSnapshot fourData ;

    private ArrayAdapter<String> oneAdapter,twoAdapter,threeAdapter,fourAdapter ;
    private ArrayList<String> first, second, third, fourth;
    private String searchCode = " ",oneCode = "",twoCode = "",threeCode = "",fourCode = "";
    private Date currentTime;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private boolean directCall=false;
    private String branchName;
    String bookType="",course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avl_category);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        //itent from regular online and theory
        Intent intent = getIntent();
        //Toast.makeText(AvlCategoryActivity.this,intent.getStringExtra("bookType"),Toast.LENGTH_SHORT).show();

        if (intent.hasExtra("course")) {
            course = intent.getStringExtra("course");
            bookType = intent.getStringExtra("bookType");
            Toast.makeText(AvlCategoryActivity.this,bookType,Toast.LENGTH_SHORT).show();
        }
        else {
            bookType = "Default";
            Toast.makeText(AvlCategoryActivity.this,"default",Toast.LENGTH_SHORT).show();
        }

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        //set toolbar as actionbar and setting title according to bookType
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (bookType) {
            case "Regular":
                toolbar.setTitle("Books");
                break;
            case "Online":
                toolbar.setTitle("Online Exam Books");
                break;
            case "Theory":
                toolbar.setTitle("End-Sem Exam Books");
                break;
            default:
                toolbar.setTitle("Error");
                break;
        }


        bookList = findViewById(R.id.recycler_view);

        bookList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        spinner1 = findViewById(R.id.branch_spinner);
        spinner2 = findViewById(R.id.year_spinner);
        spinner3 = findViewById(R.id.sem_spinner);
        spinner4 = findViewById(R.id.publication_spinner);

        bottom = findViewById(R.id.bottom_layout);
        showCartCount = findViewById(R.id.show_cart_count);

        search = findViewById(R.id.search);

        //for direct call from dashboard
        if (intent.getStringExtra("branchCode")!=null && intent.getStringExtra("yearCode")!=null){
            directCall = true;
            branchName =  intent.getStringExtra("branchCode");
            oneCode = intent.getStringExtra("branchCode");
            twoCode = intent.getStringExtra("yearCode");

            if (twoCode.equals("1")) {
                oneCode = "XX";
            }

            //fetch currentsem from database for search
            currentSemRef = FirebaseDatabase.getInstance().getReference("CurrentSem");

            //fetch and search for directCall
            currentSemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    threeCode = dataSnapshot.getValue(String.class);
                    searchCode = oneCode + twoCode + threeCode;
                    search.performClick();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

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


        codesApplicableRef = FirebaseDatabase.getInstance()
                .getReference("SPPUbooksListing").child(course).child("category").child(bookType).child("codesApplicable");

        SPPUcodesListingRef = FirebaseDatabase.getInstance().getReference("SPPUcodesListing").child(course);

        codeRef = FirebaseDatabase.getInstance().getReference("SPPUcodes").child(course);
        booksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(bookType);
        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getCurrentUser().getUid());

        //to update the bottom
        bottomUpdate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long count=0L;

                if (dataSnapshot.child("books").exists()){

                    count = dataSnapshot.child("books").getChildrenCount();

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

        //adding layout and arrayList of Strings to the adapters
        oneAdapter = new ArrayAdapter(this, R.layout.spinner_item, first);
        twoAdapter = new ArrayAdapter(this, R.layout.spinner_item, second);
        threeAdapter = new ArrayAdapter(this, R.layout.spinner_item, third);
        fourAdapter = new ArrayAdapter(this, R.layout.spinner_item, fourth);

        //adding adapter to spinners
        spinner1.setAdapter(oneAdapter);
        spinner2.setAdapter(twoAdapter);
        spinner3.setAdapter(threeAdapter);
        spinner4.setAdapter(fourAdapter);




        codesApplicableRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
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

                codesApplicableList = new ArrayList<CodesApplicable>();

                if (dataSnapshot.getChildrenCount()==0){
                    Toast.makeText(AvlCategoryActivity.this,"No Search Data Found for this Course",Toast.LENGTH_SHORT).show();
                }
                else {
                    for (DataSnapshot currentApplicable:dataSnapshot.getChildren()){
                        CodesApplicable codesApplicable = currentApplicable.getValue(CodesApplicable.class);
                        codesApplicableList.add(codesApplicable);
                    }
                }

                SPPUcodesListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String codeId="";


                        for (int i=0;i<codesApplicableList.size();i++){
                            codeId  = codesApplicableList.get(i).getCodeId();

                            CodesApplicable temp;
                            temp = codesApplicableList.get(i);
                            temp.setTopic(dataSnapshot.child(codeId).child("topic").getValue(String.class));
                            codesApplicableList.set(i,temp);
                        }


                        for (int i=0;i<codesApplicableList.size();i++){
                            switch (i){
                                case 0:
                                    first.add(codesApplicableList.get(i).getTopic());
                                    oneAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, first);
                                    spinner1.setAdapter(oneAdapter);
                                    break;
                                case 1:
                                    second.add(codesApplicableList.get(i).getTopic());
                                    twoAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, second);
                                    spinner2.setAdapter(twoAdapter);
                                    break;
                                case 2:
                                    third.add(codesApplicableList.get(i).getTopic());
                                    threeAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, third);
                                    spinner3.setAdapter(threeAdapter);
                                    break;
                                case 3:
                                    fourth.add(codesApplicableList.get(i).getTopic());
                                    fourAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, fourth);
                                    spinner4.setAdapter(fourAdapter);
                                    break;

                                default:
                                    Toast.makeText(AvlCategoryActivity.this,"more than limited parameters",Toast.LENGTH_SHORT).show();
                            }
                        }


                        codeRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //show initial image,and booklist invisible
                                if (!directCall){
                                    showempty();
                                }

                                Item currentOne;
                                Item currentTwo;
                                Item currentThree;
                                Item currentFour;

                                //children of code which contain further codes
                                for (int i=0;i<codesApplicableList.size();i++){

                                    switch (i){

                                        case 0:
                                            oneData = dataSnapshot.child(codesApplicableList.get(i).getCodeId());

                                            for (DataSnapshot branch : oneData.getChildren()) {

                                                currentOne = branch.getValue(Item.class);
                                                OneList.add(currentOne);
                                                first.add(currentOne.getName());

                                            }
                                            oneAdapter.notifyDataSetChanged();
                                            break;


                                        case 1:
                                            twoData = dataSnapshot.child(codesApplicableList.get(i).getCodeId());

                                            for (DataSnapshot year : twoData.getChildren()) {

                                                currentTwo = year.getValue(Item.class);
                                                TwoList.add(currentTwo);
                                                second.add(currentTwo.getName());

                                            }
                                            twoAdapter.notifyDataSetChanged();
                                            break;


                                        case 2:
                                            threeData = dataSnapshot.child(codesApplicableList.get(i).getCodeId());

                                            for (DataSnapshot sem : threeData.getChildren()) {

                                                currentThree = sem.getValue(Item.class);
                                                ThreeList.add(currentThree);
                                                third.add(currentThree.getName());

                                            }
                                            threeAdapter.notifyDataSetChanged();
                                            break;


                                        case 3:
                                            fourData = dataSnapshot.child(codesApplicableList.get(i).getCodeId());

                                            for (DataSnapshot publication : fourData.getChildren()) {

                                                currentFour = publication.getValue(Item.class);
                                                FourList.add(currentFour);
                                                fourth.add(currentFour.getName());

                                            }
                                            fourAdapter.notifyDataSetChanged();
                                            break;


                                    }
                                }


                                if (directCall){
                                    int i=0;
                                    for (Item currentB:OneList){
                                        Log.d("branchdebug","currentB:"+currentB.getCode()+".... branchName:"+branchName);
                                        if (currentB.getCode().equals(branchName)){
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

                if (!codeFoundForSelectedString&&!directCall) {
                    oneCode = " ";
                }

                if (twoCode.equals("1")) {
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

                if (!codeFoundForSelectedString && !directCall) {
                    twoCode = " ";
                }

                if (twoCode.equals("1")) {
                    oneCode = "XX";
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

                //show shimmer
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();

                //query for search and ui config
                Query q = booksRef.orderByChild("code").startAt(searchCode).endAt(searchCode + "\uf8ff");

                //directcall is affecting regular calls, it should be handled properly
                //directCall = false;

                //listener for setting visibility of bookList and Shimmerview
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {

                            bookList.setVisibility(View.VISIBLE);

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
        initial.setVisibility(View.GONE);

        FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>(
                Book.class, R.layout.book_row, BookViewHolder.class, query
        ) {


            @Override
            protected Book parseSnapshot(DataSnapshot snapshot) {
                return super.parseSnapshot(snapshot);

            }

            @Override
            protected void populateViewHolder(final BookViewHolder viewHolder, final Book model, final int position) {

                //reset the viewholder before getting data from the countdata
                viewHolder.removeLayout.setVisibility(View.GONE);
                viewHolder.add.setText("ADD");
                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                if (model.getAvailability()) {
                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                } else {
                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                }


                //set the count data to the viewholder
                cartRequestRef.child("books").child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //If user previously added the item
                        if (dataSnapshot.exists()) {

                            //if available set added
                            if (model.getAvailability()) {

                                viewHolder.removeLayout.setVisibility(View.VISIBLE);
                                viewHolder.add.setText("ADDED");
                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.neongreen));
                                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);

                            }
                            //if added item no longer available
                            else {
                                String id = model.getId();
                                Toast.makeText(AvlCategoryActivity.this,"We're sorry your item is not available at moment",Toast.LENGTH_SHORT).show();
                                cartRequestRef.child("books").child(id).removeValue();
                            }


                        }

                        //If is user never changed the basic values of card UI
                        else {

                            viewHolder.removeLayout.setVisibility(View.GONE);

                            viewHolder.add.setText("ADD");
                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                            //if book is available
                            if (model.getAvailability()) {
                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
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
                        model.getAvailability(), getApplicationContext());


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
                            cartItem.setItemLocation("SPPUbooks/"+"EnggBooks/"+bookType);
                            cartItem.setQuantity(1);
                            //cartItem.setItemDiscount(model.getDiscount());
                            //cartItem.setItemOriginalPrice(model.getOriginalPrice());
                            //cartItem.setItemDiscountedPrice(model.getDiscountedPrice());
                            cartRequestRef.child("books").child(id).setValue(cartItem);

                            viewHolder.add.setText("ADDED");
                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.neongreen));
                            viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);


                            viewHolder.removeLayout.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(AvlCategoryActivity.this, "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                viewHolder.removeItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = model.getId();


                        cartRequestRef.child("books").child(id).removeValue();

                        viewHolder.removeLayout.setVisibility(View.GONE);


                        viewHolder.add.setText("ADD");
                        viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                        //if book is available
                        if (model.getAvailability()) {
                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
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

        MenuItem item = menu.findItem(R.id.action_search);
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

                //remove spaces and build query
                Query q = booksRef.orderByChild("searchName").startAt(query.toLowerCase().replaceAll("//s+", ""))
                        .endAt(query.toLowerCase().replaceAll(" ", "") + "\uf8ff");

                firebaseSearch(q);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter as u type
                Query q = booksRef.orderByChild("searchName").startAt(newText.toLowerCase().replaceAll("//s+", ""))
                        .endAt(newText.toLowerCase().replaceAll(" ", "") + "\uf8ff");

                firebaseSearch(q);
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // To update the count in card if user goes to cart and changes the quantity.
        search.performClick();
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
}
