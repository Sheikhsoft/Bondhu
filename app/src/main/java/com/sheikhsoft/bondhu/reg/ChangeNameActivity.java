package com.sheikhsoft.bondhu.reg;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sheikhsoft.bondhu.R;

public class ChangeNameActivity extends AppCompatActivity {

    private Button mContunue;
    public static Activity changeNameActivity;
    private EditText mDisplayName;
    private DatabaseReference mUserDatabase;
    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
        changeNameActivity = this;
        user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mContunue = (Button)findViewById(R.id.change_name_continue_btn);
        mDisplayName =(EditText)findViewById(R.id.reg_display_name);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.change_name_prograsebar);
        progressBar.setProgress(20);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String image_url = dataSnapshot.child("name").getValue().toString();
                mDisplayName.setText(display_name);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().trim().length()==0){
                    mContunue.setVisibility(View.INVISIBLE);
                    mContunue.setEnabled(false);
                } else {
                    mContunue.setVisibility(View.VISIBLE);
                    mContunue.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length()==0){
                    mContunue.setVisibility(View.INVISIBLE);
                    mContunue.setEnabled(false);
                } else {
                    mContunue.setVisibility(View.VISIBLE);
                    mContunue.setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.toString().trim().length()==0){
                    mContunue.setVisibility(View.INVISIBLE);
                    mContunue.setEnabled(false);
                } else {
                    mContunue.setVisibility(View.VISIBLE);
                    mContunue.setEnabled(true);
                }

            }
        });

        mContunue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getText().toString();

                if (displayName.trim().length()!=0) {
                    Intent intent = new Intent(ChangeNameActivity.this, GenderActivity.class);
                    intent.putExtra("display_name",displayName);
                    intent.putExtra("user_id",user_id);

                    startActivity(intent);
                }
                else {
                    Toast.makeText(ChangeNameActivity.this, "Please Enter Your Display Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }




}
