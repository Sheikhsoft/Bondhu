package com.sheikhsoft.bondhu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.sheikhsoft.bondhu.Utils.BottomNavigationViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView mCircleImaheView;
    private TextView mName;
    private TextView mStatus;

    private ImageButton mSaveStatusBtn;
    private ImageButton mImageBtn;
    private Context condext = SettingsActivity.this;
    private Toolbar mToolBar;


    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;

    private ProgressDialog mPrograssDialog;
    private static final int ACTIVITY_NUM = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolBar = (Toolbar)findViewById(R.id.setting_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Bondhu Chat");


        mCircleImaheView = (CircleImageView) findViewById(R.id.setting_cir_Img_view);
        mName = (TextView)findViewById(R.id.setting_displayname);
        mStatus = (TextView)findViewById(R.id.setting_status);
        mImageBtn = (ImageButton)findViewById(R.id.setting_edit_info_image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        mDatabase.keepSynced(true);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thamp_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("defalt")){
                   // Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_man).into(mCircleImaheView);

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_man).into(mCircleImaheView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_man).into(mCircleImaheView);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mSaveStatusBtn = (ImageButton) findViewById(R.id.setting_setting_img_btn);
        mSaveStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent statusInstant = new Intent(SettingsActivity.this, StatusActivity.class);
                statusInstant.putExtra("status_value",status_value);
                startActivity(statusInstant);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"), GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(400,400)
                        .start(SettingsActivity.this);
            }
        });

        setupBottomNavigationView();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

       /* if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .start(this);

        }*/
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mPrograssDialog = new ProgressDialog(SettingsActivity.this);
                mPrograssDialog.setTitle("Upload Image");
                mPrograssDialog.setMessage("Please Wail While We Uploadin Your Image");
                mPrograssDialog.setCanceledOnTouchOutside(false);
                mPrograssDialog.show();
                Uri resultUri = result.getUri();

                String current_uid = mCurrentUser.getUid();
                File thumb_filePath = new File(resultUri.getPath());

                Bitmap thumb_bitmap = null;
                try{
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(150)
                            .setMaxHeight(150)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                }catch (IOException e){
                    e.printStackTrace();
                }

                Bitmap image_bitmap = null;
                try{
                    image_bitmap = new Compressor(this)
                            .setMaxWidth(400)
                            .setMaxHeight(400)
                            .setQuality(30)
                            .compressToBitmap(thumb_filePath);
                }catch (IOException e){
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();
                ByteArrayOutputStream baoss = new ByteArrayOutputStream();
                image_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baoss);
                final byte[] image_byte = baoss.toByteArray();


                StorageReference filepath = mImageStorage.child("profile_images").child(current_uid+".jpg");

                final StorageReference thumb_file_path = mImageStorage.child("profile_images").child("thumbs").child("thumb_"+current_uid+".jpg");
                filepath.putBytes(image_byte)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    @SuppressWarnings("VisibleForTests") final String download_url = task.getResult().getDownloadUrl().toString();

                                    UploadTask uploadTask = thumb_file_path.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                            @SuppressWarnings("VisibleForTests") String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
                                            if (thumb_task.isSuccessful()){
                                                Map update_hashMap = new HashMap();
                                                update_hashMap.put("image",download_url);
                                                update_hashMap.put("thumb_image",thumb_download_url);
                                                mDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            mPrograssDialog.dismiss();
                                                            Toast.makeText(SettingsActivity.this, "Uploading Image Successfull", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                });
                                            }

                                        }
                                    });
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Error In Uploading Image", Toast.LENGTH_LONG).show();
                                    mPrograssDialog.dismiss();
                                }

                            }
                        });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar ;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96)+32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void setupBottomNavigationView(){

        Log.d("Setting Activity","Setup Bottom Navigation View");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(condext,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_btn){
            mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStart();

        }
        if(item.getItemId()==R.id.main_setting_btn){
            Intent SettingInstant = new Intent(SettingsActivity.this,SettingsActivity.class);
            startActivity(SettingInstant);
        }

        if(item.getItemId()==R.id.main_users_btn){
            Intent UsersInstant = new Intent(SettingsActivity.this,UsersActivity.class);
            startActivity(UsersInstant);
        }
        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(SettingsActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

}
