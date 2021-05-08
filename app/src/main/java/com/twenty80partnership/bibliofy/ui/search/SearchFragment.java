package com.twenty80partnership.bibliofy.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofy.CartActivity;
import com.twenty80partnership.bibliofy.CommonActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.holders.BookViewHolder;
import com.twenty80partnership.bibliofy.models.CartItem;
import com.twenty80partnership.bibliofy.models.SearchIndex;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SearchFragment extends Fragment {

    RecyclerView itemList;
    MaterialSearchBar materialSearchBar;
    private Query query;
    DatabaseReference indexRef;
    private DatabaseReference cartRequestRef;
    FirebaseAuth mAuth;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
    private  FirebaseRecyclerAdapter<SearchIndex, BookViewHolder> firebaseRecyclerAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_search, container, false);

        materialSearchBar = root.findViewById(R.id.search_bar);
        itemList = root.findViewById(R.id.recycler_view);

        materialSearchBar.performClick();

        mAuth = FirebaseAuth.getInstance();

        indexRef = FirebaseDatabase.getInstance().getReference("SearchIndex/SPPU");
        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(mAuth.getUid());

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.d("indexsearch", "search conf." + " " + text.toString());

                hideKeyboard(getActivity());
                if (!text.toString().equals("")) {
                    query = indexRef.orderByKey().startAt(text.toString().toLowerCase()).endAt(text.toString().toLowerCase() + "\uf8ff");
                    firebaseSearch(query);
                }

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        itemList.setLayoutManager(mLayoutManager);


        return root;
    }

    public void firebaseSearch(Query q) {

        FirebaseRecyclerOptions<SearchIndex> options = new FirebaseRecyclerOptions.Builder<SearchIndex>()
                .setQuery(q,SearchIndex.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SearchIndex, BookViewHolder>(options) {


            @NonNull
            @Override
            public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_row,parent,false);

                return new BookViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BookViewHolder viewHolder, int position, @NonNull SearchIndex model) {

                //reset the viewholder before getting data from the countdata
                viewHolder.removeItem.setVisibility(View.GONE);
                viewHolder.add.setText("ADD");
                viewHolder.add.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                if (model.getAvailability()) {
                    viewHolder.add.setBackgroundResource(R.drawable.borderless_colored);
                } else {
                    viewHolder.add.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray));
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
                                Toast.makeText(getActivity(), "We're sorry your item is not available at moment", Toast.LENGTH_SHORT).show();
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
                                viewHolder.add.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray));

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });


                //set details of book to card
                viewHolder.setDetails(model.getName(), model.getAuthor(), model.getPublication(), model.getImg(),
                        model.getOriginalPrice(), model.getDiscountedPrice(), model.getDiscount(),
                        model.getAvailability(), getContext(), "book", model.getVisibility(), 1);


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
                            cartItem.setItemLocation("SPPUbooks/" + model.getCourse() + "/" + model.getCategory()+"/" + model.getCombination());
                            cartItem.setQuantity(1);
                            cartItem.setType(model.getType());

                            cartRequestRef.child(id).setValue(cartItem);

                            viewHolder.add.setText("ADDED");
                            viewHolder.removeItem.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getActivity(), "We'll be back soon with this item", Toast.LENGTH_SHORT).show();
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
                            viewHolder.add.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_gray));
                        }

                    }
                });

            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        //inflate the menu; this adds items to the action bar if it present
        inflater.inflate(R.menu.cart_only_menu, menu);

        MenuItem cart = menu.findItem(R.id.cart);

        cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(getActivity(), CartActivity.class));
                return false;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CommonActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setHasOptionsMenu(true);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onDestroy() {
        firebaseRecyclerAdapter.stopListening();
        super.onDestroy();
    }
}