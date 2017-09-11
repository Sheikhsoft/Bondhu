package com.sheikhsoft.bondhu;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.sheikhsoft.bondhu.Utils.BottomNavigationViewHelper;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG ="UsersActivity";

    private Toolbar mToolBar;
    private RecyclerView mUserList;
    private FirebaseAuth mAuth;

    private DatabaseReference mUsersDatabase;

    private Context condext = UsersActivity.this;
    private static final int ACTIVITY_NUM = 1;
    private SearchView mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolBar = (Toolbar)findViewById(R.id.users_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");




        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser !=null) {

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        }


        mUserList = (RecyclerView)findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        setupBottomNavigationView();




    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UserViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder userViewHolder, Users users, int position) {

                if (users.getName()!=null && users.getStatus()!=null  && users.getThumb_image()!=null ){
                    userViewHolder.setName(users.getName());
                    userViewHolder.setStatus(users.getStatus());
                    userViewHolder.setImage(users.getThumb_image());
                    final String user_id = getRef(position).getKey();

                    userViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                            profileIntent.putExtra("user_id",user_id);
                            startActivity(profileIntent);
                        }
                    });
                }

                else {
                    userViewHolder.mView.setVisibility(View.INVISIBLE);
                }

            }
        };

        mUserList.setAdapter(firebaseRecyclerAdapter);

    }



    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){


                TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
                userNameView.setText(name);




        }

        public void setStatus(String status){

                TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
                userStatusView.setText(status);

        }

        public void setImage(String image){



            CircleImageView mCircleImaheView = (CircleImageView) mView.findViewById(R.id.user_single_image);

                if (!image.equals("defalt")) {
                    Picasso.with(mView.getContext()).load(image).placeholder(R.drawable.default_man).into(mCircleImaheView);
                }



        }
    }

    private void setupBottomNavigationView(){

        Log.d(TAG,"Setup Bottom Navigation View");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(UsersActivity.this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        mSearch = (SearchView) MenuItemCompat.getActionView(item);
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query Q = mUsersDatabase.orderByChild("name").startAt(newText).endAt(newText+"\uf8ff");

                FirebaseRecyclerAdapter<Users,UserViewHolder> mAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                        Users.class,
                        R.layout.users_single_layout,
                        UserViewHolder.class,
                        Q
                ){
                    @Override
                    protected void populateViewHolder(UserViewHolder userViewHolder, Users users, int position) {

                        if (users.getName()!=null && users.getStatus()!=null  && users.getThumb_image()!=null ){
                            userViewHolder.setName(users.getName());
                            userViewHolder.setStatus(users.getStatus());
                            userViewHolder.setImage(users.getThumb_image());
                            final String user_id = getRef(position).getKey();

                            userViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                                    profileIntent.putExtra("user_id",user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }

                        else {
                            userViewHolder.mView.setVisibility(View.INVISIBLE);
                        }

                    }

                };
                mUserList.setAdapter(mAdapter);

                return true;
            }
        });


        return true;
    }
}
