package com.sheikhsoft.bondhu.reg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sheikhsoft.bondhu.MainActivity;
import com.sheikhsoft.bondhu.R;

import java.util.HashMap;
import java.util.Map;

public class ProfessionActivity extends AppCompatActivity {
    private Button mFinsh;
    LinearLayout student_layout,employee_layout;
    private ProgressBar mPrograssBar;

    private DatabaseReference mUserDetalisDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseUser user;
    private ProgressDialog mPrograss;

    private TextInputLayout mSchool_name;
    private TextInputLayout mCompany_name;
    private TextInputLayout mPosition;


    private String user_id,display_name,gender,birthday,location,profession,school,mcompany,mposition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profession);
        mFinsh = (Button)findViewById(R.id.profession_finish_btn);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.profassion_radioGroup);
        student_layout = (LinearLayout)findViewById(R.id.student_layout);
        employee_layout = (LinearLayout)findViewById(R.id.employee_layout);
        mSchool_name  = (TextInputLayout) findViewById(R.id.school_name_til);
        mCompany_name  = (TextInputLayout) findViewById(R.id.company_name_til);
        mPosition  = (TextInputLayout) findViewById(R.id.position_edittext_til);
        mPrograssBar = (ProgressBar)findViewById(R.id.profation_prograssbar) ;
        mPrograssBar.setProgress(100);
        mPrograss = new ProgressDialog(this);

        user_id = getIntent().getStringExtra("user_id");
        display_name = getIntent().getStringExtra("display_name");
        gender =getIntent().getStringExtra("gender");
        birthday=getIntent().getStringExtra("birthday");
        location =getIntent().getStringExtra("location");

        user = FirebaseAuth.getInstance().getCurrentUser();






        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mFinsh.setVisibility(View.VISIBLE);
                if(checkedId == R.id.radio_student){
                    student_layout.setVisibility(View.VISIBLE);
                    employee_layout.setVisibility(View.INVISIBLE);
                    profession = "Student";



                } else if (checkedId == R.id.radio_employee){
                    employee_layout.setVisibility(View.VISIBLE);
                    student_layout.setVisibility(View.INVISIBLE);
                    profession = "Employee";
                }
            }
        });

        mFinsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrograss.setTitle("Logging In");
                mPrograss.setMessage("Please Wait When We Check Your Credential");
                mPrograss.setCanceledOnTouchOutside(false);
                mPrograss.show();
                school = mSchool_name.getEditText().getText().toString();
                mcompany = mCompany_name.getEditText().getText().toString();
                mposition = mPosition.getEditText().getText().toString();


                if(!user.equals("") && !display_name.equals("") && !gender.equals("") && !profession.equals("") && !location.equals("") && !birthday.equals("") ){

                    if ((!mcompany.equals("") && !mposition.equals("")) || !school.equals("") ){
                        String current_user_id = user.getUid();
                        updateUI(current_user_id);
                    }else {
                        mPrograss.dismiss();
                        Toast.makeText(ProfessionActivity.this, "Enter Your Profession Properly", Toast.LENGTH_LONG).show();

                    }

                }
                else{
                    mPrograss.dismiss();
                    Toast.makeText(ProfessionActivity.this, "Please Fill All the Information", Toast.LENGTH_SHORT).show();
                }





            }
        });
    }

    private void updateUI(String current_user_id) {

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_user_id);
        Map update_hashMap = new HashMap();

        if (profession.equals("Student")){
            mcompany = "defult";
            mposition="defult";
        }else {
            school ="defult";
        }

        update_hashMap.put("name", display_name);
        update_hashMap.put("gender", gender);
        update_hashMap.put("birthday", birthday);
        update_hashMap.put("location",location);
        update_hashMap.put("profession",profession);
        update_hashMap.put("school",school);
        update_hashMap.put("company",mcompany);
        update_hashMap.put("position",mposition);


        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                mPrograss.dismiss();

                Intent mainIntend = new Intent(ProfessionActivity.this,MainActivity.class);
                mainIntend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntend);
                GenderActivity.genderActivity.finish();
                AgeActivity.ageActivity.finish();
                ChangeNameActivity.changeNameActivity.finish();
                LocationActivity.locationActivity.finish();
                finish();


            }
        });




    }
}
