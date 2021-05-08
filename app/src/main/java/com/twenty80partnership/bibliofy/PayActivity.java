package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.adapters.CustomChooserAdapter;
import com.twenty80partnership.bibliofy.events.ListEvent;
import com.twenty80partnership.bibliofy.models.Address;
import com.twenty80partnership.bibliofy.models.EvalInstalledAppInfo;
import com.twenty80partnership.bibliofy.models.Order;
import com.twenty80partnership.bibliofy.models.OrderRequest;
import com.twenty80partnership.bibliofy.models.PriceDetails;
import com.twenty80partnership.bibliofy.models.UpiAddress;
import com.twenty80partnership.bibliofy.models.UpiTransaction;
import com.twenty80partnership.bibliofy.utils.ActionSheet;
import com.twenty80partnership.bibliofy.utils.ApkInfoUtil;
import com.twenty80partnership.bibliofy.utils.CommonUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.graphics.Color.WHITE;
import static android.graphics.Color.pack;

public class PayActivity extends AppCompatActivity implements ActionSheet.MenuItemClickListener{

    final int UPI_PAYMENT = 0;
    RadioGroup radioGroup;
    RadioButton radioButton;
    TextView placeOrder;
    boolean notDeliverable = false;
    FirebaseAuth mAuth;
    DatabaseReference totalAmountRef,payTargetRef;
    private ProgressDialog pd,addressPd;
    private Intent rIntent;
    private Query q;
    private ActionSheet actionSheet;
    private Context mContext;
    private String appName;
    private ArrayList<UpiAddress> targetUpiList;
    private int count;
    private String approvalRefNo;
    private String responseCode;
    private String status;
    private String tempOrderId;
    private String paymentCancel;
    private ValueEventListener amountPaybleLoader;
    private TextView name,number,type, addressView;
    private Button userAnotherAddress;
    private TextView totalItems, amountItems, amountDelivery, amountTotal, amountSavings, payableAmount,continueBtn,noItemText;
    private String pin;
    private ValueEventListener priceDetailsUpdateListener;
    private LinearLayout priceDetailsView;
    private int savings;
    private Integer charges;
    private Integer amountTotalInt;
    int amount;
    private DatabaseReference priceDetailsRef;
    private String id;
    private TextView pinView;
    private RelativeLayout addressDetailsLayout;
    private ImageView edit;
    private Address address = new Address();
    private PriceDetails priceDetails;
    private FirebaseDatabase db;
    private String uid;
    private TextView tvDaysForDelivery;
    private int daysForDelivery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Log.d("debugOrder","acrt");

        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();

        address.setId("");

        radioGroup = findViewById(R.id.radio_group);
        placeOrder = findViewById(R.id.place_order);
        payableAmount = findViewById(R.id.payable_amount);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        type = findViewById(R.id.type);
        userAnotherAddress = findViewById(R.id.use_another_address);
        addressView = findViewById(R.id.address);
        priceDetailsView = findViewById(R.id.price_details);
        addressDetailsLayout = findViewById(R.id.address_details);
        edit = findViewById(R.id.edit);




        //get data from cart activity
        Intent intent = getIntent();

