package com.twenty80partnership.bibliofy;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.models.Banner;
import com.twenty80partnership.bibliofy.models.User;
import com.twenty80partnership.bibliofy.services.ListenOrder;
import com.twenty80partnership.bibliofy.sql.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;



public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog pd;
    private DrawerLayout drawerLayout;
    private TextView headerName, headerEmail;
    private ImageView headerPhoto;
    private SliderLayout sliderLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference banners,userDataRef,currentSemRef;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private User currentUser;

    private boolean val = false;
    private ArrayList<Banner> bannerList;
    private ValueEventListener userDataListener;

    private DatabaseHelper databaseHelper;
    private String path;
    private SliderView sliderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent service = new Intent(DashboardActivity.this, ListenOrder.class);
        startService(service);


        databaseHelper = new DatabaseHelper(this);
        //bannerTable = new DatabaseHelper(this);


        ImageView downloadImg = findViewById(R.id.downloadimg);

        Cursor cursor1 =  databaseHelper.getAllData1();

//        StringBuffer stringBuffer = new StringBuffer();
//        while (cursor1.moveToNext()){
//            stringBuffer.append(" id "+cursor1.getString(0)+"\n");
//            stringBuffer.append(" name "+cursor1.getString(2)+"\n");
//            stringBuffer.append(" priority "+cursor1.getString(3)+"\n");
//        }
//
//        TextView textView = findViewById(R.id.info);
//        textView.setText(stringBuffer.toString());

