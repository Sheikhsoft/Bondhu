package com.sheikhsoft.bondhu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.sheikhsoft.bondhu.reg.ChangeNameActivity;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mLoginBtn;
    private SignInButton mGoogleSignBtn;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN=1;
    private FirebaseAuth mAuth;
    private static final String TAG ="StartActivity";
    private DatabaseReference mUserDatabase;

    private ProgressDialog mLoginPrograss;
    private FirebaseUser user;
    private CallbackManager mCallbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mRegBtn = (Button) findViewById(R.id.start_reg_btn);
        mLoginBtn = (Button) findViewById(R.id.start_login_btn);
        mGoogleSignBtn = (SignInButton)findViewById(R.id.google_sign_btn);
        TextView textView = (TextView) mGoogleSignBtn.getChildAt(0);
        textView.setText("SIGN IN WITH GOOGLE");
        user = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent =  new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent =  new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);
            }
        });



        mLoginPrograss = new ProgressDialog(this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");



        //-------------Google SignIn-----------
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mGoogleSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(user !=null){
            String current_user_id = user.getUid();
            updateUI(current_user_id);
        }

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mLoginPrograss.setTitle("Logging In");
            mLoginPrograss.setMessage("Please Wait When We Check Your Credential");
            mLoginPrograss.setCanceledOnTouchOutside(false);
            mLoginPrograss.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                mLoginPrograss.dismiss();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mLoginPrograss.dismiss();
                            Log.d(TAG, "signInWithCredential:success");

                            user = FirebaseAuth.getInstance().getCurrentUser();
                            final String current_user_id = user.getUid();
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();



                            mUserDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(current_user_id)){

                                        mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mLoginPrograss.dismiss();

                                            }
                                        });
                                    }
                                    else {

                                        RegisterUser(current_user_id,deviceToken);

                                    }



                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            updateUI(current_user_id);
                        } else {
                            mLoginPrograss.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }



                        // ...
                    }
                });
    }

    private void RegisterUser(String current_user_id,String deviceToken) {



        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String mproviderId = profile.getProviderId();

                // UID specific to the provider
                String muid = profile.getUid();

                // Name, email address, and profile photo Url
                String mname = profile.getDisplayName();
                String memail = user.getEmail();
                Uri mphotoUrl = profile.getPhotoUrl();
                String Phone = profile.getPhoneNumber();

                HashMap<String, String> userMAp = new HashMap<String, String>();
                userMAp.put("device_token", deviceToken);
                userMAp.put("name", mname);
                userMAp.put("email", memail);
                userMAp.put("phone",Phone);
                userMAp.put("provider",muid);
                userMAp.put("provider_id",mproviderId);
                userMAp.put("status", "Hi there, I'm Using Bondho chat");
                userMAp.put("image",mphotoUrl.toString());
                userMAp.put("thumb_image",mphotoUrl.toString());
                userMAp.put("online","true");

                mUserDatabase.child(current_user_id).setValue(userMAp).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mLoginPrograss.dismiss();


                        }
                    }
                });


            };
        }



    }

    private void updateUI(final String current_user_id) {
        mUserDatabase.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("gender") ){
                    Intent mainIntend = new Intent(StartActivity.this,MainActivity.class);
                    mainIntend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntend);
                    finish();
                }
                else {

                    Intent regIntent = new Intent(StartActivity.this,ChangeNameActivity.class);
                    regIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    regIntent.putExtra("user_id",current_user_id);
                    startActivity(regIntent);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        mLoginPrograss.setTitle("Logging In");
        mLoginPrograss.setMessage("Please Wait When We Check Your Credential");
        mLoginPrograss.setCanceledOnTouchOutside(false);
        mLoginPrograss.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoginPrograss.dismiss();
                            Log.d(TAG, "signInWithCredential:success");


                            user = FirebaseAuth.getInstance().getCurrentUser();
                            final String current_user_id = user.getUid();
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();



                            mUserDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(current_user_id)){

                                        mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mLoginPrograss.dismiss();

                                            }
                                        });
                                    }
                                    else {

                                        RegisterUser(current_user_id,deviceToken);

                                    }



                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });




                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(current_user_id);
                        } else {
                            mLoginPrograss.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