        amount = intent.getIntExtra("amount",0);
        pin = intent.getStringExtra("pin");
        charges = intent.getIntExtra("charges",0);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mContext = this.getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(PayActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Loading...");

        addressPd = new ProgressDialog(PayActivity.this);
        addressPd.setCancelable(false);
        addressPd.setMessage("Loading Address...");


        priceDetailsRef = FirebaseDatabase.getInstance().getReference("PriceDetails").child(mAuth.getCurrentUser().getUid());

        getAddress();

        totalAmountRef = FirebaseDatabase.getInstance().getReference("PriceDetails").child(mAuth.getCurrentUser().getUid()).child("amountDiscounted");

        targetUpiList = new ArrayList<UpiAddress>();
        count = 0;


        payTargetRef = FirebaseDatabase.getInstance().getReference("PayTarget");

        payTargetRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UpiAddress upiAddres;
                    for(DataSnapshot d:dataSnapshot.getChildren()){
                        upiAddres = d.getValue(UpiAddress.class);
                        targetUpiList.add(upiAddres);
                    }

                    pd.dismiss();
                    pd.setMessage("Hold On... Placing Your Order...");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pd.dismiss();
                pd.setMessage("Hold On... Placing Your Order...");
                Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


        amountPaybleLoader = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null){
                    final Integer amount =  dataSnapshot.getValue(Integer.class);
                    DatabaseReference deliveryRef = FirebaseDatabase.getInstance().getReference("Delivery");

                    deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            pd.dismiss();
                          //  Integer d = dataSnapshot.child("basic").child("rate").getValue(Integer.class);
                          //  Integer total = d + amount;
                           // payableAmount.setText(total.toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

        totalAmountRef.addValueEventListener(amountPaybleLoader);

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(notDeliverable){

                    final AlertDialog.Builder alert = new AlertDialog.Builder(PayActivity.this);
                    alert.setTitle("Address not deliverable");
                    alert.setMessage("Please try different pincode to get your order delivered");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();



//                    Intent selectAddressIntent = new Intent(PayActivity.this,AddressesActivity.class);
//                    selectAddressIntent.putExtra("mode","select");
//                    selectAddressIntent.putExtra("defaultAddressId",address.getId());
//                    startActivityForResult(selectAddressIntent,4);
                    return;
                }

                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);
                if (selectedId == -1){
                    Toast.makeText(getApplicationContext(),"Please select payment method",Toast.LENGTH_SHORT).show();
                }
                else {
                    switch (selectedId){
                        case R.id.cod:
                            makeOrderRequestCOD(address.getId(),"cod");
                            Log.d("debugOrder","makeOrderCalled");
                            break;
                        case R.id.upi:
                            Log.d("chargesDebug",String.valueOf(priceDetails.getAmountDiscounted()+charges));






                            if(count<targetUpiList.size()){
                                //Toast.makeText(getApplicationContext(),targetUpiList.get(count).getAddress(),Toast.LENGTH_SHORT).show();
                                String amtWithDel = String.valueOf(priceDetails.getAmountDiscounted()+charges);
                                Log.d("chargesDebug",amtWithDel);

                                payUsingUpi(amtWithDel,targetUpiList.get(count).getAddress(),"Bibliofy Order","Bibliofy Order");
                            }
                            else {
                                //Toast.makeText(getApplicationContext(),targetUpiList.get(0).getAddress(),Toast.LENGTH_SHORT).show();
                                    count = 0;
                                String amtWithDel = String.valueOf(priceDetails.getAmountDiscounted()+charges);
                                Log.d("chargesDebug",amtWithDel);


                                payUsingUpi(amtWithDel,targetUpiList.get(count).getAddress(),"Bibliofy Order","Bibliofy Order");

                            }

                           // Toast.makeText(getApplicationContext(),"Google Pay / PhonePe / BHIM UPI / Paytm / Any UPI app",Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent editIntent = new Intent(PayActivity.this,AddAddressActivity.class);
                editIntent.putExtra("id",id);
                startActivityForResult(editIntent,4);
            }
        });

        userAnotherAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent selectAddressIntent = new Intent(PayActivity.this,AddressesActivity.class);
                selectAddressIntent.putExtra("mode","select");
                selectAddressIntent.putExtra("defaultAddressId",address.getId());
                startActivityForResult(selectAddressIntent,4);

            }
        });




    }
    private void showPriceDetails() {

        pd.show();
        Log.d("payactivitydebug","insideshowpd");

        totalItems = findViewById(R.id.total_items);
        amountTotal = findViewById(R.id.amount_total);
        amountDelivery = findViewById(R.id.amount_delivery);
        amountItems = findViewById(R.id.amount_items);
        amountSavings = findViewById(R.id.amount_savings);
        payableAmount = findViewById(R.id.payable_amount);
        pinView = findViewById(R.id.pin_view);
        tvDaysForDelivery = findViewById(R.id.tv_day_for_delivery);

        pinView.setText(pin.toString());

        if (daysForDelivery==1){
            tvDaysForDelivery.setText("(Delivery in "+daysForDelivery+" Day)");
        }
        else{
            tvDaysForDelivery.setText("(Delivery in "+daysForDelivery+" Days)");
        }


        priceDetailsUpdateListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("payactivitydebug","inside listener");

                //show progress dialogue as soon as there is change in price
//                if (!pricePd.isShowing() && pricePd != null) {
////                    mShimmerViewContainer.startShimmerAnimation();
////                    mShimmerViewContainer.setVisibility(View.VISIBLE);
//                    //pricePd.show();
//                }
                //1 count for total items in cart
                PriceDetails details = dataSnapshot.getValue(PriceDetails.class);
                priceDetails = details;


                //totalItemsInt = dataSnapshot.child("count").getValue(Integer.class);

                // if count is zero then hide pricedetails bottom and show noItemInCart Picture
                if (details.getCount() == 0) {
                    priceDetailsView.setVisibility(View.GONE);
                    //bottom.setVisibility(View.GONE);
                    //this was showing empty if not empty while loading
                    //noItemInCart.setVisibility(View.VISIBLE);
                    //Log.d("noItemInCart","480");
                }

                //set total items
                totalItems.setText("Price (" + details.getCount()+ " items)");

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

                //check for delivery rate
                //4
                //set delivery amount
                amountDelivery.setText(charges.toString());

                //5
                //set total with added delivery
                amountTotalInt = charges + details.getAmountDiscounted();
                amountTotal.setText("₹ " + amountTotalInt.toString());

                //6
                //set amount to bottom
                payableAmount.setText("₹ " + amountTotalInt.toString());

                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("payactivitydebug","error fetching listener dadta");

                Toast.makeText(PayActivity.this,"priceDetailsUpdateListener"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        };


        priceDetailsRef.addValueEventListener(priceDetailsUpdateListener);

        priceDetailsView.setVisibility(View.VISIBLE);

    }

    private void getAddress() {

        pd.show();

        DatabaseReference addressRef = FirebaseDatabase.getInstance()
                .getReference("Addresses").child(mAuth.getCurrentUser().getUid());

        addressRef.orderByChild("timeAdded").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //address exists
                if (dataSnapshot.exists()){

                    Log.d("addressdebug","addess exists");

                    Address addressObj = new Address();

                    Address middleAddress = new Address();

                    boolean flag = false;

                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        addressObj = ds.getValue(Address.class);

                        if (addressObj.getPincode().equals(pin)){
                            flag = true;
                            middleAddress = addressObj;
                        }
                    }

                    //
                   if (flag && middleAddress.getPincode().equals(pin)){
                       Log.d("addressdebug","pincode equal ");

                       address = addressObj;

                      // showPriceDetails();
                       setAddressByObject(address);
                       pin = address.getPincode();
                       calculateDeliveryAndSetPriceDetails();
                   }
                   else{

                       Intent selectAddressIntent = new Intent(PayActivity.this,AddressesActivity.class);
                       selectAddressIntent.putExtra("mode","select");
                       startActivityForResult(selectAddressIntent,3);
                   }

                }
                //address doesn't exit
                else{
                    Log.d("addressdebug","addess does not exists");

                    Intent addressIntent = new Intent(PayActivity.this,AddAddressActivity.class);
                    addressIntent.putExtra("pin",pin);
                    startActivityForResult(addressIntent,44);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("pin",pin);
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    void payUsingUpi(String amount, String upiId, String name, String note) {


        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();


        Intent upiPayIntent = new Intent();
        upiPayIntent.setData(uri);

        String[] desireApp = new String[]{"net.one97.paytm", "com.freecharge.android",
                "com.google.android.apps.nbu.paisa.user"};

        setTheme(R.style.ActionSheetStyle);
        showActionSheet(upiPayIntent,desireApp,upiId,name,note);

//        // will always show a dialog to user to choose an app
//        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
//
//        // check if intent resolves
//        if(null != chooser.resolveActivity(getPackageManager())) {
//            startActivityForResult(chooser, UPI_PAYMENT);
//        } else {
//            Toast.makeText(PayActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
//        }

    }

    public void showActionSheet(Intent intent,String[] desireApp ,String upiId,String name,String note) {
        actionSheet = new ActionSheet(this);
        actionSheet.setCancelButtonTitle("Cancel Payment");

        //want to set list call this method
        //actionSheet.addItems("Cricket","Football","Hockey","BasketBall");

        //want to set gridLayout call this method or you can add any type of custom layout
        //get view with populated data and pass to
        View view = bottomDialogAsChooserCustom(intent,desireApp,upiId,name,note);
        actionSheet.setBottomDialogAsCustomView(view);

        actionSheet.setItemClickListener(this);
        actionSheet.setCancelableOnTouchMenuOutside(true);
        actionSheet.showMenu();

    }

    private View bottomDialogAsChooserCustom(final Intent prototype, String[] desiredApp, final String upiId, final String name, final String note) {
        String noAppMsg = "";
        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(prototype, 0);
        EvalInstalledAppInfo evalInstalledAppInfo;
        final ArrayList<EvalInstalledAppInfo> evalInstalledAppInfoArrayList = new ArrayList<>();

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                if (Arrays.asList(desiredApp).contains(resolveInfo.activityInfo.packageName)) {

                    ApkInfoUtil apkInfoUtil = new ApkInfoUtil(mContext);
                    Drawable appIcon = apkInfoUtil.getAppIconByPackageName(resolveInfo.activityInfo.packageName);

                    if(String.valueOf(resolveInfo.activityInfo.loadLabel(mContext.getPackageManager())).toLowerCase().equals("UPI In-app payments".toLowerCase()))
                    {
                        evalInstalledAppInfo = new EvalInstalledAppInfo(appIcon,
                                resolveInfo.activityInfo.name, resolveInfo.activityInfo.packageName,
                                "Google Pay");
                    }else
                    {
                        evalInstalledAppInfo = new EvalInstalledAppInfo(appIcon,
                                resolveInfo.activityInfo.name, resolveInfo.activityInfo.packageName,
                                String.valueOf(resolveInfo.activityInfo.loadLabel(mContext.getPackageManager())));
                    }
                    evalInstalledAppInfoArrayList.add(evalInstalledAppInfo);
                }
            }

            if (!evalInstalledAppInfoArrayList.isEmpty()) {
                // sorting for nice readability
                Collections.sort(evalInstalledAppInfoArrayList, new Comparator<EvalInstalledAppInfo>() {
                    @Override
                    public int compare(EvalInstalledAppInfo map, EvalInstalledAppInfo map2) {
                        return map.getSimpleName().compareTo(map2.getSimpleName());
                    }
                });

            }else
            {
                noAppMsg = "Desired app is not available in your android phone.Please try with GPay,Paytm and Freecharge.";
            }
        }else
        {
            noAppMsg = "There is no UPI app in your mobile.";
            Toast.makeText(mContext, "There is no UPI app in your mobile.", Toast.LENGTH_SHORT).show();
        }

        //View view = getLayoutInflater().inflate(R.layout.custom_chooser_main_layout, null);
        View mainLayoutInGridView = getLayoutInflater().inflate(R.layout.custom_chooser_main_layout, null);

        /*final BottomSheetDialog dialog = new BottomSheetDialog(mContext); //for this you have to use design lib

        //dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box_bg);

        dialog.setContentView(view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();*/

        GridView customChooserMainGv = (GridView) mainLayoutInGridView.findViewById(R.id.chooserGv);
        TextView noData = (TextView) mainLayoutInGridView.findViewById(R.id.noDataTextView);
        Button cancelDialog = (Button) mainLayoutInGridView.findViewById(R.id.cancel);

        cancelDialog.setVisibility(View.GONE);

        mainLayoutInGridView.setBackgroundDrawable(CommonUtil.setRoundedCorner(WHITE, WHITE,10,
                GradientDrawable.RECTANGLE));

        if(!evalInstalledAppInfoArrayList.isEmpty())
        {
            noData.setVisibility(View.GONE);
            customChooserMainGv.setVisibility(View.VISIBLE);

            CustomChooserAdapter customChooserAdapter = new CustomChooserAdapter(mContext);
            customChooserAdapter.setDataAdapter(evalInstalledAppInfoArrayList, new ListEvent() {
                @Override
                public void onLongClick(int index) {
                    appName = evalInstalledAppInfoArrayList.get(index).getSimpleName();
                    Toast.makeText(PayActivity.this,appName,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onClick(final int index) {


                    Toast.makeText(PayActivity.this,"clickeddd",Toast.LENGTH_SHORT).show();

//                    if(evalInstalledAppInfoArrayList.get(index).getSimpleName().toLowerCase().equals("UPI In-app payments".toLowerCase()))
//                    {
//                        appName = "Google Pay";
//                    }else
//                    {
                    appName = evalInstalledAppInfoArrayList.get(index).getSimpleName();
//                    }

                    //paytm doesn't open the app perfectly if it is not open already in background. so i have used this way to work
                    //some time.
//                    if(evalInstalledAppInfoArrayList.get(index).getSimpleName().toLowerCase().equals("Paytm".toLowerCase()))
//                    {
////                        startActivity(mContext.getPackageManager().getLaunchIntentForPackage(evalInstalledAppInfoArrayList.get(index).getPackageName()));
////
////                        new Handler().postDelayed(new Runnable() {
////                            @Override
////                            public void run() {
////                                Intent targetedShareIntent = (Intent) prototype.clone();
////                                targetedShareIntent.setPackage(evalInstalledAppInfoArrayList.get(index).getPackageName());
////                                targetedShareIntent.setClassName(evalInstalledAppInfoArrayList.get(index).getPackageName(),
////                                        evalInstalledAppInfoArrayList.get(index).getClassName());
////                                startActivityForResult(targetedShareIntent,UPI_PAYMENT);
////                                //FgtPaymentNdOffers.this.startActivityForResult(targetedShareIntent, ActivityCode.PAY_REQ_CODE);
////                            }
////                        },1800);
//                    }else {


                    DatabaseReference discountedAmtRef = db.getReference("PriceDetails").
                            child(mAuth.getCurrentUser().getUid()).child("amountDiscounted");

                    discountedAmtRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String amountDiscounted = String.valueOf(dataSnapshot.getValue(Integer.class)+charges);

                            makeOrderRequestUPI(address.getId(),"upi",
                                    upiId,name,amountDiscounted,
                                    evalInstalledAppInfoArrayList.get(index).getPackageName(),
                                    evalInstalledAppInfoArrayList.get(index).getClassName());

                            pd.show();

                            //When you want to use this in fragment.
                            //FgtPaymentNdOffers.this.startActivityForResult(targetedShareIntent, ActivityCode.PAY_REQ_CODE);
                            //      }
                            //dialog.dismiss();
                            actionSheet.dismissMenu ();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });
            customChooserMainGv.setAdapter(customChooserAdapter);

        }else
        {
            //noAppMsg = "Desired app is not available in your android phone.Please try with GPay, Paytm and Freecharge.";
            noData.setText(noAppMsg);
            noData.setPadding(20,20,
                    20,20);
            noData.setVisibility(View.VISIBLE);
            customChooserMainGv.setVisibility(View.GONE);
        }

        return mainLayoutInGridView;

    }




    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:

                pd.show();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UserOrders").child(mAuth.getCurrentUser().getUid());
                final Query query = ref.orderByChild("tOid").equalTo(tempOrderId);

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {

                            if (dataSnapshot.getValue() != null) {

                                query.removeEventListener(this);

                                Order order = dataSnapshot1.getValue(Order.class);

                                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                                    if (data != null) {
                                        String trxt = data.getStringExtra("response");
                                        Log.d("UPI", "onActivityResult: " + trxt);
                                        ArrayList<String> dataList = new ArrayList<>();
                                        dataList.add(trxt);
                                        upiPaymentDataOperation(dataList,order.getOrderId());
                                    }
                                    else {
                                        updateUpiConfirmation(order.getOrderId(),targetUpiList.get(count).getAddress(),"","failed",1L);
                                        Log.d("UPI", "onActivityResult: " + "Return data is null, contact if getting issue");
                                    }
                                }
                                else {
                                    updateUpiConfirmation(order.getOrderId(),targetUpiList.get(count).getAddress(),"","failed",1L);
                                    Log.d("UPI", "onActivityResult: " + "Please select app after clicking place order button"); //when user simply back without payment
                                }
                                break;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                break;

            case 44 :
                if (resultCode==RESULT_OK){
                    if (data!=null){
                        Address addressObj = (Address) data.getSerializableExtra("address");

                        address = addressObj;

                        if (address.getPincode()!=null){
                            notDeliverable = false;
                            setAddressByObject(address);
                            pin = address.getPincode();
                            calculateDeliveryAndSetPriceDetails();
                        }
                    }
                    else{
                        finish();
                    }
                }
                else{
                    finish();
                }
                break;

            case 3:
                if (resultCode==RESULT_OK){
                    //after selecting address from list
                    Address addressObj = (Address) data.getSerializableExtra("address");

                    address = addressObj;

                    if (address.getPincode()!=null){
                        notDeliverable = false;
                        setAddressByObject(address);
                        pin = address.getPincode();
                        calculateDeliveryAndSetPriceDetails();
                    }

                }
                else{
                    finish();
                }

                break;

            case 4:
                if (resultCode==RESULT_OK){
                    //after selecting address from list
                    Address addressObj = (Address) data.getSerializableExtra("address");

                    address = addressObj;

                    if (address.getPincode()!=null){
                        notDeliverable = false;
                        setAddressByObject(address);
                        pin = address.getPincode();
                        calculateDeliveryAndSetPriceDetails();
                    }

                }
                break;


        }
    }

    private void calculateDeliveryAndSetPriceDetails() {
        if (pin.toString().length() == 6){

            pd.show();

            DatabaseReference deliveryRef = FirebaseDatabase.getInstance().
                    getReference("Delivery").child("pin").child(pin.toString());

            deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("deliveryCharges").exists()){
                        pd.dismiss();
                        charges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
                        daysForDelivery = dataSnapshot.child("daysForDelivery").getValue(Integer.class);
                        showPriceDetails();
                    }
                    else{
                        pd.dismiss();
                        pinView = findViewById(R.id.pin_view);
                        pinView.setText(pin.toString());
                        amountDelivery = findViewById(R.id.amount_delivery);
                        amountTotal = findViewById(R.id.amount_total);
                        amountDelivery.setText("--");
                        amountTotal.setText("--");
                        pin = "000000";
                        Toast.makeText(PayActivity.this,"Not deliverable",Toast.LENGTH_SHORT).show();
                        notDeliverable = true;


                        final AlertDialog.Builder alert = new AlertDialog.Builder(PayActivity.this);
                        alert.setTitle("Address not deliverable");
                        alert.setMessage("Please try different pincode to get your order delivered");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog dialog = alert.create();
                        dialog.show();


//                        Intent selectAddressIntent = new Intent(PayActivity.this,AddressesActivity.class);
//                        selectAddressIntent.putExtra("mode","select");
//                        selectAddressIntent.putExtra("defaultAddressId",address.getId());
//                        startActivityForResult(selectAddressIntent,4);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    pd.dismiss();
                    Toast.makeText(PayActivity.this,"Delivery fees access not allowed",Toast.LENGTH_SHORT).show();
                }
            });


        }else{
            Toast.makeText(PayActivity.this,"Please enter 6 digit pin code",Toast.LENGTH_SHORT).show();
        }

    }

    private void setAddressByObject(Address addressObj){



        TextView name,number,type,address;
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        type = findViewById(R.id.type);
        address = findViewById(R.id.address);

        name.setText(addressObj.getName());
        number.setText(addPhone(addressObj.getNumber()));
        type.setText(addressObj.getType());

        id = addressObj.getId();

        String combinedAddress = addressObj.getBuildingNameNumber() +" "+
                addressObj.getAreaRoad()  +" "+
                addressObj.getCity()  +" "+
                addressObj.getState() + "-"+
                addressObj.getPincode();

        address.setText(combinedAddress);
        addressDetailsLayout.setVisibility(View.VISIBLE);
    }

    private String addPhone(String numberFromDatabase){

        FirebaseUser user = mAuth.getCurrentUser();

        Boolean found = false;
        String num = "";

        for (UserInfo profile : user.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();

            if(providerId.equals("firebase")){

                String phone = profile.getPhoneNumber();
                if(phone!=null && phone.length()==13){
                    found = true;
                    phone = phone.substring(3);
                    num = phone;
                }
                break;
            }

        }

        if (!found){
            if (numberFromDatabase!=null && numberFromDatabase.length()==10){
                num = numberFromDatabase;
            }
        }

        return num;
    }


    private void upiPaymentDataOperation(ArrayList<String> data,String oId) {

            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            paymentCancel = "";
            if(str == null) str = "discard";
            status = "";
            approvalRefNo = "";
            responseCode="";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                    else if (equalStr[0].toLowerCase().equals("responseCode".toLowerCase())){
                        responseCode = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        Date currentDate = Calendar.getInstance().getTime();

        String date = dateFormat.format(currentDate);


        if (status.equals("success") && responseCode.equals("0")) {
            //Code to handle successful transaction here.
            // makeOrderRequest(addressId,"upi",approvalRefNo,targetUpiList.get(count).getAddress());

            updateUpiConfirmation(oId,targetUpiList.get(count).getAddress(),approvalRefNo,"success",Long.parseLong(date));
            pd.setTitle("Transaction Successful...");

            // Toast.makeText(PayActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
            // Log.d("UPI", "responseStr: "+approvalRefNo);
        }
        else if ( (status.toLowerCase().equals("submitted")) || (status.toLowerCase().equals("success") && responseCode.equals("92")) ){
            updateUpiConfirmation(oId,targetUpiList.get(count).getAddress(),approvalRefNo,"pending",Long.parseLong(date));

        }
        else if("Payment cancelled by user.".equals(paymentCancel)) {
            updateUpiConfirmation(oId,targetUpiList.get(count).getAddress(),approvalRefNo,"failed",Long.parseLong(date));
            Toast.makeText(PayActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
        }
        else {
            updateUpiConfirmation(oId,targetUpiList.get(count).getAddress(),approvalRefNo,"failed",Long.parseLong(date));
            count++;
            Toast.makeText(PayActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
        }





    }

    private void updateUpiConfirmation(final String oId, String targetUpi, String tsnId, final String upiStatus, Long time){

        DatabaseReference upiConfirmationRef = FirebaseDatabase.getInstance().getReference("UpiConfirmation").child(mAuth.getCurrentUser().getUid()).child(oId);

        UpiTransaction upiTransaction = new UpiTransaction(oId,tsnId,targetUpi,time,upiStatus);


        upiConfirmationRef.setValue(upiTransaction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull final Task<Void> task) {
                if (task.isSuccessful()){

                    if (upiStatus.equals("failed")){
                        pd.dismiss();
                    }
                    else {
                        final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(mAuth.getCurrentUser().getUid())
                                .child(oId);

                        statusRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("priceDetails").child("upiStatus").getValue()!=null &&
                                    !dataSnapshot.child("priceDetails").child("upiStatus").getValue().equals("failed")
                                && !dataSnapshot.child("priceDetails").child("upiStatus").getValue().equals("")){
                                    Order order = dataSnapshot.getValue(Order.class);

                                    if (order.getPriceDetails().getUpiStatus()!=null){
                                        statusRef.removeEventListener(this);

                                        rIntent.putExtra("orderId",order.getOrderId());
                                        rIntent.putExtra("source","orderFlow");
                                        pd.dismiss();

                                        setResult(RESULT_OK, rIntent);
                                        finish();
                                    }

                                }
                                else {
                                    pd.dismiss();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(PayActivity.this,"order access "+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                            }
                        });
                    }


                }
                else {
                    Toast.makeText(PayActivity.this,"upiConfirmation "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    private void makeOrderRequestCOD(final String addressId, final String method) {
        Log.d("debugOrder","makeOrderCalled inside");

        if (!pd.isShowing()){
            pd.show();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        Date currentDate = Calendar.getInstance().getTime();

        String date = dateFormat.format(currentDate);

        final DatabaseReference orderRequestRef = FirebaseDatabase.getInstance().getReference("OrderReq").child(mAuth.getCurrentUser().getUid());

        final OrderRequest order = new OrderRequest();
        order.setAddressId(addressId);
        order.setMethod(method);
        order.setUserTimeAdded(Long.valueOf(date));
        order.setPin(pin);

        tempOrderId = orderRequestRef.push().getKey();

                orderRequestRef.child(tempOrderId).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("debugOrder","request task completed");

                        rIntent = new Intent();

                            if (method.equals("cod")){
                                Log.d("debugOrder","task is successful");

                                pd.setTitle("Confirming your transaction...");
                                pd.show();

                                DatabaseReference userOrdersRef = db.getReference("UserOrders").child(uid);
                                q = userOrdersRef.orderByChild("tOid").equalTo(tempOrderId);

                                db.getReference("UserOrderOutOfStock").child(uid).child(tempOrderId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()!=null){
                                        db.getReference("UserOrderOutOfStock").child(uid).child(tempOrderId).removeEventListener(this);

                                        pd.dismiss();
                                        Toast.makeText(mContext, "Sorry Some items went out of stock... Try again :(", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                                q.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                                            if (dataSnapshot.getValue()!=null){
                                                Log.d("debugOrder",dataSnapshot1.toString());

                                                String orderId = dataSnapshot1.child("orderId").getValue(String.class);
                                                Log.d("debugOrder","orderId "+orderId);

                                                q.removeEventListener(this);
                                                Order order = dataSnapshot1.getValue(Order.class);
                                                Log.d("debugOrder","orderId in object cheack: "+order.getOrderId());

                                                rIntent.putExtra("orderId",order.getOrderId());
                                                rIntent.putExtra("source","orderFlow");
                                                pd.dismiss();

                                                setResult(RESULT_OK, rIntent);
                                                finish();
                                            }
                                        }




                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d("debugOrder","database error");
                                        pd.dismiss();
                                        Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                    }
                });
    }

    private void makeOrderRequestUPI(final String addressId, final String method,
                                     final String upiId, final String name,
                                     final String amountDiscounted,final String packageName,
                                     final String className) {
        Log.d("debugOrder","makeOrderCalled inside");

        if (!pd.isShowing()){
            pd.show();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        Date currentDate = Calendar.getInstance().getTime();

        String date = dateFormat.format(currentDate);

        final DatabaseReference orderRequestRef = FirebaseDatabase.getInstance().getReference("OrderReq").child(mAuth.getCurrentUser().getUid());

        final OrderRequest order = new OrderRequest();
        order.setAddressId(addressId);
        order.setMethod(method);
        order.setUserTimeAdded(Long.valueOf(date));
        order.setPin(pin);

        tempOrderId = orderRequestRef.push().getKey();

        orderRequestRef.child(tempOrderId).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("debugOrder","request task completed");

                rIntent = new Intent();

                    db.getReference("UserOrderOutOfStock").child(uid).child(tempOrderId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()!=null){
                                db.getReference("UserOrderOutOfStock").child(uid).child(tempOrderId).removeEventListener(this);
                                pd.dismiss();
                                Toast.makeText(mContext, "Sorry Some items went out of stock... Try again :(", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    DatabaseReference userOrdersRef = db.getReference("UserOrders").child(uid);

                    q = userOrdersRef.orderByChild("tOid").equalTo(tempOrderId);
                    q.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue()!=null){

                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    Log.d("PayActivityLog",ds.toString());
                                    Uri uri = Uri.parse("upi://pay").buildUpon()
                                            .appendQueryParameter("pa",upiId)
                                            .appendQueryParameter("pn", name)
                                            .appendQueryParameter("tn", "Order id "+ds.child("orderId").getValue(String.class))
                                            .appendQueryParameter("am", amountDiscounted)
                                            .appendQueryParameter("cu", "INR")
                                            .build();


                                    Intent upiPayIntent = new Intent();
                                    upiPayIntent.setData(uri);

                                    Intent targetedShareIntent = (Intent) upiPayIntent.clone();
                                    targetedShareIntent.setPackage(packageName);
                                    targetedShareIntent.setClassName(packageName,
                                            className);
                                    pd.dismiss();
                                    startActivityForResult(targetedShareIntent, UPI_PAYMENT);
                                    q.removeEventListener(this);
                                    break;
                                }

                            }




                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("debugOrder","database error");
                            pd.dismiss();
                            Toast.makeText(PayActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }

        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        totalAmountRef.removeEventListener(amountPaybleLoader);
    }

    @Override
    public void onItemClick(int itemPosition) {

            Toast.makeText(PayActivity.this,"Item "+itemPosition+" Clicked",Toast.LENGTH_SHORT).show();
    }
}
