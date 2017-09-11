package com.sheikhsoft.bondhu;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Sk Shamimul islam on 08/17/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;



    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public EmojiconTextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public RelativeLayout ChatLayout;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (EmojiconTextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            //displayName = (TextView) view.findViewById(R.id.name_text_layout);
            ChatLayout= (RelativeLayout) view.findViewById(R.id.message_single_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();


        if (from_user.equals(current_user_id)){


            viewHolder.messageText.setBackgroundColor(Color.WHITE);
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.ChatLayout.setGravity(Gravity.RIGHT);
            viewHolder.profileImage.setVisibility(View.INVISIBLE);




        }else {

            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);
            viewHolder.ChatLayout.setGravity(Gravity.LEFT);
            viewHolder.profileImage.setVisibility(View.VISIBLE);
        }

        if (current_user_id != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    // viewHolder.displayName.setText(name);


                        Picasso.with(viewHolder.profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);




                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }





        viewHolder.messageText.setText(c.getMessage());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



}
