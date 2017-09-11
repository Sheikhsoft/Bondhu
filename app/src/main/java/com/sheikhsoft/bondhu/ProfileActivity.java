package com.sheikhsoft.bondhu;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileNAme,mProfileStatus,mProfileFriendCount;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mPrograssDialog;

    private String mCurrrent_state;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mProfileNAme = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendCount = (TextView) findViewById(R.id.profile_friend_number);
        mProfileSendReqBtn =(Button)findViewById(R.id.profile_friend_send_req_brn);
        mProfileDeclineBtn =(Button)findViewById(R.id.profile_decline_req_btn);



        mCurrrent_state ="not_friends";

        mPrograssDialog = new ProgressDialog(this);
        mPrograssDialog.setTitle("Loding User Data");
        mPrograssDialog.setMessage("Please Wait While We Load User Data");
        mPrograssDialog.setCanceledOnTouchOutside(false);
        mPrograssDialog.show();



        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String profile_status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileNAme.setText(display_name);
                mProfileStatus.setText(profile_status);

                if(!image.equals("defalt")){
                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_man).into(mProfileImage);
                }


                //--------------Friend List / Request Feature
                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){

                                mCurrrent_state ="req_received";
                                mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");

                            }else if (req_type.equals("send")){
                                mCurrrent_state ="req_send";
                                mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");
                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            }
                            mPrograssDialog.dismiss();

                        }else {

                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                            mProfileDeclineBtn.setEnabled(false);
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend");



                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {


                                }
                            });

                            mPrograssDialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mPrograssDialog.dismiss();

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mPrograssDialog.dismiss();

            }
        });

        if (user_id.equals(mCurrent_user.getUid())){
            mProfileSendReqBtn.setEnabled(false);
            mProfileSendReqBtn.setVisibility(View.INVISIBLE);



        }

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);
                if(mCurrrent_state.equals("not_friends")) {



                    DatabaseReference newNotificationRef = mRootRef.child("notification").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrent_user.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();

                    requestMap.put("friend_request/" + mCurrent_user.getUid() + "/" + user_id +"/"+ "request_type", "send");
                    requestMap.put("friend_request/" + user_id + "/" + mCurrent_user.getUid() +"/"+ "request_type", "received");

                    requestMap.put("notification/" + user_id + "/" + newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Toast.makeText(ProfileActivity.this, "There Is A Error Sending Friend Request", Toast.LENGTH_SHORT).show();
                            }

                            else {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrrent_state ="req_send";
                                mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                mProfileSendReqBtn.setText("CANCEN FRIEND REQUEST");

                                Toast.makeText(ProfileActivity.this, "Request Send Successfull", Toast.LENGTH_LONG).show();
                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }



                        }
                    });


                }
                if(mCurrrent_state.equals("req_send")){
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrrent_state ="not_friends";
                                    mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    mProfileSendReqBtn.setText("SEND FRIEND REQUEST");
                                    Toast.makeText(ProfileActivity.this, "Friend Canceled", Toast.LENGTH_LONG).show();
                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });


                }

                if (mCurrrent_state.equals("req_received")){
                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/"+mCurrent_user.getUid()+"/"+user_id+"/date",current_date);
                    friendsMap.put("friends/"+user_id+"/"+mCurrent_user.getUid()+"/date",current_date);

                    friendsMap.put("friend_request/"+mCurrent_user.getUid()+"/"+user_id,null);
                    friendsMap.put("friend_request/"+user_id+"/"+mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrrent_state ="friends";
                                //mProfileSendReqBtn.setBackgroundColor(R.color.colorPrimary);
                                mProfileSendReqBtn.setText("Unfriend");
                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }
                        }
                    });
                }

                if(mCurrrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError ==null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrrent_state ="not_friends";
                                //mProfileSendReqBtn.setBackgroundColor(R.color.colorPrimary);
                                mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });



    }


}
