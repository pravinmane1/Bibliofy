package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.RBTreeSortedMap;
import com.twenty80partnership.bibliofy.adapters.MySpinnerAdapter;
import com.twenty80partnership.bibliofy.holders.BookTemplateViewHolder;
import com.twenty80partnership.bibliofy.holders.CartItemViewHolder;
import com.twenty80partnership.bibliofy.holders.MenuItemViewHolder;
import com.twenty80partnership.bibliofy.holders.ReturnBookViewHolder;
import com.twenty80partnership.bibliofy.holders.TemplateViewHolder;
import com.twenty80partnership.bibliofy.models.Address;
import com.twenty80partnership.bibliofy.models.Book;
import com.twenty80partnership.bibliofy.models.BookTemplate;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.Course;
import com.twenty80partnership.bibliofy.models.Item;
import com.twenty80partnership.bibliofy.models.ItemMeta;
import com.twenty80partnership.bibliofy.models.MenuItem1;
import com.twenty80partnership.bibliofy.models.PriceDetails;
import com.twenty80partnership.bibliofy.models.ReturnBook;
import com.twenty80partnership.bibliofy.models.StationaryItem;
import com.twenty80partnership.bibliofy.models.UserCourse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private final boolean changeAddress = false;
    private final boolean oos = false;
    private final String[] courseCode = new String[1];
    private final String[] semCode = new String[1];
    private final String[] yearCode = new String[1];
    private final String[] branchCode = new String[1];
    Long cartChildrenCount = 0L;
    FirebaseAuth mAuth;
    String uid;
    Integer charges;
    String pin = "000000";
    boolean[] selected = new boolean[20];
    int[] price = new int[20];
    private ShimmerFrameLayout mShimmerViewContainer;
    private RecyclerView bookList;
    private TextView pinView, totalItems, amountItems, amountDelivery, amountTotal, amountSavings, payableAmount, continueBtn, noItemText;
    private CardView bottom;
    private LinearLayout priceDetails;
    private ProgressDialog pricePd, pd, simplePd, oldDeliveryPd;
    private LottieAnimationView noItemInCart;
    private DatabaseReference cartRef, cartRequestRef, rootRef, wishlistRef, priceDetailsRef, deliveryRef, addressesRef, returnPreferenceRef;
    private ValueEventListener priceDetailsUpdateListener, countListener;
    private RelativeLayout rlReturnBooks;
    private Integer delivery, amountItemsInt, amountTotalInt, savings, totalItemsInt;
    private String bookPath;
    private Date currentTime;
    private ValueEventListener showAndClearListener;
    private DatabaseReference cartItemCount;
    // private boolean isAddressEnabled = false;
    private Toolbar toolbar;
    private String id;
    private String addressId;
    private PriceDetails details;
    private Integer amount;
    private FirebaseDatabase db;
    private RecyclerView rvOldBooks;
    private String combination;
    private TextView tvDesc;
    private Button btnShortDesc;
    private Integer cartNewBooksRequiredCount;
    private TextView tvView;
    private TextView tvRemove;
    private RecyclerView rvBooksSelected,rvYMAL;
    private TextView tvYMAL;
    private RelativeLayout rlReturnBooksSuccess;
    private TextView tvDesc2;
    private Integer daysForDelivery;
    private TextView tvDaysForDelivery;
    private FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<CartItem, CartItemViewHolder> cartItemFRA;
    private FirebaseRecyclerAdapter<ReturnBook, ReturnBookViewHolder> returnBookFRA;
    FirebaseRecyclerAdapter<BookTemplate, TemplateViewHolder> searchReturnFRA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance();


        //set up references
        cartRef = db.getReference("Cart").child(uid);
        cartRequestRef = db.getReference("CartReq").child(uid);
        rootRef = db.getReference();
        wishlistRef = db.getReference("Wishlist").child(uid);
        priceDetailsRef = db.getReference("PriceDetails").child(uid);
        deliveryRef = db.getReference("Delivery");
        cartItemCount = db.getReference("PriceDetails").child(uid).child("count");
        addressesRef = db.getReference("Addresses").child(uid);
        returnPreferenceRef = db.getReference("ReturnPreference").child(uid).child("returnBooks");

        returnPreferenceRef.removeValue();


        noItemInCart = findViewById(R.id.no_item_in_cart);
        noItemText = findViewById(R.id.no_item_text);
        bottom = findViewById(R.id.bottom);
        priceDetails = findViewById(R.id.price_details);
        bookList = findViewById(R.id.recycler_view);
        continueBtn = findViewById(R.id.continue_btn);
        pinView = findViewById(R.id.pin_view);
        rlReturnBooks = findViewById(R.id.rl_return_books);
        tvDesc = findViewById(R.id.tv_desc);
        btnShortDesc = findViewById(R.id.btn_short_desc);
        tvView = findViewById(R.id.tv_view);
        rlReturnBooksSuccess = findViewById(R.id.rl_return_books_success);
        tvDesc2 = findViewById(R.id.tv_desc2);
        tvRemove = findViewById(R.id.tv_remove);
        rvYMAL = findViewById(R.id.rv_ymal);
        tvYMAL = findViewById(R.id.tv_ymal);

        pinView.setPaintFlags(pinView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        DatabaseReference YouMayAlsoLikeMenu = db.getReference("YouMayAlsoLikeMenu").child("SPPU");

        rvYMAL.setHasFixedSize(false);
        rvYMAL.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));

        Query query = YouMayAlsoLikeMenu.orderByChild("priority");

        firebaseSearchYouMayAlsoLike(query);

        pinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeliveryPinAndSetPriceDetails();
            }
        });

        //set up pricePd
        simplePd = new ProgressDialog(CartActivity.this);
        simplePd.setMessage("Loading...");
        simplePd.setCancelable(false);

        loadDefaultDeliveryChargesAndSetPriceDetails();


        db.getReference("Users").child(uid).child("userCourse").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Log.d("CartActivityDebug", dataSnapshot.toString());
                    UserCourse userCourse = dataSnapshot.getValue(UserCourse.class);


                    branchCode[0] = userCourse.getBranchCode();
                    yearCode[0] = userCourse.getYearCode();
                    courseCode[0] = userCourse.getCourseCode();


                    db.getReference("SPPUbooksListing").child("details").child(courseCode[0]).child("currentSem").addListenerForSingleValueEvent(new ValueEventListener() {
                        private int year;

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                semCode[0] = dataSnapshot.getValue(String.class);

                                if (semCode[0]==null)
                                    semCode[0] = "__";

                                if (!yearCode[0].equals("__")) {
                                    year = Integer.parseInt(yearCode[0]) - 1;
                                    yearCode[0] = "0" + year;
                                } else {
                                    yearCode[0] = "__";
                                }


                                combination = branchCode[0] + yearCode[0] + semCode[0];

                                returnPreferenceRef.child("combination").setValue(combination);
                                returnPreferenceRef.child("courseCode").setValue(courseCode[0]);

                                Toast.makeText(CartActivity.this, "c: " + courseCode[0], Toast.LENGTH_SHORT).show();
                                db.getReference("ReturnEnablement").child("SPPU").child(courseCode[0]).child(combination).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child("returnable").exists() && dataSnapshot.child("returnable").getValue(Boolean.class)) {

                                            if (dataSnapshot.child("description").exists() && !dataSnapshot.child("description").getValue(String.class).isEmpty()) {
                                                tvDesc.setText("Get upto ₹ " + dataSnapshot.child("description").getValue(String.class) + " by returning your Last Year Books");
                                                btnShortDesc.setText("GET UPTO ₹ " + dataSnapshot.child("description").getValue(String.class) + " OFF");
                                            } else {
                                                tvDesc.setText("Get offer by returning your Last Year Books");
                                                btnShortDesc.setText("GRAB OLD BOOKS RETURN OFFER");
                                            }


                                            cartNewBooksRequiredCount = dataSnapshot.child("cartNewBooksRequiredCount").getValue(Integer.class);

                                            db.getReference("CartReq").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        rlReturnBooks.setVisibility(View.VISIBLE);
                                                    } else {
                                                        rlReturnBooks.setVisibility(View.GONE);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            btnShortDesc.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    rlReturnBooks.performClick();
                                                }
                                            });
                                            rlReturnBooks.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {


                                                    db.getReference("CartReq").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            int count = 0;
                                                            if (dataSnapshot.exists()) {
                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                    if (ds.child("type").getValue(String.class).equals("books")) {
                                                                        count++;
                                                                    }
                                                                }

                                                                if (cartNewBooksRequiredCount != null) {
                                                                    if (count >= cartNewBooksRequiredCount) {
                                                                        DatabaseReference ref = db.getReference("SPPUbooksTemplates").child(courseCode[0])
                                                                                .child("return").child(combination);

                                                                        showReturnSheet(ref);
                                                                    } else {

                                                                        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
                                                                        alert.setTitle("Please add " + (cartNewBooksRequiredCount - count) + " more books to the cart");
                                                                        alert.setMessage("You can grab this return book offer by adding " + (cartNewBooksRequiredCount - count) + " more books' to the cart.");
                                                                        alert.setCancelable(false);
                                                                        alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        });

                                                                        AlertDialog dialog = alert.create();
                                                                        dialog.show();
                                                                        // Toast.makeText(CartActivity.this, "You must buy at least " + cartNewBooksRequiredCount + " new books to avail this offer :)", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                } else {
                                                                    DatabaseReference ref = db.getReference("SPPUbooksTemplates").child(courseCode[0])
                                                                            .child("return").child(combination);

                                                                    showReturnSheet(ref);
                                                                }


                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });


                                                }
                                            });

                                        } else {
                                            rlReturnBooks.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    rlReturnBooks.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

// this code asks delivery pin again and again from user we have to avoid this thats why code is commented
//        cartRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    getDeliveryPinAndSetPriceDetails();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        tvView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference returnBooksRef = db.getReference("ReturnPreference").child(uid).child("returnBooks").child("books");

                AlertDialog dialog;

                final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);

                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = CartActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.show_return_books_selected_dialog,null);
                alert.setView(customDialog);
                rvBooksSelected = customDialog.findViewById(R.id.rv_books_selected);
                rvBooksSelected.setHasFixedSize(false);

                LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(CartActivity.this);
                rvBooksSelected.setLayoutManager(linearLayoutManager2);

                firebaseSearchReturnShow(returnBooksRef);

                alert.setTitle("Selected Books For Return");
                alert.setIcon(R.drawable.about);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                dialog = alert.create();
                dialog.show();
            }
        });

        tvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlReturnBooks.setVisibility(View.VISIBLE);
                rlReturnBooksSuccess.setVisibility(View.GONE);
                returnPreferenceRef.removeValue();
            }
        });


        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        //start shimmer as soon as activity starts
        mShimmerViewContainer.startShimmerAnimation();

        //set up pricePd
        pricePd = new ProgressDialog(CartActivity.this);
        pricePd.setMessage("Loading Price Details");
        pricePd.setCancelable(false);

        //set up pricePd
        pd = new ProgressDialog(CartActivity.this);
        pd.setMessage("Loading Delivery Details");
        pd.setCancelable(false);


        //set toolbar as actionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        bookList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        //by default pd bottom and suggestion card are invisible.
        priceDetails.setVisibility(View.GONE);
        bottom.setVisibility(View.GONE);


        //show cart items added according to time added
        firebaseSearch(cartRef);


        //when function count (priceDetails) and user defined (cart requests) count are equal then it shows the data
        //(After checking if there is item in cart)
        countListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("priced", cartChildrenCount + " " + dataSnapshot.getValue());

                //display data when pricedetails count and cartreq becomes equals
                if (cartChildrenCount == dataSnapshot.getValue() && cartChildrenCount != 0) {

                    noItemInCart.setVisibility(View.GONE);
                    noItemText.setVisibility(View.GONE);
                    priceDetails.setVisibility(View.VISIBLE);
                    bottom.setVisibility(View.VISIBLE);
                    bookList.setVisibility(View.VISIBLE);

                    rvYMAL.setVisibility(View.VISIBLE);
                    tvYMAL.setVisibility(View.VISIBLE);

                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmerAnimation();
                    pricePd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CartActivity.this, "countlistener" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();

            }
        };

        final ProgressDialog untilItemExistCheck = new ProgressDialog(this);
        untilItemExistCheck.setMessage("Loading...");
        untilItemExistCheck.setCancelable(false);

        untilItemExistCheck.show();

        //to check if there is no item in cart and if there then set listener to show data.
        showAndClearListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                cartChildrenCount = 0L;
                untilItemExistCheck.dismiss();


                if (dataSnapshot.exists()) {


//                    if (dataSnapshot.child("stationary").exists()){
//
//                        for(DataSnapshot stationarySnapshot: dataSnapshot.child("stationary").getChildren()){
//                            cartChildrenCount =cartChildrenCount + stationarySnapshot.child("quantity").getValue(Long.class);
//                        }
//                        //  cartChildrenCount = dataSnapshot.child("books").getChildrenCount() + dataSnapshot.child("stationary").getChildrenCount();
//                    }


                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {
                        //Log.d("data",booksSnapshot.toString()+"\n");
                        cartChildrenCount = cartChildrenCount + booksSnapshot.child("quantity").getValue(Long.class);
                    }

                }


                //if there is no item in cart request
                if (cartChildrenCount == 0) {

                    rvYMAL.setVisibility(View.GONE);
                    tvYMAL.setVisibility(View.GONE);

                    rlReturnBooks.setVisibility(View.GONE);
                    bookList.setVisibility(View.GONE);
                    priceDetails.setVisibility(View.GONE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmerAnimation();
                    noItemInCart.setVisibility(View.VISIBLE);
                    noItemText.setVisibility(View.VISIBLE);
                    noItemInCart.setProgress(0);
                    noItemInCart.playAnimation();
                    Log.d("noItemInCart", "213");
                    pricePd.dismiss();

                }

                //if there are item in cart
                else {
                    cartItemCount.addValueEventListener(countListener);

                    //initially this makes empty card invisible
                    noItemInCart.setVisibility(View.GONE);
                    noItemText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                untilItemExistCheck.dismiss();
                Toast.makeText(CartActivity.this, "show and clear" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };

        cartRequestRef.addValueEventListener(showAndClearListener);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //if phone is not present add phone number
                if (!isPhonePresent()) {
                    Intent addPhoneIntent = new Intent(CartActivity.this, PhoneNumberActivity.class);
                    addPhoneIntent.putExtra("loginFlow", "no");
                    startActivityForResult(addPhoneIntent, 5);
                } else {
                    if (pin.equals("000000")) {
                        // Toast.makeText(CartActivity.this, "Enter Valid Pincode", Toast.LENGTH_SHORT).show();
                        getDeliveryPinAndSetPriceDetails();
                    } else {
                        pd.show();

                        db.getReference("CartReq").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    Boolean proceed = true;

                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.child("outOfStock").exists() && ds.child("outOfStock").getValue(Boolean.class)) {
                                            // do not proceed
                                            proceed = false;
                                        }
                                    }

                                    if (!proceed) {
                                        pd.dismiss();
                                        firebaseSearch(cartRef);
                                        Toast.makeText(CartActivity.this, "Please remove out of stock products from cart to procceed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ///prevent addtion of items from same login from different device to pay less amount
                                        priceDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                                details = dataSnapshot.getValue(PriceDetails.class);
                                                amount = details.getAmountDiscounted();

                                                pd.dismiss();
                                                Integer total = charges + amount;
                                                Intent paymentIntent = new Intent(CartActivity.this, PayActivity.class);
                                                paymentIntent.putExtra("amount", total.toString());
                                                paymentIntent.putExtra("pin", pin);
                                                paymentIntent.putExtra("charges", charges);
                                                startActivityForResult(paymentIntent, 4);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                pd.dismiss();
                                            }
                                        });
                                    }
                                } else {
                                    pd.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                pd.dismiss();
                            }
                        });

                    }

                }
            }
        });
    }

    private void showReturnSheet(final DatabaseReference oldBooksRef) {

        Typeface face = Typeface.createFromAsset(getAssets(), "newfont.ttf");


        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                CartActivity.this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_return_books_sheet,
                        (NestedScrollView) findViewById(R.id.bottom_sheet_container)
                );

        rvOldBooks = bottomSheetView.findViewById(R.id.recycler_view);
        rvOldBooks.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvOldBooks.setLayoutManager(linearLayoutManager);


        Button btnCalculate = bottomSheetView.findViewById(R.id.btn_calculate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnCalculate.setStateListAnimator(null);
        }

        btnCalculate.setTypeface(face);

        btnCalculate.setOnClickListener(v -> {

            final ProgressDialog pdLoading = new ProgressDialog(CartActivity.this);
            pdLoading.setMessage("Loading...");
            pdLoading.setCancelable(false);
            pdLoading.show();


            db.getReference("ReturnEnablement").child("SPPU").child(courseCode[0]).child(combination).child("required").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    pdLoading.dismiss();

                    if (dataSnapshot.exists()) {
                        int required = dataSnapshot.getValue(Integer.class);
                        int count = 0;
                        int totalPrice = 0;

                        for (int i = 0; i < 20; i++) {
                            if (selected[i]) {
                                count++;
                            }

                            totalPrice = price[i] + totalPrice;
                        }

                        if (count < required) {
                            returnPreferenceRef.child("requiredCountSatisfied").setValue(false);

                            final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
                            alert.setTitle("Please select " + (required - count) + " more books' publication from the list");
                            alert.setMessage("You can grab this return book offer by selecting " + (required - count) + " more books' publication.");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Continue", (dialog, which) -> dialog.dismiss());

                            AlertDialog dialog = alert.create();
                            dialog.show();

                            //Toast.makeText(CartActivity.this, "Please select at least " + required + " books publication to continue", Toast.LENGTH_SHORT).show();
                        } else {

                            final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
                            alert.setTitle("Congratulations!!  You have got ₹ " + totalPrice + " Off for your current order");
                            alert.setMessage("Please make sure you are able to return your specified old books at the time when new books will be delivered.");
                            alert.setCancelable(false);
                            final int finalTotalPrice = totalPrice;
                            alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    returnPreferenceRef.child("requiredCountSatisfied").setValue(true);
                                    returnPreferenceRef.child("returnPrice").setValue(Integer.valueOf(finalTotalPrice)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                showPriceDetails();
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                    bottomSheetDialog.dismiss();
                                    rlReturnBooks.setVisibility(View.GONE);

                                    tvDesc2.setText("Congratulations you have got ₹ "+ finalTotalPrice +" Off");
                                    rlReturnBooksSuccess.setVisibility(View.VISIBLE);

                                }
                            });

                            alert.setNegativeButton("Cancel", (dialog, which) -> {
                                rlReturnBooks.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            });

                            AlertDialog dialog = alert.create();
                            dialog.show();
                            //Toast.makeText(CartActivity.this, "Hurrey you are eligible to get " + totalPrice + " Rupees", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    pdLoading.dismiss();
                }
            });

        });
