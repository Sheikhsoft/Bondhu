package com.sheikhsoft.bondhu;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private DatabaseReference mRequestDatabase;
    private DatabaseReference mUsersDatabase;

    private RecyclerView mRequestList;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private String mRequestType;

    private Button mAcceptBtn;
    private DatabaseReference mRootRef;

    private Button mCancelBtn;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request").child(mCurrent_user_id);
        mRequestDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRootRef = FirebaseDatabase.getInstance().getReference();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestFragment.RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestFragment.RequestsViewHolder>(

                Requests.class,
                R.layout.requests_single_suser_layout,
                RequestFragment.RequestsViewHolder.class,
                mRequestDatabase
        ){
            @Override
            protected void populateViewHolder(final RequestFragment.RequestsViewHolder requestsViewHolder, Requests requests, int position) {

                mRequestType = requests.getRequest_type();
                requestsViewHolder.setStatus(mRequestType);
                final String list_user_id = getRef(position).getKey();

                if (!mRequestType.equals("send")) {
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String username = dataSnapshot.child("name").getValue().toString();
                            final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            requestsViewHolder.setName(username);
                            requestsViewHolder.setImage(userThumb);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mAcceptBtn = (Button)requestsViewHolder.mView.findViewById(R.id.request_accept_btn);
                    mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestAccept(list_user_id);
                        }
                    });

                    mCancelBtn =(Button)requestsViewHolder.mView.findViewById(R.id.request_single_cancle_btn);
                    mCancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancelRequest(list_user_id);
                        }
                    });



                }
            }
        };


            mRequestList.setAdapter(requestsRecyclerViewAdapter);



    }

    private void cancelRequest(String list_user_id) {

        final String requestUserId = list_user_id;
        mRootRef.child("friend_request").child(mCurrent_user_id).child(requestUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mRootRef.child("friend_request").child(requestUserId).child(mCurrent_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Friend Request Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestAccept(String listUserId) {
        final String current_date = DateFormat.getDateTimeInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("friends/"+mCurrent_user_id+"/"+listUserId+"/date",current_date);
        friendsMap.put("friends/"+listUserId+"/"+mCurrent_user_id+"/date",current_date);

        friendsMap.put("friend_request/"+mCurrent_user_id+"/"+listUserId,null);
        friendsMap.put("friend_request/"+listUserId+"/"+mCurrent_user_id,null);


        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    Toast.makeText(getContext(), "Friend request Acepted", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static class RequestsViewHolder  extends RecyclerView.ViewHolder{
        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.request_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){

            TextView userStatusView = (TextView) mView.findViewById(R.id.request_single_message);

            RelativeLayout userRelativeLayout = (RelativeLayout) mView.findViewById(R.id.request_single_layout);
            if (status.equals("send")){
                userRelativeLayout.removeAllViews();


            }else {
                userStatusView.setText(status);
            }
        }

        public void setImage(String image){

            CircleImageView mCircleImaheView = (CircleImageView) mView.findViewById(R.id.request_single_image);
            if (!image.equals("defalt")){
                Picasso.with(mView.getContext()).load(image).placeholder(R.drawable.default_man).into(mCircleImaheView);
            }


        }
    }
}
