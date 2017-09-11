package com.sheikhsoft.bondhu.reg;

import android.app.Activity;


import android.app.DatePickerDialog;
import android.app.Dialog;

import android.content.Intent;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import com.sheikhsoft.bondhu.R;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Locale;

import br.com.sapereaude.maskedEditText.MaskedEditText;

public class AgeActivity extends AppCompatActivity {
    public static Activity ageActivity;
    private Button mContunue;
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private MaskedEditText mbirdatDate;
    private ProgressBar mprograssbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age);
        ageActivity=this;
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mContunue=(Button)findViewById(R.id.age_contunue_btn);
        mbirdatDate = (MaskedEditText)findViewById(R.id.birday_edit_text);
        mprograssbar = (ProgressBar)findViewById(R.id.age_progressBar) ;
        mprograssbar.setProgress(60);

        mbirdatDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.show();

            }
        });
        mContunue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AgeActivity.this,LocationActivity.class);
                intent.putExtra("user_id",getIntent().getStringExtra("user_id"));
                intent.putExtra("display_name",getIntent().getStringExtra("display_name"));
                intent.putExtra("gender",getIntent().getStringExtra("gender"));
                intent.putExtra("birthday",mbirdatDate.getText().toString());
                startActivity(intent);

            }
        });

        setDateTimeField();


    }

    private void setDateTimeField() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.YEAR, -35);



        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();


                newDate.set(year, monthOfYear, dayOfMonth);
                mbirdatDate.setText(dateFormatter.format(newDate.getTime()));
                mContunue.setVisibility(View.VISIBLE);
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));




    }


}