//        downloadImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                databaseHelper.deleteData2(1);
//            }
//        });

        Cursor cursor = databaseHelper.getData2(1);

        sliderLayout = findViewById(R.id.imageSlider);
        bannerList=new ArrayList<>();

        if (cursor.getCount()==0){
            initializeBannerDatabase();
        }
        else {
            setSliderViews();
            checkVersionAndUpdateBannerTable();
        }


        pd = new ProgressDialog(DashboardActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);
        //pd.show();


       // setSliderViews();
        Log.d("downloadimg","in oncreate");

       // String r = downloadAndSetImg("https://firebasestorage.googleapis.com/v0/b/trialmanual.appspot.com/o/index.jpeg?alt=media&token=73496335-faf7-4abb-b059-ae01e0f72ddc");
        //Toast.makeText(DashboardActivity.this,r,Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();


        //taking system time
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date currentTime= Calendar.getInstance().getTime();
        Long date=Long.parseLong(dateFormat.format(currentTime));


        DatabaseReference lastUsed = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("lastOpened");
        lastUsed.setValue(date);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer);

        View headerView = navigationView.getHeaderView(0);


        headerName = (TextView) headerView.findViewById(R.id.header_name);
        headerEmail = (TextView) headerView.findViewById(R.id.header_email);
        headerPhoto = headerView.findViewById(R.id.header_photo);
        //CardView photoCard = headerView.findViewById(R.id.view2);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(DashboardActivity.this,ProfileActivity.class));
                Intent mSharedIntent = new Intent(DashboardActivity.this,ProfileActivity.class);

                Pair[] pairs = new Pair[2];
                //pairs[0] = new Pair<View,String>(photoCard,"photo_transition");
                pairs[0] = new Pair<View,String>(headerName,"name_transition");
                pairs[1] = new Pair<View,String>(headerEmail,"email_transition");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DashboardActivity.this,pairs);
                    startActivity(mSharedIntent,options.toBundle());

                }
                else {
                    startActivity(new Intent(DashboardActivity.this,ProfileActivity.class));
                }

            }
        });

        CardView bookCard = findViewById(R.id.book_card);
        CardView examCard = findViewById(R.id.exam_card);
        CardView projectCard = findViewById(R.id.project_card);





        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

                String id = mAuth.getCurrentUser().getUid();
                userDataRef = FirebaseDatabase.getInstance().getReference("Users").child(id);

        //if user is not created then create
        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ( !dataSnapshot.child("email").exists() ){
                    FirebaseUser firebaseUser= mAuth.getCurrentUser();
                    User user = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(),
                            firebaseUser.getPhotoUrl().toString(),
                            null,null,null,null,null,0,null,0L,mAuth.getCurrentUser().getUid(),firebaseUser.getDisplayName().toLowerCase());
                    userDataRef.setValue(user);
                }
                else {
                    currentUser = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this,"userDataListerner"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();

            }
        };

        userDataRef.addValueEventListener(userDataListener);

        bookCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (currentUser!=null) {

                    Intent courseBookIntent;

                    if (currentUser.getCourseCode()!=null){
                        courseBookIntent = new Intent(DashboardActivity.this,AvlCategoryActivity.class);
                        courseBookIntent.putExtra("course",currentUser.getCourseCode());
                        courseBookIntent.putExtra("branchCode",currentUser.getBranchCode());
                        courseBookIntent.putExtra("yearCode",currentUser.getYearCode());
                        courseBookIntent.putExtra("bookType","Regular");
                        startActivity(courseBookIntent);
                    }
                    else {
                        startActivity(new Intent(DashboardActivity.this,BooksActivity.class));
                    }



                }
                else {
                    Toast.makeText(getApplicationContext(),"user is null",Toast.LENGTH_SHORT).show();
                }
            }
        });

        examCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,ExamsActivity.class));
            }
        });

        projectCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,ProjectsActivity.class));
            }
        });
    }

    private void checkVersionAndUpdateBannerTable() {
        DatabaseReference bannerUpdateRef = FirebaseDatabase.getInstance().getReference("BannerUpdate").child("SPPU");

        bannerUpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int version = dataSnapshot.child("version").getValue(Integer.class);

                Cursor cursor = databaseHelper.getData2(1);

                cursor.moveToFirst();
                if (Integer.valueOf(cursor.getString(2))!=version){

                    DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("Banners").child("SPPU");

                    bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot BannerData:dataSnapshot.getChildren()){

                                Log.d("dashboarddb","eventlistener called");

                                Banner banner=BannerData.getValue(Banner.class);
                                Cursor c = databaseHelper.getData1(banner.getId());

                                if(c.getCount()==0){
                                    boolean success = databaseHelper.insertData1(banner.getId(),banner.getImg(),banner.getName(),(int)banner.getPriority());
                                }
                                else {
                                    boolean success = databaseHelper.updateData1(banner.getId(),banner.getImg(),banner.getName(),(int)banner.getPriority());
                                }


                            }



                            boolean success = databaseHelper.updateData2(1,"bannerUpdate",version);
                            if(!success){
                                Toast.makeText(DashboardActivity.this,"bannerUpdate info failed",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    Toast.makeText(DashboardActivity.this,"version mismatch",Toast.LENGTH_SHORT).show();

                    boolean success = databaseHelper.updateData2(1,"bannerUpdate",version);
                    if (success){
                        Toast.makeText(DashboardActivity.this,"version updated",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(DashboardActivity.this,"version updation failed",Toast.LENGTH_SHORT).show();
                    }


                }
                //no need to update version show old bannerdata
                else{
                   // setSliderViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeBannerDatabase() {

        DatabaseReference bannerUpdateRef = FirebaseDatabase.getInstance().getReference("BannerUpdate").child("SPPU");

        bannerUpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int version = dataSnapshot.child("version").getValue(Integer.class);

                DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("Banners").child("SPPU");

                bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot BannerData:dataSnapshot.getChildren()){

                            Log.d("dashboarddb","eventlistener called");

                            Banner banner=BannerData.getValue(Banner.class);
                            //downloadAndSave(,"insert");
                            boolean success = databaseHelper.insertData1(banner.getId(),banner.getImg(),banner.getName(),(int)banner.getPriority());

                            if (!success)
                                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//                                else {
//                                    //Toast.makeText(getApplicationContext(), "id " + banner.getId() + " is inserted.", Toast.LENGTH_SHORT).show();
//                                }

                        }

                        setSliderViews();

                        boolean success = databaseHelper.insertData2(1,"bannerUpdate",version);
                        if(success){
                            Toast.makeText(DashboardActivity.this,"bannerUpdate info added",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(DashboardActivity.this,"banner update ref "+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();

                    }
                });





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void downloadAndSave(final String id, String url, final String name, final Integer priority,final String command){
        ImageRequest imgRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {

                Log.d("downloadimg","bitmap obtained");

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath=new File(directory,id+".jpg");

                path = mypath.toString();

                if (command.equals("insert")) {


                    boolean success = databaseHelper.insertData1(id, path, name, priority);

                    if (!success)
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getApplicationContext(), "id " + id + " is inserted.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    boolean success = databaseHelper.updateData1(id, path, name, priority);

                    if (!success)
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getApplicationContext(), "id " + id + " is inserted.", Toast.LENGTH_SHORT).show();
                    }
                }

                Log.d("downloadimg","path is set");

                FileOutputStream fos = null;
                try {
                    // Use the compress method on the Bitmap object to write image to
                    // the OutputStream
                    fos = new FileOutputStream(mypath);

                    // Writing the bitmap to the output stream
                    response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Log.d("downloadimg","bitmap is compressed");

                } catch (Exception e) {
                    Log.d("downloadimg","fos excep");

                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.d("downloadimg","fos close excep");

                        e.printStackTrace();
                    }
                }


            }
        }, 150, 150, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (command.equals("insert")) {


                            boolean success = databaseHelper.insertData1(id, "", name, priority);

                            if (!success)
                                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getApplicationContext(), "id " + id + " is inserted.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            boolean success = databaseHelper.updateData1(id, "", name, priority);

                            if (!success)
                                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getApplicationContext(), "id " + id + " is inserted.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Log.d("downloadimg","volleyerror");
                    }
                });

        Volley.newRequestQueue(this).add(imgRequest);
    }


    private String downloadAndSetImg(String url) {
        path="empty";

        Log.d("downloadimg","fun called");
        ImageRequest imgRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Log.d("downloadimg","bitmap obtained");

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath=new File(directory,"profile.jpg");

                path = mypath.toString();
//                Toast.makeText(getApplicationContext(),path,Toast.LENGTH_SHORT).show();

                Log.d("downloadimg","path is set");

                FileOutputStream fos = null;
                try {
                    // Use the compress method on the Bitmap object to write image to
                    // the OutputStream
                    fos = new FileOutputStream(mypath);

                    // Writing the bitmap to the output stream
                    response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Log.d("downloadimg","bitmap is compressed");

                } catch (Exception e) {
                    Log.d("downloadimg","fos excep");

                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.d("downloadimg","fos close excep");

                        e.printStackTrace();
                    }
                }
                String path = directory.getAbsolutePath();

                try {
                    File f=new File(path, "profile.jpg");
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                    Log.d("downloadimg","bitmap is accessed");

                    ImageView img=(ImageView)findViewById(R.id.downloadimg);
                    img.setImageBitmap(b);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }


            }
        }, 150, 150, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"error while downloading...",Toast.LENGTH_SHORT).show();

                    }
                });

        Volley.newRequestQueue(this).add(imgRequest);
