package com.twenty80partnership.bibliofy.ui.home;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.AvlCategoryActivity;
import com.twenty80partnership.bibliofy.BookListActivity;
import com.twenty80partnership.bibliofy.CartActivity;
import com.twenty80partnership.bibliofy.ExtraCategoryActivity;
import com.twenty80partnership.bibliofy.HomeCoursesActivity;
import com.twenty80partnership.bibliofy.HomeStationaryActivity;
import com.twenty80partnership.bibliofy.ProfileEditActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.StationaryActivity;
import com.twenty80partnership.bibliofy.StationaryItemsActivity;
import com.twenty80partnership.bibliofy.adapters.SliderAdapter;
import com.twenty80partnership.bibliofy.holders.MenuItemViewHolder;
import com.twenty80partnership.bibliofy.models.MenuItem1;
import com.twenty80partnership.bibliofy.models.SliderItem;
import com.twenty80partnership.bibliofy.models.User;
import com.twenty80partnership.bibliofy.services.ListenOrder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference userDataRef;
    private FirebaseDatabase db;

    RecyclerView itemList,rvYMAL;
    private ViewPager2 viewPager2;

    private ImageView appIcon,ivAllCourses;
    private Handler sliderHandler = new Handler();

    private  FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder> firebaseRecyclerAdapter,rymalFRA;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        Log.d("debugLoad", "oncreate of fragment");

        db = FirebaseDatabase.getInstance();

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        appIcon = root.findViewById(R.id.app_icon);
        rvYMAL = root.findViewById(R.id.rv_ymal);
        ivAllCourses = root.findViewById(R.id.iv_all_courses);

        appIcon.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileEditActivity.class));
        });

        ivAllCourses.setOnClickListener( v -> {
            Intent courseBookIntent = new Intent(getActivity(), HomeCoursesActivity.class);

            courseBookIntent.putExtra("uni", "sppu");
            startActivity(courseBookIntent);
        });



        DatabaseReference YouMayAlsoLikeMenu = db.getReference("YouMayAlsoLikeMenu").child("SPPU");

        rvYMAL.setHasFixedSize(false);
        rvYMAL.setLayoutManager(new GridLayoutManager(getActivity(), 4));

        Query query1 = YouMayAlsoLikeMenu.orderByChild("priority");

        firebaseSearchYouMayAlsoLike(query1);

        //////////////////////////////////////////////////////
        viewPager2 = root.findViewById(R.id.view_pager_image_slider);


        final List<SliderItem> sliderItems = new ArrayList<>();

        DatabaseReference SliderRef = FirebaseDatabase.getInstance().getReference("Slider").child("SPPU");

        SliderRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    SliderItem sliderItem = ds.getValue(SliderItem.class);

                    sliderItems.add(sliderItem);
                }

                viewPager2.setAdapter(new SliderAdapter(sliderItems, viewPager2, getContext()));
                viewPager2.setClipToPadding(false);
                viewPager2.setClipChildren(false);
                viewPager2.setOffscreenPageLimit(3);
                viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                compositePageTransformer.addTransformer(new MarginPageTransformer(40));

                viewPager2.setPageTransformer(compositePageTransformer);

                viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        sliderHandler.removeCallbacks(sliderRunnable);
                        sliderHandler.postDelayed(sliderRunnable, 5000);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ////////////////////////////////////////////////


        Intent service = new Intent(getActivity(), ListenOrder.class);
        getActivity().startService(service);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference homeMenuRef = FirebaseDatabase.getInstance().getReference("HomeMenu")
                .child("SPPU");

        Log.d("debugLoad2", "before loading");


        itemList = root.findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(getActivity(), 4));

        Log.d("debugLoad", "grid layout set");


        Query query = homeMenuRef.orderByChild("priority");

        firebaseSearch(query);

        //testing
        TextView c = root.findViewById(R.id.course);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cIntent = new Intent(getActivity(), ExtraCategoryActivity.class);
                cIntent.putExtra("loginFlow", "no");
                startActivity(cIntent);
            }
        });


        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("loading");
        progressDialog.setCancelable(false);


        //taking system time
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date currentTime = Calendar.getInstance().getTime();
        Long date = Long.parseLong(dateFormat.format(currentTime));


        DatabaseReference lastUsed = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("lastOpened");
        lastUsed.setValue(date);


        Button bookCard = root.findViewById(R.id.book);
        Button examCard = root.findViewById(R.id.exam);
        ImageView cart = root.findViewById(R.id.cart);
        Button allBooks = root.findViewById(R.id.allbooks);
        Button allStationary = root.findViewById(R.id.allstationary);


        CardView cvSpecific = root.findViewById(R.id.cv_specific);

        cvSpecific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseBookIntent = new Intent(getActivity(), BookListActivity.class);

                courseBookIntent.putExtra("userCourse", "true");
                courseBookIntent.putExtra("category", "regular");
                startActivity(courseBookIntent);
            }
        });


        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        String id = mAuth.getCurrentUser().getUid();
        userDataRef = FirebaseDatabase.getInstance().getReference("Users").child(id);

        //if user is not created then create
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child("email").exists()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    User user = new User(
                            firebaseUser.getDisplayName(),
                            firebaseUser.getEmail(),
                            firebaseUser.getPhotoUrl().toString(),
                            0,
                            null,
                            mAuth.getCurrentUser().getUid(),
                            firebaseUser.getDisplayName().toLowerCase());
                    userDataRef.setValue(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "userDataListerner" + databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
                }

            }
        };

        if (!isAnonymous()) {
            userDataRef.addValueEventListener(userDataListener);
        }

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CartActivity.class));
            }
        });

        bookCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent courseBookIntent = new Intent(getActivity(), BookListActivity.class);

                courseBookIntent.putExtra("userCourse", "true");
                courseBookIntent.putExtra("category", "regular");
                startActivity(courseBookIntent);

            }
        });

        examCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseBookIntent = new Intent(getActivity(), BookListActivity.class);

                courseBookIntent.putExtra("userCourse", "true");
                courseBookIntent.putExtra("category", "used");
                startActivity(courseBookIntent);
            }
        });

        allBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseBookIntent = new Intent(getActivity(), HomeCoursesActivity.class);

                courseBookIntent.putExtra("uni", "sppu");
                startActivity(courseBookIntent);
            }
        });

        allStationary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseBookIntent = new Intent(getActivity(), HomeStationaryActivity.class);

                courseBookIntent.putExtra("uni", "sppu");
                startActivity(courseBookIntent);
            }
        });

        return root;
    }

    public void firebaseSearch(Query q) {

        FirebaseRecyclerOptions<MenuItem1> options = new FirebaseRecyclerOptions.Builder<MenuItem1>()
                .setQuery(q,MenuItem1.class)
                .build();

        firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<MenuItem1, MenuItemViewHolder>(options) {

            @NonNull
            @Override
            public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item_row,parent,false);

                return new MenuItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuItemViewHolder viewHolder, int position, @NonNull MenuItem1 model) {

                Log.d("debugLoad", "populateViewHolder called for " + model.getId());
                viewHolder.mView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_transition_animation));


                viewHolder.setDetails(model.getTop(), model.getImg(), model.getTitle());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(getActivity(), model.getTitle(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getType().equals("books")) {
                            Intent bookIntent = new Intent(getActivity(), AvlCategoryActivity.class);
                            bookIntent.putExtra("course", model.getCourse());
                            bookIntent.putExtra("bookType", model.getCategory());
                            bookIntent.putExtra("sem", model.getSem());
                            startActivity(bookIntent);
                        } else if (model.getType().equals("stationary")) {
                            Intent stationaryIntent = new Intent(getActivity(), StationaryItemsActivity.class);
                            stationaryIntent.putExtra("stationaryId", model.getCategory());
                            stationaryIntent.putExtra("categoryName", model.getTitle());
                            startActivity(stationaryIntent);
                        }
                    }
                });


            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        Log.d("debugLoad", "itemList setadapter is called");

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

        if (count == 1) {
            return true;
        }
        return false;

    }


    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
        }


    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager2.setCurrentItem(0);
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    public void firebaseSearchYouMayAlsoLike(Query q) {

        FirebaseRecyclerOptions<MenuItem1> options  = new FirebaseRecyclerOptions.Builder<MenuItem1>()
                .setQuery(q,MenuItem1.class)
                .build();

        rymalFRA
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
                viewHolder.mView.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_transition_animation));


                viewHolder.setDetailsYMAL(model.getTop(), model.getImg(), model.getTitle(),model.getType());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(getActivity(), model.getTitle(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getType().equals("books")) {
                            Intent bookIntent = new Intent(getActivity(), BookListActivity.class);
                            bookIntent.putExtra("course", model.getCourse());
                            bookIntent.putExtra("category", model.getCategory());
                            bookIntent.putExtra("sem", model.getSem());
                            startActivity(bookIntent);
                        } else if (model.getType().equals("stationary")) {
                            Intent stationaryIntent = new Intent(getActivity(), StationaryItemsActivity.class);
                            stationaryIntent.putExtra("stationaryId", model.getCategory());
                            stationaryIntent.putExtra("categoryName", model.getTitle());
                            stationaryIntent.putExtra("source","cart");
                            startActivity(stationaryIntent);
                        } else if (model.getType().equals("more")){
                            Intent selectAddressIntent = new Intent(getActivity(), StationaryActivity.class);
                            startActivity(selectAddressIntent);
                        }
                    }
                });

            }
        };


        rvYMAL.setAdapter(rymalFRA);
        rymalFRA.startListening();
        Log.d("debugLoad", "itemList setadapter is called");

    }

    @Override
    public void onDestroy() {

        if (rymalFRA!=null)
            rymalFRA.stopListening();

        if (firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();

        super.onDestroy();
    }
}