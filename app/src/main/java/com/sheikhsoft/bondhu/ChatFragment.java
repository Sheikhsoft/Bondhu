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
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chat, container, false);
        mChatsList = (RecyclerView) mMainView.findViewById(R.id.chat_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        if (mCurrent_user_id!=null) {
            mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mChatsDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            mUsersDatabase.keepSynced(true);
        }


        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chats, ChatsViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(
                Chats.class,
                R.layout.users_single_layout,
                ChatFragment.ChatsViewHolder.class,
                mChatsDatabase
        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder chatsViewHolder, final Chats chats, int position) {
                final String list_user_id = getRef(position).getKey();

                DatabaseReference messageRef = mChatsDatabase.child(list_user_id);
                Query messageQuery = messageRef.limitToLast(1);

                messageQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {

                            String message = (String) messageSnapshot.child("message").getValue();
                            chatsViewHolder.setDate(message);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });






                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        chatsViewHolder.setName(username);
                        chatsViewHolder.setImage(userThumb);

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            chatsViewHolder.setUserOnline(userOnline);
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mChatsList.setAdapter(chatsRecyclerViewAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDate(String date){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);

        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setImage(String image){

            CircleImageView mCircleImaheView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            if (!image.equals("defalt")){
                Picasso.with(mView.getContext()).load(image).placeholder(R.drawable.default_man).into(mCircleImaheView);
            }


        }

        public void setUserOnline(String online_Status){
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.user_single_online);
            if (online_Status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }


    }
}
