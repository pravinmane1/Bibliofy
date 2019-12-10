package com.twenty80partnership.bibliofy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ExamsActivity extends AppCompatActivity {
    TextView online,theory;
    ImageView intro;
    DatabaseReference introRef;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);

        online=findViewById(R.id.online);
        theory=findViewById(R.id.theory);
        intro=findViewById(R.id.intro_img);

        introRef=FirebaseDatabase.getInstance().getReference("Intro/SPPU/exams/header" );

        introRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                url=dataSnapshot.getValue(String.class);
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.sample_intro)
                        .into(intro);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ExamsActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ExamsActivity.this,AvlCategoryActivity.class);
                i.putExtra("bookType","Online");
                startActivity(i);
            }
        });
        theory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ExamsActivity.this,AvlCategoryActivity.class);
                i.putExtra("bookType","Theory");
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                startActivity(new Intent(ExamsActivity.this,CartActivity.class));
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}