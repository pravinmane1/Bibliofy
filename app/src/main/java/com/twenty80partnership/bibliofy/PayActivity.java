package com.twenty80partnership.bibliofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.adapters.CustomChooserAdapter;
import com.twenty80partnership.bibliofy.events.ListEvent;
import com.twenty80partnership.bibliofy.models.EvalInstalledAppInfo;
import com.twenty80partnership.bibliofy.models.Order;
import com.twenty80partnership.bibliofy.models.OrderRequest;
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

public class PayActivity extends AppCompatActivity implements ActionSheet.MenuItemClickListener{

    final int UPI_PAYMENT = 0;
    RadioGroup radioGroup;
    RadioButton radioButton;
    TextView placeOrder;
    TextView payableAmount;
    FirebaseAuth mAuth;
    DatabaseReference totalAmountRef,payTargetRef;
    private String addressId;
    private ProgressDialog pd;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Log.d("debugOrder","acrt");

        mContext = this.getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(PayActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Loading...");
        pd.show();

        radioGroup = findViewById(R.id.radio_group);
        placeOrder = findViewById(R.id.place_order);
        payableAmount = findViewById(R.id.payable_amount);


        mAuth = FirebaseAuth.getInstance();
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



        totalAmountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null){
                  final Integer amount =  dataSnapshot.getValue(Integer.class);
                    DatabaseReference deliveryRef = FirebaseDatabase.getInstance().getReference("Delivery");

                    deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            pd.dismiss();
                            Integer d = dataSnapshot.child("basic").child("rate").getValue(Integer.class);
                            Integer total = d + amount;
                           payableAmount.setText(total.toString());
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

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();
                if (intent.getStringExtra("id")!=null){
                    addressId = intent.getStringExtra("id");
                }
                else {
                    finish();
                }

                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);
                if (selectedId == -1){
                    Toast.makeText(getApplicationContext(),"Please select payment method",Toast.LENGTH_SHORT).show();
                }
                else {
                    switch (selectedId){
                        case R.id.cod:
                            makeOrderRequest(addressId,"cod","","");
                            Log.d("debugOrder","makeOrderCalled");
                            break;
                        case R.id.upi:
                            if(count<targetUpiList.size()){
                                //Toast.makeText(getApplicationContext(),targetUpiList.get(count).getAddress(),Toast.LENGTH_SHORT).show();

                                payUsingUpi(intent.getStringExtra("amount"),targetUpiList.get(count).getAddress(),"Bibliofy Order","Bibliofy Order");
                            }
                            else {
                                //Toast.makeText(getApplicationContext(),targetUpiList.get(0).getAddress(),Toast.LENGTH_SHORT).show();
                                    count = 0;
                                  payUsingUpi(intent.getStringExtra("amount"),targetUpiList.get(count).getAddress(),"Bibliofy Order","Bibliofy Order");

                            }

                           // Toast.makeText(getApplicationContext(),"Google Pay / PhonePe / BHIM UPI / Paytm / Any UPI app",Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            }
        });





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
        showActionSheet(upiPayIntent,desireApp);

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

    public void showActionSheet(Intent intent,String[] desireApp ) {
        actionSheet = new ActionSheet(this);
        actionSheet.setCancelButtonTitle("Cancel Payment");

        //want to set list call this method
        //actionSheet.addItems("Cricket","Football","Hockey","BasketBall");

        //want to set gridLayout call this method or you can add any type of custom layout
        //get view with populated data and pass to
        View view = bottomDialogAsChooserCustom(intent,desireApp);
        actionSheet.setBottomDialogAsCustomView(view);

        actionSheet.setItemClickListener(this);
        actionSheet.setCancelableOnTouchMenuOutside(true);
        actionSheet.showMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                pd.show();
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null, contact if getting issue");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    pd.dismiss();
                    Log.d("UPI", "onActivityResult: " + "Please select app after clicking place order button"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    //Toast.makeText(PayActivity.this,"Please select app after clicking place order button",Toast.LENGTH_SHORT).show();
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private View bottomDialogAsChooserCustom(final Intent prototype, String[] desiredApp) {
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

//                    if(evalInstalledAppInfoArrayList.get(index).getSimpleName().toLowerCase().equals("UPI In-app payments".toLowerCase()))
//                    {
//                        appName = "Google Pay";
//                    }else
//                    {
                    appName = evalInstalledAppInfoArrayList.get(index).getSimpleName();
//                    }

                    //paytm doesn't open the app perfectly if it is not open already in background. so i have used this way to work
                    //some time.
                    if(evalInstalledAppInfoArrayList.get(index).getSimpleName().toLowerCase().equals("Paytm".toLowerCase()))
                    {
                        startActivity(mContext.getPackageManager().getLaunchIntentForPackage(evalInstalledAppInfoArrayList.get(index).getPackageName()));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent targetedShareIntent = (Intent) prototype.clone();
                                targetedShareIntent.setPackage(evalInstalledAppInfoArrayList.get(index).getPackageName());
                                targetedShareIntent.setClassName(evalInstalledAppInfoArrayList.get(index).getPackageName(),
                                        evalInstalledAppInfoArrayList.get(index).getClassName());
                                startActivityForResult(targetedShareIntent,UPI_PAYMENT);
                                //FgtPaymentNdOffers.this.startActivityForResult(targetedShareIntent, ActivityCode.PAY_REQ_CODE);
                            }
                        },1800);
                    }else {
                        Intent targetedShareIntent = (Intent) prototype.clone();
                        targetedShareIntent.setPackage(evalInstalledAppInfoArrayList.get(index).getPackageName());
                        targetedShareIntent.setClassName(evalInstalledAppInfoArrayList.get(index).getPackageName(),
                                evalInstalledAppInfoArrayList.get(index).getClassName());
                        startActivityForResult(targetedShareIntent, UPI_PAYMENT);

                        //When you want to use this in fragment.
                        //FgtPaymentNdOffers.this.startActivityForResult(targetedShareIntent, ActivityCode.PAY_REQ_CODE);
                    }
                    //dialog.dismiss();
                    actionSheet.dismissMenu ();
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

