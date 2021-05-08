package com.twenty80partnership.bibliofy.ui.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofy.AboutActivity;
import com.twenty80partnership.bibliofy.AddressesActivity;
import com.twenty80partnership.bibliofy.CourseActivity;
import com.twenty80partnership.bibliofy.HelpAndSupportActivity;
import com.twenty80partnership.bibliofy.LoginActivity;
import com.twenty80partnership.bibliofy.PhoneNumberActivity;
import com.twenty80partnership.bibliofy.ProfileEditActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.WishlistActivity;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private TextView name, number, email;
    private ImageView userImg;
    private GoogleSignInClient mGoogleSignInClient;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        firebaseUser = mAuth.getCurrentUser();

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);


        RelativeLayout course = root.findViewById(R.id.course);
        RelativeLayout addresses = root.findViewById(R.id.addresses);
        RelativeLayout wishlist = root.findViewById(R.id.wishlist);
        RelativeLayout helpAndSupport = root.findViewById(R.id.help_and_support);
        RelativeLayout referAndEarn = root.findViewById(R.id.refer_and_earn);
        RelativeLayout aboutUs = root.findViewById(R.id.about_us);
        RelativeLayout userSettings = root.findViewById(R.id.profile);
        userImg = root.findViewById(R.id.user);
        RelativeLayout logout = root.findViewById(R.id.logout);


        name = root.findViewById(R.id.name);
        number = root.findViewById(R.id.number);
        email = root.findViewById(R.id.email);

        Glide.with(getActivity()).load(R.drawable.profile_boy).apply(RequestOptions.circleCropTransform()).into(userImg);


        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent(getActivity(), PhoneNumberActivity.class);
                phoneIntent.putExtra("loginFlow","no");
                startActivityForResult(phoneIntent,3);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setMessage("Click Yes To Logout");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                mGoogleSignInClient.signOut();
                                Toast.makeText(getActivity(), "Successfully Signed Out", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
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
            }
        });

        userSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivityForResult(profileIntent, 1);
            }
        });

        course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseIntent = new Intent(getActivity(), CourseActivity.class);
                courseIntent.putExtra("loginFlow", "no");
                startActivity(courseIntent);
            }
        });

        addresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddressesActivity.class));
            }
        });

        wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WishlistActivity.class));
            }
        });

        helpAndSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HelpAndSupportActivity.class));
            }
        });

        referAndEarn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseUser != null) {

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("gender");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String gender = dataSnapshot.getValue(String.class);

                        if (gender.equals("male")) {
                            Glide.with(getActivity()).load(R.drawable.profile_boy).apply(RequestOptions.circleCropTransform()).into(userImg);
                        } else {
                            Glide.with(getActivity()).load(R.drawable.profile_girl).apply(RequestOptions.circleCropTransform()).into(userImg);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d("Profiledebug", "user is set");
            Log.d("Profiledebug", "display name : " + firebaseUser.getDisplayName());

            if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().equals("")) {
                Log.d("Profiledebug", "display name inside if : " + firebaseUser.getDisplayName());

                name.setText(firebaseUser.getDisplayName());
            } else {
                Log.d("Profiledebug", "display name inside else : " + firebaseUser.getDisplayName());

                name.setText("Set User Name >");
            }

            if (firebaseUser.getEmail() != null && !firebaseUser.getEmail().equals("")) {
                email.setText(firebaseUser.getEmail());
            } else {
                email.setText("Email is not set");
            }


            for (UserInfo profile : firebaseUser.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                if (providerId.equals("phone")) {

                    String phone = profile.getPhoneNumber();

                    number.setText(phone);
                    break;
                } else {
                    number.setText("Add number >");
                }
                // UID specific to the provider
                String uid = profile.getUid();

                // Name, email address, and profile photo Url
//                String name = profile.getDisplayName();
//                String email = profile.getEmail();
//                String phone = profile.getPhoneNumber();
//
//                Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.d("Profiledebug", "req is 1");
            mAuth = FirebaseAuth.getInstance();
            firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("gender");

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String gender = dataSnapshot.getValue(String.class);

                            if (gender.equals("male")) {
                                Glide.with(getActivity()).load(R.drawable.profile_boy).apply(RequestOptions.circleCropTransform()).into(userImg);
                            } else {
                                Glide.with(getActivity()).load(R.drawable.profile_girl).apply(RequestOptions.circleCropTransform()).into(userImg);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Log.d("Profiledebug", "user is set");
                Log.d("Profiledebug", "display name : " + firebaseUser.getDisplayName());

                if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().equals("")) {
                    Log.d("Profiledebug", "display name inside if : " + firebaseUser.getDisplayName());

                    name.setText(firebaseUser.getDisplayName());
                } else {
                    Log.d("Profiledebug", "display name inside else : " + firebaseUser.getDisplayName());

                    name.setText("Set User Name >");
                }

                if (firebaseUser.getEmail() != null && !firebaseUser.getEmail().equals("")) {
                    email.setText(firebaseUser.getEmail());
                } else {
                    email.setText("Email is not set");
                }


                for (UserInfo profile : firebaseUser.getProviderData()) {
                    // Id of the provider (ex: google.com)
                    String providerId = profile.getProviderId();

                    if (providerId.equals("phone")) {

                        String phone = profile.getPhoneNumber();

                        number.setText(phone);
                        break;
                    } else {
                        number.setText("Add number >");
                    }
                    // UID specific to the provider
                    String uid = profile.getUid();

                    // Name, email address, and profile photo Url
//                String name = profile.getDisplayName();
//                String email = profile.getEmail();
//                String phone = profile.getPhoneNumber();
//
//                Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
                }
            } else {
                Toast.makeText(getActivity(), "data isn ull", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode==3){
            if (resultCode == RESULT_OK){
                updatePhone();
                Log.d("userdata","updating phone");
            }
        }
    }

    private void updatePhone() {
        Log.d("userdata","setPhoneProvider is called from requestcode 3");

        boolean isPhone=false;

        for (UserInfo profile : firebaseUser.getProviderData()) {
            // Id of the provider (ex: google.com)
            String providerId = profile.getProviderId();


            if(providerId.equals("phone")){
                isPhone=true;
                String phone = profile.getPhoneNumber();
                number.setText(phone);
                break;
            }


            // UID specific to the provider
            String uid = profile.getUid();

            // Name, email address, and profile photo Url
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            Log.d("userdata","Provider id:"+providerId+" uid:"+uid+"name :"+name+" email:"+email+" phone"+phone);
        }

        if (!isPhone){
            number.setText("Add number >");
        }
        // [END get_provider_data]
    }
}