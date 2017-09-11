package com.sheikhsoft.bondhu.reg;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sheikhsoft.bondhu.R;

public class GenderActivity extends AppCompatActivity {
    private Button mContunue;
    public static Activity genderActivity;
    private RadioButton mMale;
    private RadioButton mFemale;
    private String gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);
        genderActivity = this;

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
        mMale = (RadioButton)findViewById(R.id.male_radio_btn) ;
        mFemale = (RadioButton)findViewById(R.id.female_radio_btn) ;
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.gender_prograss_bar);
        progressBar.setProgress(40);



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.male_radio_btn){
                    mContunue.setVisibility(View.VISIBLE);
                    mMale.setTextColor(getResources().getColor(R.color.white));
                    mFemale.setTextColor(getResources().getColor(R.color.gray));
                    gender ="Male";
                } else {
                    mContunue.setVisibility(View.VISIBLE);
                    mFemale.setTextColor(getResources().getColor(R.color.white));
                    mMale.setTextColor(getResources().getColor(R.color.gray));
                    gender ="Female";
                }
            }
        });

        mContunue = (Button)findViewById(R.id.gender_contunue_btn);
        mContunue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GenderActivity.this,AgeActivity.class);
                intent.putExtra("user_id",getIntent().getStringExtra("user_id"));
                intent.putExtra("display_name",getIntent().getStringExtra("display_name"));
                intent.putExtra("gender",gender);
                startActivity(intent);
            }
        });
    }
}