//        pincode.setTypeface(face);

//        oldDeliveryPd = new ProgressDialog(this);
//        oldDeliveryPd.setMessage("Loading");
//        oldDeliveryPd.setCancelable(false);
//
//        oldDeliveryPd.show();
        firebaseSearchReturn(oldBooksRef);


//        final Button enter = bottomSheetView.findViewById(R.id.enter);
//        final EditText pincode = bottomSheetView.findViewById(R.id.pincode);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            enter.setStateListAnimator(null);
//        }
//
//        enter.setTypeface(face);
//        pincode.setTypeface(face);
//
//        enter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                if (pincode.getText().length() == 6){
//
//                    simplePd.show();
//
//                    DatabaseReference deliveryRef = db.getReference("Delivery").child("pin").child(pincode.getText().toString());
//
//                    deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.child("deliveryCharges").exists()){
//                                simplePd.dismiss();
//                                charges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
//                                pin = pincode.getText().toString();
//                                showPriceDetails();
//                                bottomSheetDialog.dismiss();
//                            }
//                            else{
//                                pinView = findViewById(R.id.pin_view);
//                                pinView.setText(pincode.getText().toString());
//                                amountDelivery = findViewById(R.id.amount_delivery);
//                                amountTotal = findViewById(R.id.amount_total);
//                                amountDelivery.setText("--");
//                                amountTotal.setText("--");
//                                pin = "000000";
//                                //bottomSheetDialog.dismiss();
//                                simplePd.dismiss();
//                                Toast.makeText(CartActivity.this,"Not deliverable. Try different location...",Toast.LENGTH_SHORT).show();
//
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            bottomSheetDialog.dismiss();
//                            simplePd.dismiss();
//                            Toast.makeText(CartActivity.this,"Delivery fees access not allowed",Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//                }else{
//                    Toast.makeText(CartActivity.this,"Please enter 6 digit pin code",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        //clicklisteners

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
//        oldDeliveryPd.show();
//
//        addressesRef.orderByChild("timeAdded").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                if (dataSnapshot.exists()){
//                    Address address = new Address();
//                    for (DataSnapshot ds : dataSnapshot.getChildren()){
//                        address = ds.getValue(Address.class);
//                    }
//
//                    pincode.setText(address.getPincode().toString());
//                    enter.setText("CONTINUE");
//                    oldDeliveryPd.dismiss();
//                }
//                else {
//                    enter.setText("ENTER");
//                    oldDeliveryPd.dismiss();
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                oldDeliveryPd.dismiss();
//            }
//        });
    }

    private void firebaseSearchReturn(DatabaseReference ref) {


        selected = new boolean[20];
        price = new int[20];

        FirebaseRecyclerOptions<BookTemplate> options = new FirebaseRecyclerOptions.Builder<BookTemplate>()
                .setQuery(ref,BookTemplate.class)
                .build();

        searchReturnFRA
                = new FirebaseRecyclerAdapter<BookTemplate, TemplateViewHolder>(options) {

            @NonNull
            @Override
            public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.template_row,parent,false);

                return new TemplateViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TemplateViewHolder viewHolder, int position1, @NonNull BookTemplate model) {
                viewHolder.setDetails(model.getName());

                ////////////////////////////////////////////////////////////////////////////////////
                final ArrayList<Item> publicationDataList = new ArrayList<Item>();

                //these will be used to show the data in spinners which is obtained by above lists
                final ArrayList<String> publicationDisplayList = new ArrayList<String>();

                DatabaseReference codeRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing").child("codes").child(courseCode[0]);


                publicationDisplayList.add("--Select--");
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
                                final String publicationSelected = viewHolder.spPublication.getSelectedItem().toString();

                                if (viewHolder.spPublication.getSelectedItemPosition() != 0) {

                                    for (int i = 0; i < publicationDataList.size(); i++) {

                                        if (publicationDataList.get(i).getName().equals(publicationSelected)) {

                                            final String oldBookId = model.getTemplateId() + publicationDataList.get(i).getCode() + "return";

                                            db.getReference("SPPUbooks").child(courseCode[0]).child("return")
                                                    .child(combination).child(oldBookId).child("discountedPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        viewHolder.tvSubject.setTextColor(getResources().getColor(R.color.black));
                                                        viewHolder.tvSubject.setTypeface(viewHolder.tvSubject.getTypeface(), Typeface.ITALIC);

                                                        selected[position1] = true;
                                                        price[position1] = dataSnapshot.getValue(Integer.class);

                                                        Map<String, Object> map = new HashMap<>();

                                                        map.put("discountedPrice", price[position1]);
                                                        map.put("bookId", oldBookId);
                                                        map.put("templateId", model.getTemplateId());
                                                        map.put("publication", publicationSelected);

                                                        returnPreferenceRef.child("books").child(model.getTemplateId()).setValue(map);
                                                    } else {
                                                        viewHolder.tvSubject.setTextColor(getResources().getColor(R.color.default_text));
                                                        viewHolder.tvSubject.setTypeface(viewHolder.tvSubject.getTypeface(),Typeface.NORMAL);

                                                        selected[position1] = false;
                                                        price[position1] = 0;
                                                        returnPreferenceRef.child("books").child(model.getTemplateId()).removeValue();
                                                        viewHolder.spPublication.setSelection(0);
                                                        viewHolder.spPublication.performClick();
                                                        Toast.makeText(CartActivity.this, "Selected publication is not returnable for this book", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            break;
                                        }
                                    }

                                } else {
                                    viewHolder.tvSubject.setTextColor(getResources().getColor(R.color.default_text));
                                    viewHolder.tvSubject.setTypeface(viewHolder.tvSubject.getTypeface(),Typeface.NORMAL);

                                    selected[position1] = false;
                                    price[position1] = 0;
                                    returnPreferenceRef.child("books").child(model.getTemplateId()).removeValue();
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };


        rvOldBooks.setAdapter(searchReturnFRA);
        searchReturnFRA.startListening();
    }

    private void firebaseSearchReturnShow(DatabaseReference ref) {

        FirebaseRecyclerOptions<ReturnBook> options = new FirebaseRecyclerOptions.Builder<ReturnBook>()
                .setQuery(ref,ReturnBook.class)
                .build();

        returnBookFRA
                = new FirebaseRecyclerAdapter<ReturnBook, ReturnBookViewHolder>(options) {

            @NonNull
            @Override
            public ReturnBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.return_book_row,parent,false);

                return new ReturnBookViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReturnBookViewHolder viewHolder, int position, @NonNull ReturnBook model) {
                viewHolder.setDetails(model.getTemplateId(),model.getPublication());
            }
        };


        rvBooksSelected.setAdapter(returnBookFRA);
        returnBookFRA.startListening();
    }

    private void getDeliveryPinAndSetPriceDetails() {

        oldDeliveryPd = new ProgressDialog(this);
        oldDeliveryPd.setMessage("Loading");
        oldDeliveryPd.setCancelable(false);


        Typeface face = Typeface.createFromAsset(getAssets(), "newfont.ttf");


        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                CartActivity.this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet,
                        (ScrollView) findViewById(R.id.bottom_sheet_container)
                );

        final Button enter = bottomSheetView.findViewById(R.id.enter);
        final EditText pincode = bottomSheetView.findViewById(R.id.pincode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter.setStateListAnimator(null);
        }

        enter.setTypeface(face);
        pincode.setTypeface(face);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (pincode.getText().length() == 6) {

                    simplePd.show();

                    DatabaseReference deliveryRef = db.getReference("Delivery").child("pin").child(pincode.getText().toString());

                    deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("deliveryCharges").exists()) {
                                simplePd.dismiss();
                                charges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
                                pin = pincode.getText().toString();
                                daysForDelivery = dataSnapshot.child("daysForDelivery").getValue(Integer.class);
                                showPriceDetails();
                                bottomSheetDialog.dismiss();
                            } else {
                                pinView = findViewById(R.id.pin_view);
                                pinView.setText(pincode.getText().toString());
                                amountDelivery = findViewById(R.id.amount_delivery);
                                amountTotal = findViewById(R.id.amount_total);
                                amountDelivery.setText("--");
                                amountTotal.setText("--");
                                pin = "000000";
                                //bottomSheetDialog.dismiss();
                                simplePd.dismiss();
                                Toast.makeText(CartActivity.this, "Not deliverable. Try different location...", Toast.LENGTH_SHORT).show();


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            bottomSheetDialog.dismiss();
                            simplePd.dismiss();
                            Toast.makeText(CartActivity.this, "Delivery fees access not allowed", Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    Toast.makeText(CartActivity.this, "Please enter 6 digit pin code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //clicklisteners

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        oldDeliveryPd.show();

        addressesRef.orderByChild("timeAdded").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Address address = new Address();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        address = ds.getValue(Address.class);
                    }

                    pincode.setText(address.getPincode());
                    enter.setText("CONTINUE");
                    oldDeliveryPd.dismiss();
                } else {
                    enter.setText("ENTER");
                    oldDeliveryPd.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                oldDeliveryPd.dismiss();
            }
        });
    }

    private void loadDefaultDeliveryChargesAndSetPriceDetails() {

        Log.d("asdebug", "load default called");

        DatabaseReference defaultDelivery = db.getReference("Delivery").child("defaultPin");

        simplePd.show();

        defaultDelivery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    simplePd.dismiss();
                    charges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
                    pin = dataSnapshot.child("pin").getValue(String.class);
                    daysForDelivery = dataSnapshot.child("daysForDelivery").getValue(Integer.class);
                    showPriceDetails();
                } else {
                    simplePd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                simplePd.dismiss();
                pin = "000000";
                Toast.makeText(CartActivity.this, "Delivery fees access not allowed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPhonePresent() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        boolean isPhonePresent = false;

        for (UserInfo profile : firebaseUser.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if (providerId.equals("phone")) {
                isPhonePresent = true;
                break;
            }
        }

        return isPhonePresent;
    }

    private boolean isAnonymous() {

        int count = 0;

        FirebaseUser user = mAuth.getCurrentUser();


        for (UserInfo profile : user.getProviderData()) {
            count++;
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();
            // UID specific to the provider
            String uid = profile.getUid();

            // Name, email address, and profile photo Url
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            Log.d("userdata", "Provider id:" + providerId + " uid:" + uid + "name :" + name + " email:" + email + " phone" + phone);
        }

        return count == 1;

    }


    private void showPriceDetails() {

        totalItems = findViewById(R.id.total_items);
        amountTotal = findViewById(R.id.amount_total);
        amountDelivery = findViewById(R.id.amount_delivery);
        amountItems = findViewById(R.id.amount_items);
        amountSavings = findViewById(R.id.amount_savings);
        payableAmount = findViewById(R.id.payable_amount);
        tvDaysForDelivery = findViewById(R.id.tv_day_for_delivery);

        pinView.setText(pin);

        if (daysForDelivery==1){
            tvDaysForDelivery.setText("(Delivery in "+daysForDelivery+" Day)");
        }
        else{
            tvDaysForDelivery.setText("(Delivery in "+daysForDelivery+" Days)");
        }

        priceDetailsUpdateListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("showing", "update price detail event listener called");

                //show progress dialogue as soon as there is change in price

                //1 count for total items in cart
                details = dataSnapshot.getValue(PriceDetails.class);


                // if count is zero then hide pricedetails bottom and show noItemInCart Picture
                if (details.getCount() == 0) {
                    priceDetails.setVisibility(View.GONE);
                    bottom.setVisibility(View.GONE);
                    //this was showing empty if not empty while loading
                    //noItemInCart.setVisibility(View.VISIBLE);
                    //Log.d("noItemInCart","480");
                }

                //set total items
                totalItems.setText("Price (" + details.getCount() + " items)");

                //2
                //set total amount
                //amountItemsInt = dataSnapshot.child("amountDiscounted").getValue(Integer.class);
                amountItems.setText("₹ " + details.getAmountDiscounted());
                amount = details.getAmountDiscounted();

                //3
                //set total savings
                //savings = dataSnapshot.child("amountOriginal").getValue(Integer.class) - amountItemsInt;
                savings = details.getAmountOriginal() - details.getAmountDiscounted();
                amountSavings.setText("You will save ₹ " + savings + " on this order");

                amountDelivery.setText(charges.toString());

                returnPreferenceRef.child("returnPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            int returnPrice = dataSnapshot.getValue(Integer.class);
                            //5
                            //set total with added delivery
                            amountTotalInt = charges + details.getAmountDiscounted() - returnPrice;
                        }
                        else{

                            //5
                            //set total with added delivery
                            amountTotalInt = charges + details.getAmountDiscounted();
                        }

                        amountTotal.setText("₹ " + amountTotalInt.toString());

                        //6
                        //set amount to bottom
                        payableAmount.setText("₹ " + amountTotalInt.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CartActivity.this, "priceDetailsUpdateListener" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        };


        priceDetailsRef.addValueEventListener(priceDetailsUpdateListener);


    }

    private void firebaseSearch(DatabaseReference q) {

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        FirebaseRecyclerOptions<CartItem> options = new FirebaseRecyclerOptions.Builder<CartItem>()
                .setQuery(q,CartItem.class)
                .build();

        cartItemFRA = new FirebaseRecyclerAdapter<CartItem, CartItemViewHolder>(options) {

            @NonNull
            @Override
            public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cart_item_row,parent,false);

                return new CartItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CartItemViewHolder viewHolder, int position, @NonNull CartItem model) {
                if (model.getType() != null && model.getType().equals("books")) {
                    final String bookId = model.getItemId();
                    final String bookLocation = model.getItemLocation();


                    //add listener to obtained path of book ,to set the data of book
                    rootRef.child(bookLocation).child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {

                                Book book;
                                book = dataSnapshot.getValue(Book.class);
                                model.setItemName(book.getName());
                                model.setItemAuthor(book.getAuthor());
                                model.setItemPublication(book.getPublication());
                                model.setItemImg(book.getImg());
                                model.setItemOriginalPrice(book.getOriginalPrice());
                                model.setItemDiscountedPrice(book.getDiscountedPrice());
                                model.setItemDiscount(book.getDiscount());


                                Log.d("showing", "capture " + dataSnapshot.toString());

                                viewHolder.setDetails(model.getItemName(), model.getItemPublication(), model.getItemImg(),
                                        model.getItemOriginalPrice(), model.getItemDiscountedPrice(), model.getItemDiscount(), model.getQuantity(), "books"
                                        , book.getCount());

                                if (model.getQuantity() > book.getCount()) {
                                    Toast.makeText(CartActivity.this, "it is reaching", Toast.LENGTH_SHORT).show();
                                    rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(bookId).child("outOfStock").setValue(true);
                                } else {
                                    rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(bookId).child("outOfStock").removeValue();
                                }

                            } else {
                                rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(bookId).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(CartActivity.this, "rootRef" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });


                    //remove button listener
                    viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();
                            //pricePd is dismissed when there is update to price of data by cloud function
                            String id = model.getItemId();
                            bookList.setVisibility(View.GONE);
                            priceDetails.setVisibility(View.GONE);
                            bottom.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.VISIBLE);

                            ///
                            returnPreferenceRef.removeValue();
                            rlReturnBooksSuccess.setVisibility(View.GONE);
                            rlReturnBooks.setVisibility(View.VISIBLE);

                            cartRequestRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    //wishlist button listener
                    viewHolder.wishlist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();
                            //pricePd is dismissed when there is update to price of data by cloud function
                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);

                            final String id = model.getItemId();
                            cartRequestRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });


                            ItemMeta itemMeta = new ItemMeta(id, model.getItemLocation(), Long.parseLong(date), model.getType());
                            wishlistRef.child(id).setValue(itemMeta);

                        }
                    });


                }
                else {
                    final String stationaryId = model.getItemId();
                    final String stationaryLocation = model.getItemLocation();

                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    uid = mAuth.getCurrentUser().getUid();

                    ValueEventListener quantityListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            viewHolder.quantity.setText(dataSnapshot.child("quantity").getValue(Integer.class).toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };

                    rootRef.child("Cart").child(uid).child(stationaryId).addListenerForSingleValueEvent(quantityListener);

                    //add listener to obtained path of book ,to set the data of book
                    rootRef.child(stationaryLocation).child(stationaryId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            StationaryItem stationaryItem;
                            stationaryItem = dataSnapshot.getValue(StationaryItem.class);
                            model.setItemName(stationaryItem.getName());
                            model.setItemPublication(stationaryItem.getAtr1());

                            if (stationaryItem.getAtr2() != null && !stationaryItem.getAtr2().equals(""))
                                model.setItemAuthor(stationaryItem.getAtr2());


                            model.setItemImg(stationaryItem.getImg());
                            model.setItemOriginalPrice(stationaryItem.getOriginalPrice());
                            model.setItemDiscountedPrice(stationaryItem.getDiscountedPrice());
                            model.setItemDiscount(stationaryItem.getDiscount());

                            Log.d("showing", "capture " + dataSnapshot.toString());

                            viewHolder.setDetails(model.getItemName(), model.getItemPublication(), model.getItemImg(),
                                    model.getItemOriginalPrice(), model.getItemDiscountedPrice(), model.getItemDiscount(),
                                    model.getQuantity(), "stationary", stationaryItem.getCount());

                            if (model.getQuantity() > stationaryItem.getCount()) {
                                rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(stationaryId).child("outOfStock").setValue(true);
                            } else {
                                rootRef.child("CartReq").child(mAuth.getCurrentUser().getUid()).child(stationaryId).child("outOfStock").removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(CartActivity.this, "rootRef" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });


                    // if (!isAddressEnabled){

                    //remove button listener
                    viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();
                            //pricePd is dismissed when there is update to price of data by cloud function
                            String id = model.getItemId();
                            String[] locs = model.getItemLocation().split("/");
                            String stationaryType = locs[locs.length - 1];

                            bookList.setVisibility(View.GONE);
                            priceDetails.setVisibility(View.GONE);
                            bottom.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.VISIBLE);

                            DatabaseReference countDataRef = db.getReference("CountData")
                                    .child(uid).child("stationary").child(stationaryType).child(id);
                            countDataRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });

                            cartRequestRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    //wishlist button listener
                    viewHolder.wishlist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();

                            String id = model.getItemId();
                            String[] locs = model.getItemLocation().split("/");
                            String type = locs[locs.length - 1];

                            bookList.setVisibility(View.GONE);
                            priceDetails.setVisibility(View.GONE);
                            bottom.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.VISIBLE);

                            DatabaseReference countDataRef = db.getReference("CountData")
                                    .child(uid).child("stationary").child(type).child(id);

                            countDataRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });

                            cartRequestRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });

                            //pricePd is dismissed when there is update to price of data by cloud function
                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);

                            ItemMeta itemMeta = new ItemMeta(id, model.getItemLocation(), Long.parseLong(date),model.getType());
                            wishlistRef.child(id).setValue(itemMeta);

                        }
                    });


                    viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (model.getQuantity() < 101) {
                                pricePd.show();
                                final Integer qUpdate = model.getQuantity() + 1;
                                viewHolder.quantity.setText(qUpdate.toString());

                                cartRequestRef.child(model.getItemId()).child("quantity").setValue(qUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            pricePd.dismiss();
                                        }

                                    }
                                });
                            } else {
                                Toast.makeText(CartActivity.this, "Quantity can't be greater than 100", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (model.getQuantity() > 1) {
                                pricePd.show();
                                final Integer qUpdate = model.getQuantity() - 1;
                                viewHolder.quantity.setText(qUpdate.toString());

                                cartRequestRef.child(model.getItemId()).child("quantity").setValue(qUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(CartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            pricePd.dismiss();
                                        }

                                    }
                                });
                            } else {
                                Toast.makeText(CartActivity.this, "Quantity can't be less than 1", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        };


        bookList.setAdapter(cartItemFRA);
        cartItemFRA.startListening();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //after adding address if not present just after cart activity
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //showShortList();
                setAddress();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(CartActivity.this, "failed to add address", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(CartActivity.this, CartActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
        //after updating address on delivery page by clicking edit button
        else if (requestCode == 2) {

            if (resultCode == RESULT_OK) {
                Toast.makeText(CartActivity.this, "address updated", Toast.LENGTH_SHORT).show();

                //showShortList();
                setAddress();
            }

        }
        //returning back after clicking use another address on delivery page
        else if (requestCode == 3) {
            Log.d("resultIntent", "got requestcode 3");
            if (resultCode == RESULT_OK) {
                Log.d("resultIntent", "got result ok");
                addressId = data.getStringExtra("addressId");
                setAddress();
                Toast.makeText(CartActivity.this, "address changed successfully", Toast.LENGTH_SHORT).show();
            }
        }
        //for showing orderdetails after successful order request.
        else if (requestCode == 4) {
            Log.d("resultIntent", "got requestcode 4");
            if (resultCode == RESULT_OK) {

                if (data.hasExtra("orderId")) {

                    Intent orderIntent = new Intent(CartActivity.this, OrderDetailsActivity.class);

                    String orderId = data.getStringExtra("orderId");
                    String source = data.getStringExtra("source");

                    orderIntent.putExtra("orderId", orderId);
                    orderIntent.putExtra("source", source);

                    startActivity(orderIntent);
                    setResult(RESULT_OK);
                    finish();
                } else {


//                    Intent orderIntent = new Intent(CartActivity.this, MyOrdersActivity.class);
//                    startActivity(orderIntent);
//                    finish();
                    //removed todo show order list
                }

            } else {
                firebaseSearch(cartRef);

                Log.d("returnedintent", "result is not ok");
                if (data != null) {
                    Log.d("returnedintent", "data exists");

                    String payPin = data.getStringExtra("pin");

                    if (pin != null && !payPin.equals(pin)) {
                        Log.d("returnedintent", "pin not equal");
                        pin = payPin;
                        calculateDeliveryAndSetPriceDetails();
                    }
                } else {
                    Log.d("returnedintent", "data doesnt exists");

                }

            }
        }
        //after adding phone number from cart page allow to go to delivery page
        else if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                continueBtn.performClick();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(CartActivity.this, "Please verify your phone number to continue!!", Toast.LENGTH_LONG).show();

            }
        } else if (requestCode == 6) {
            if (resultCode == RESULT_OK) {
                continueBtn.performClick();
            }
        }
    }


    private void calculateDeliveryAndSetPriceDetails() {
        if (pin.length() == 6) {

            pd.show();

            DatabaseReference deliveryRef = db.getReference("Delivery").child("pin").child(pin);

            deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("deliveryCharges").exists()) {
                        pd.dismiss();
                        charges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
                        daysForDelivery = dataSnapshot.child("daysForDelivery").getValue(Integer.class);
                        showPriceDetails();
                    } else {
                        pd.dismiss();
                        pin = "000000";
                        Toast.makeText(CartActivity.this, "Not deliverable", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    pd.dismiss();
                    Toast.makeText(CartActivity.this, "Delivery fees access not allowed", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(CartActivity.this, "Please enter 6 digit pin code", Toast.LENGTH_SHORT).show();
        }

    }


    private void setAddress() {
        Log.d("resultIntent", "setaddress");
        addressesRef.orderByChild("timeAdded").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Address addressObj = new Address();

                if (changeAddress) {

                    addressObj = dataSnapshot.child(addressId).getValue(Address.class);
                } else {
                    for (DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {

                        addressObj = addressSnapshot.getValue(Address.class);
                    }
                }


                TextView name, number, type, address;
                name = findViewById(R.id.name);
                number = findViewById(R.id.number);
                type = findViewById(R.id.type);
                address = findViewById(R.id.address);

                name.setText(addressObj.getName());
                number.setText(addPhone(addressObj.getNumber()));
                type.setText(addressObj.getType());

                id = addressObj.getId();

                String combinedAddress = addressObj.getBuildingNameNumber() + " " +
                        addressObj.getAreaRoad() + " " +
                        addressObj.getCity() + " " +
                        addressObj.getState() + "-" +
                        addressObj.getPincode();

                address.setText(combinedAddress);

                bookList.setVisibility(View.VISIBLE);
                priceDetails.setVisibility(View.VISIBLE);
                bottom.setVisibility(View.VISIBLE);
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private String addPhone(String numberFromDatabase) {

        FirebaseUser user = mAuth.getCurrentUser();

        Boolean found = false;
        String num = "";

        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if (providerId.equals("firebase")) {

                String phone = profile.getPhoneNumber();
                if (phone != null && phone.length() == 13) {
                    found = true;
                    phone = phone.substring(3);
                    num = phone;
                }
                break;
            }

        }

        if (!found) {
            if (numberFromDatabase != null && numberFromDatabase.length() == 10) {
                num = numberFromDatabase;
            }
        }

        return num;
    }


    public void firebaseSearchYouMayAlsoLike(Query q) {

        FirebaseRecyclerOptions<MenuItem1> options = new FirebaseRecyclerOptions.Builder<MenuItem1>()
                .setQuery(q,MenuItem1.class)
                .build();

         firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder>(options) {

            @NonNull
            @Override
            public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ymal_row,parent,false);

                return new MenuItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuItemViewHolder viewHolder, int position, @NonNull MenuItem1 model) {
                Log.d("debugLoad", "populateViewHolder called for " + model.getId());
                viewHolder.mView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_transition_animation));


                viewHolder.setDetailsYMAL(model.getTop(), model.getImg(), model.getTitle(),model.getType());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(getApplicationContext(), model.getTitle(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getType().equals("books")) {
                            Intent bookIntent = new Intent(CartActivity.this, BookListActivity.class);
                            bookIntent.putExtra("course", model.getCourse());
                            bookIntent.putExtra("category", model.getCategory());
                            bookIntent.putExtra("sem", model.getSem());
                            startActivity(bookIntent);
                        } else if (model.getType().equals("stationary")) {
                            Intent stationaryIntent = new Intent(CartActivity.this, StationaryItemsActivity.class);
                            stationaryIntent.putExtra("stationaryId", model.getCategory());
                            stationaryIntent.putExtra("categoryName", model.getTitle());
                            stationaryIntent.putExtra("source","cart");
                            startActivity(stationaryIntent);
                        } else if (model.getType().equals("more")){
                            Intent selectAddressIntent = new Intent(CartActivity.this, StationaryActivity.class);
                            startActivity(selectAddressIntent);
                        }
                    }
                });
            }
        };


        rvYMAL.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    protected void onDestroy() {

        if (priceDetailsUpdateListener != null)
            priceDetailsRef.removeEventListener(priceDetailsUpdateListener);

        if (showAndClearListener != null)
            cartRequestRef.removeEventListener(showAndClearListener);

        if (countListener != null)
            cartItemCount.removeEventListener(countListener);

        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();

        if (cartItemFRA!=null)
            cartItemFRA.stopListening();

        if (returnBookFRA!=null)
            returnBookFRA.stopListening();

        if (searchReturnFRA!=null)
            searchReturnFRA.stopListening();

        super.onDestroy();
    }

}
