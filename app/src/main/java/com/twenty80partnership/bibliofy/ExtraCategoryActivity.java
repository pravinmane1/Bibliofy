package com.twenty80partnership.bibliofy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twenty80partnership.bibliofy.models.RequestBook;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExtraCategoryActivity extends AppCompatActivity {

    private Button request;
    private EditText course, branch, currentyear, bookName;
    private ProgressDialog pd;

    private FirebaseAuth mAuth;
    private DatabaseReference requestRef;

    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(ExtraCategoryActivity.this);

        pd.setMessage("Requesting your book");
        pd.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        requestRef = FirebaseDatabase.getInstance().getReference("RequestedBooks").child(mAuth.getCurrentUser().getUid());

        course = findViewById(R.id.course_name);
        branch = findViewById(R.id.branch_name);
        currentyear = findViewById(R.id.current_year);
        bookName = findViewById(R.id.book_name);
        request = findViewById(R.id.request);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bookName.getText().toString().isEmpty()) {
                    bookName.setError("Book Name Can't be empty");
                    return;
                }
                pd.show();

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

                currentDate = Calendar.getInstance().getTime();

                String date = dateFormat.format(currentDate);

                RequestBook requestBook = new RequestBook(course.getText().toString(),
                        branch.getText().toString(),
                        currentyear.getText().toString(),
                        bookName.getText().toString(),
                        date);

                requestRef.child(date).setValue(requestBook).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {


                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ExtraCategoryActivity.this);
                            builder1.setMessage("Your request for book " + bookName.getText().toString() + " has been done");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "Request another book",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            course.setText("");
                                            branch.setText("");
                                            currentyear.setText("");
                                            bookName.setText("");
                                            course.requestFocus();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                                        }
                                    });

                            builder1.setNegativeButton(
                                    "Quit",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder1.create();
                            alert.show();
                        } else {
                            Toast.makeText(ExtraCategoryActivity.this, "Something Went Wrong!! try again", Toast.LENGTH_SHORT).show();
                        }
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

}
