package com.twenty80partnership.bibliofy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.holders.OrderMessageViewHolder;
import com.twenty80partnership.bibliofy.models.OrderMessage;


public class OrderHelpActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages2";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private GoogleSignInClient mSignInClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<OrderMessage, OrderMessageViewHolder>
            mFirebaseAdapter;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_help);


        orderId = getIntent().getStringExtra("orderId");

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        //mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<OrderMessage> parser = new SnapshotParser<OrderMessage>() {
            @Override
            public OrderMessage parseSnapshot(DataSnapshot dataSnapshot) {
                OrderMessage friendlyMessage = dataSnapshot.getValue(OrderMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child("Chats").child("OrderChats").child(mFirebaseAuth.getUid())
                .child(orderId);

        FirebaseRecyclerOptions<OrderMessage> options =
                new FirebaseRecyclerOptions.Builder<OrderMessage>()
                        .setQuery(messagesRef, parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<OrderMessage, OrderMessageViewHolder>(options) {
            @Override
            public OrderMessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new OrderMessageViewHolder(inflater.inflate(R.layout.order_message_row, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final OrderMessageViewHolder viewHolder,
                                            int position,
                                            OrderMessage friendlyMessage) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);



                if (friendlyMessage.getId()!=null){


                    if (!friendlyMessage.getUid().equals(mFirebaseAuth.getUid())){

                        Log.d("mainactivitydebu",friendlyMessage.getId()+" "+mFirebaseAuth.getUid());

                        viewHolder.llOne.setVisibility(View.VISIBLE);
                        viewHolder.llTwo.setVisibility(View.GONE);

                        if (friendlyMessage.getText() != null) {
                            viewHolder.messageTextView.setText(friendlyMessage.getText());
                            viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                            viewHolder.messageImageView.setVisibility(ImageView.GONE);
                        } else if (friendlyMessage.getImageUrl() != null) {
                            String imageUrl = friendlyMessage.getImageUrl();
                            if (imageUrl.startsWith("gs://")) {
                                StorageReference storageReference = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(imageUrl);
                                storageReference.getDownloadUrl().addOnCompleteListener(
                                        new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    String downloadUrl = task.getResult().toString();
                                                    Glide.with(viewHolder.messageImageView.getContext())
                                                            .load(downloadUrl)
                                                            .into(viewHolder.messageImageView);
                                                } else {
                                                    Log.w(TAG, "Getting download url was not successful.",
                                                            task.getException());
                                                }
                                            }
                                        });
                            } else {
                                Glide.with(viewHolder.messageImageView.getContext())
                                        .load(friendlyMessage.getImageUrl())
                                        .into(viewHolder.messageImageView);
                            }
                            viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                            viewHolder.messageTextView.setVisibility(TextView.GONE);
                        }
                    }
                    else{
                        viewHolder.llOne.setVisibility(View.GONE);
                        viewHolder.llTwo.setVisibility(View.VISIBLE);

                        if (friendlyMessage.getText() != null) {
                            viewHolder.messageTextView2.setText(friendlyMessage.getText());
                            viewHolder.messageTextView2.setVisibility(TextView.VISIBLE);
                            viewHolder.messageImageView2.setVisibility(ImageView.GONE);
                        } else if (friendlyMessage.getImageUrl() != null) {
                            String imageUrl = friendlyMessage.getImageUrl();
                            if (imageUrl.startsWith("gs://")) {
                                StorageReference storageReference = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(imageUrl);
                                storageReference.getDownloadUrl().addOnCompleteListener(
                                        new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    String downloadUrl = task.getResult().toString();
                                                    Glide.with(viewHolder.messageImageView2.getContext())
                                                            .load(downloadUrl)
                                                            .into(viewHolder.messageImageView2);
                                                } else {
                                                    Log.w(TAG, "Getting download url was not successful.",
                                                            task.getException());
                                                }
                                            }
                                        });
                            } else {
                                Glide.with(viewHolder.messageImageView2.getContext())
                                        .load(friendlyMessage.getImageUrl())
                                        .into(viewHolder.messageImageView2);
                            }
                            viewHolder.messageImageView2.setVisibility(ImageView.VISIBLE);
                            viewHolder.messageTextView2.setVisibility(TextView.GONE);
                        }
                    }
                }





                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    Glide.with(OrderHelpActivity.this)
                            .load(R.drawable.user)
                            .into(viewHolder.messengerImageView);
                } else {
                    Glide.with(OrderHelpActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderMessage friendlyMessage = new
                        OrderMessage(mFirebaseAuth.getUid(),mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null /* no image */);

                mFirebaseDatabaseReference.child("Chats").child("OrderChats").child(mFirebaseAuth.getUid())
                        .child(orderId).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    OrderMessage tempMessage = new OrderMessage(mFirebaseAuth.getUid(),null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(mFirebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(OrderHelpActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(OrderHelpActivity.this,
                                            new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        OrderMessage friendlyMessage =
                                                                new OrderMessage(mFirebaseAuth.getUid(),null, mUsername, mPhotoUrl,
                                                                        task.getResult().toString());
                                                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                                                                .setValue(friendlyMessage);
                                                    }
                                                }
                                            });
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }

}