return path;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUri = user.getPhotoUrl();

            String arr[] = name.split(" ", 2);
            String firstName = arr[0];
            headerName.setText(firstName);
            headerEmail.setText(email);
            Picasso.get().load(photoUri).placeholder(R.drawable.userdisplay).into(headerPhoto);
        }

    }



    private void setSliderViews() {


       // sliderLayout = findViewById(R.id.imageSlider);

        Cursor c = databaseHelper.getAllData1();
        while (c.moveToNext()){

            Log.d("dashboarddb",c.getString(0));


            Banner banner=new Banner(c.getString(0),c.getString(1),c.getString(2),Float.valueOf(c.getString(3)));


            bannerList.add(banner);

        }

        Collections.sort(bannerList);

        for (Banner currentBanner:bannerList){

            sliderView = new SliderView(DashboardActivity.this);
            Log.d("downloddd",currentBanner.getId());

//            if (!currentBanner.getImg().equals("")){
//                File f=new File(currentBanner.getImg(), currentBanner.getId()+".jpg");
//                Bitmap b = null;
//                try {
//                    b = BitmapFactory.decodeStream(new FileInputStream(f));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                Log.d("downloadimg","bitmap is accessed");
//
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                if (b!=null)
//                b.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                sliderView.setImageByte(byteArray);
//               // b.recycle();
//            }
//            else{
//                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Banners/SPPU/"+currentBanner.getId()+"/img");
//                ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.getValue()!=null&&!dataSnapshot.getValue().equals("")){
//                            sliderView.setImageUrl(dataSnapshot.getValue().toString());
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//            }

            final String name = currentBanner.getName();

            sliderView.setDescription(currentBanner.getName());

            sliderView.setImageUrl(currentBanner.getImg());

            sliderView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);

            sliderView.setOnSliderClickListener(new SliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(SliderView sliderView) {

                    switch (name){

                        case "BOOKS":
                            startActivity(new Intent(DashboardActivity.this,BooksActivity.class));
                            break;
                        case "PROJECTS":
                            startActivity(new Intent(DashboardActivity.this,ProjectsActivity.class));
                            break;
                        case "EXAM SAVIOURS":
                            startActivity(new Intent(DashboardActivity.this,ExamsActivity.class));
                            break;
                    }
                }
            });

            sliderLayout.addSliderView(sliderView);

        }

        sliderLayout.setIndicatorAnimation(SliderLayout.Animations.SCALE_DOWN); // :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderLayout.setScrollTimeInSec(3); //set scroll delay in seconds :
        sliderLayout.animate();




    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() != R.id.logout)
            closeDrawer();

        switch (item.getItemId()) {
            case R.id.my_orders:
                startActivity(new Intent(DashboardActivity.this,MyOrdersActivity.class));
                break;
            case R.id.wishlist:
                startActivity(new Intent(DashboardActivity.this,WishlistActivity.class));
                break;
            case R.id.payments:
                  startActivity(new Intent(DashboardActivity.this,PaymentsActivity.class));
                break;

            case R.id.policies:
                startActivity(new Intent(DashboardActivity.this,PoliciesActivity.class));
                break;
            case R.id.help_and_support:
                startActivity(new Intent(DashboardActivity.this,HelpAndSupportActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(DashboardActivity.this,AboutActivity.class));
                break;
            case R.id.logout:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(DashboardActivity.this);
                builder1.setMessage("Click Yes To Logout");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                userDataRef.removeEventListener(userDataListener);
                                mAuth.signOut();
                                mGoogleSignInClient.signOut();

                                // mGoogleSignInClient.signOut();
                                Toast.makeText(DashboardActivity.this, "Successfully Signed Out", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                                finish();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder1.create();
                alert.show();
                break;
        }

        return true;
    }

    private void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();

        } else {
            if (!val) {
                Toast.makeText(DashboardActivity.this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
                val = true;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        val = false;
                    }
                }, 2000);
            } else {
                userDataRef.removeEventListener(userDataListener);
                super.onBackPressed();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it present
        getMenuInflater().inflate(R.menu.dashboard_menu,menu);
        MenuItem cart=menu.findItem(R.id.cart);
        // cart.setIcon(R.drawable.search_icon);
        cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(DashboardActivity.this,CartActivity.class));
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


}
