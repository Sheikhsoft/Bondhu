package com.sheikhsoft.bondhu;

import android.content.Intent;
import android.nfc.Tag;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.sheikhsoft.bondhu.Utils.BottomNavigationViewHelper;
import com.sheikhsoft.bondhu.reg.ChangeNameActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDetailsDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mToolbar;

    private ViewPager mViewPager;

    private SectionsPagerAdepter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    private static final int ACTIVITY_NUM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"onCreate: Starting");

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser !=null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        }

       // mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Bondhu Chat");

        mViewPager = (ViewPager) findViewById(R.id.main_tab_pager);

        mSectionsPagerAdapter = new SectionsPagerAdepter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        mTabLayout.setupWithViewPager(mViewPager);
        setupBottomNavigationView();
        //mAuth.signOut();





    }

    @Override
    public void onStart() {
        super.onStart();



        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }else {

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String gender = dataSnapshot.child("gender").getValue().toString();

                    if (gender.equals("defult") ){
                        Intent regIntent = new Intent(MainActivity.this,ChangeNameActivity.class);
                        regIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        regIntent.putExtra("user_id",currentUser.getUid());
                        startActivity(regIntent);
                        finish();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


           mUserDatabase.child("online").setValue("true");


        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }



    private void setupBottomNavigationView(){

       Log.d(TAG,"Setup Bottom Navigation View");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(MainActivity.this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
}
