package com.twenty80partnership.bibliofy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.twenty80partnership.bibliofy.adapters.BookRecyclerAdapter;
import com.twenty80partnership.bibliofy.adapters.StationaryItemRecyclerAdapter;
import com.twenty80partnership.bibliofy.models.Book;
import com.twenty80partnership.bibliofy.models.StationaryItem;

import java.util.ArrayList;

public class OrderDataActivity extends AppCompatActivity {

    RecyclerView recyclerView,recyclerView2;
    RecyclerView.LayoutManager layoutManager,layoutManager2;
    BookRecyclerAdapter adapter;
    StationaryItemRecyclerAdapter stationaryItemRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Ordered Items");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //books
        recyclerView = findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new BookRecyclerAdapter();
        adapter.loadData((ArrayList<Book>) getIntent().getSerializableExtra("bookArrayList"));
        recyclerView.setAdapter(adapter);

        //stationary
        recyclerView2 = findViewById(R.id.recycler_view2);

        layoutManager2 = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(layoutManager2);

        stationaryItemRecyclerAdapter = new StationaryItemRecyclerAdapter();
        stationaryItemRecyclerAdapter.loadData((ArrayList<StationaryItem>) getIntent().getSerializableExtra("stationaryItemArrayList"));
        recyclerView2.setAdapter(stationaryItemRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