    private void upiPaymentDataOperation(ArrayList<String> data) {

            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
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

            if (status.equals("success") && responseCode.equals("0")) {
                //Code to handle successful transaction here.
                makeOrderRequest(addressId,"upi",approvalRefNo,targetUpiList.get(count).getAddress());

                pd.setTitle("Transaction Successful...");

               // Toast.makeText(PayActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
               // Log.d("UPI", "responseStr: "+approvalRefNo);
            }
            else if ( (status.toLowerCase().equals("submitted")) || (status.toLowerCase().equals("success") && responseCode.equals("92")) ){
                if (pd.isShowing()){
                    pd.dismiss();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(PayActivity.this);
                builder.setMessage("1.If amount debited press \"CONTINUE\" to proceed with order." +
                        "\n\n2.If you want to place order with current payment status (even if amount is not debited) press \"CONTINUE\".");
                builder.setCancelable(false);
                builder.setTitle("Payment Not Confirmed");

                builder.setPositiveButton("CONTINUE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                makeOrderRequest(addressId,"upiPending",approvalRefNo,targetUpiList.get(count).getAddress());
                            }
                        });

                builder.setNegativeButton("CANCEL ORDER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {

                pd.dismiss();
                Toast.makeText(PayActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                count++;
                pd.dismiss();
                Toast.makeText(PayActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }

    }



    private void makeOrderRequest(final String addressId, final String method, final String tsnId, final String targetUpi) {
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
        order.setTsnId(tsnId);
        order.setTargetUpi(targetUpi);

        final String tempOrderId = orderRequestRef.push().getKey();

                orderRequestRef.child(tempOrderId).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("debugOrder","request task completed");

                        rIntent = new Intent();

                        if (!task.isSuccessful()){
                            Log.d("debugOrder","task is not successful");

                            if (!method.toLowerCase().equals("cod")){
                                pd.setTitle("Retrying order");
                                orderRequestRef.child(tempOrderId).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //rIntent.putExtra("order",order);
                                            rIntent.putExtra("source","orderFlow");
                                            pd.dismiss();

                                            setResult(RESULT_OK, rIntent);
                                            finish();
                                        }
                                        else{
                                            Date currentTime = Calendar.getInstance().getTime();
                                            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                                            String date = dateFormat.format(currentTime);

                                            final UpiTransaction upiTransaction = new UpiTransaction(mAuth.getCurrentUser().getUid(),
                                                    tsnId,targetUpi,Long.parseLong(date),false);

                                            final DatabaseReference upiFailedRef = FirebaseDatabase.getInstance().getReference("OrderPlaceFailed").child(mAuth.getCurrentUser().getUid());
                                            upiFailedRef.push().setValue(upiTransaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        pd.dismiss();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(PayActivity.this);
                                                        builder.setTitle("Order Request Failed");
                                                        builder.setMessage("Your order request is failed, if any amount deducted will be credited back.");
                                                        builder.setCancelable(false);

                                                        builder.setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        finish();
                                                                    }
                                                                });

                                                        AlertDialog alert = builder.create();
                                                        alert.show();
                                                    }
                                                    else {
                                                        pd.dismiss();
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else {
                                pd.dismiss();
                                setResult(RESULT_CANCELED, rIntent);
                                Toast.makeText(PayActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {

                            Log.d("debugOrder","task is successful");

                            pd.setTitle("Confirming your transaction...");
                            DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(mAuth.getCurrentUser().getUid());
                            q = userOrdersRef.orderByChild("tOid").equalTo(tempOrderId);


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

                                            rIntent.putExtra("order",order);
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


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onItemClick(int itemPosition) {

            Toast.makeText(PayActivity.this,"Item "+itemPosition+" Clicked",Toast.LENGTH_SHORT).show();
    }
}
