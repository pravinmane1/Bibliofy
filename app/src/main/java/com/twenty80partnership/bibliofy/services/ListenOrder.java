package com.twenty80partnership.bibliofy.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.OrderDetailsActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.models.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.os.Build.VERSION_CODES.P;

public class ListenOrder extends Service implements ValueEventListener {

    FirebaseDatabase db;
    DatabaseReference orders;
    FirebaseAuth mAuth;


    public ListenOrder() {


    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

    }

    public class MyBinder extends Binder {
        public ListenOrder getService() {
            return ListenOrder.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("listenOrder","working");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        orders = db.getReference("UserOrders").child(mAuth.getCurrentUser().getUid());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orders.orderByChild("timeAdded").limitToLast(3).addValueEventListener(this);
        return  START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void showNotification(String key, Order order) {
        //todo
//
//      //  Log.d("listenOrder","sn is called for order id"+order.getOrderId());
//
//        Intent intent = new Intent(getBaseContext(), OrderDetailsActivity.class);
//        intent.putExtra("orderId",order.getOrderId());
//        intent.putExtra("order",order);
//        intent.putExtra("source","notification");
//        Log.d("listenOrder","intent is created");
//
//        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),P,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
//
//        builder.setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setTicker("Bibliofy")
//                .setContentInfo("Order Notification")
//                .setContentText("Your order id: "+order.getOrderId()+" is "+order.getOrderStatus())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(contentIntent);
//        Log.d("listenOrder","notification is built");
//
//        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        //unique id for each notification
//        int randomInt = new Random().nextInt(9999-1)+1;
//        manager.notify(randomInt,builder.build());
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Log.d("listenOrder",dataSnapshot.toString());

        HashMap<String,Order> orderMap = new HashMap<String,Order>();

        for(DataSnapshot ds:dataSnapshot.getChildren()){
            Order order = ds.getValue(Order.class);

            orderMap.put(order.getOrderId(),order);
        }

        for(Map.Entry<String,Order> entry:orderMap.entrySet()) {


            Order order = entry.getValue();//dataSnapshot1.getValue(Order.class);
            if (order == null) {
                Log.d("listenOrder", "order is null");

            }
            if(order.getUserViewed()!=null && order.getOrderStatus()!=null) {
                if (!order.getUserViewed() && !order.getOrderStatus().equals("placed")) {
                    showNotification(order.getOrderId(), order);
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
