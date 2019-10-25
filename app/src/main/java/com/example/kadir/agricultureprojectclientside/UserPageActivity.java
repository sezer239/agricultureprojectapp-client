package com.example.kadir.agricultureprojectclientside;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.Unused.FragmentEditUserDialogue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserPageActivity extends AppCompatActivity {

    private TextView currentUserIdText;
    private TextView currentUserNameText;
    private TextView currentUserEmailText;
    private TextView currentUserPhoneText;
    private TextView currentUserAddressText;

    private Button editUserButton;
    private Button buttonSignOut;
    private Button editProfileButton;
    private Button editAccountButton;

    private ImageView imageAsset;
    private Dialog editDialog;
    private ProgressBar progressBar;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("User");
    private String uid;

    public void initializeScreen() {

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullName = (dataSnapshot.child(uid).child("profile").child("userName").getValue() + "")
                        + " " + (dataSnapshot.child(uid).child("profile").child("userLastname").getValue() + "");
                currentUserIdText.setText(dataSnapshot.child(uid).getKey().toString());
                currentUserNameText.setText(fullName);
                currentUserEmailText.setText(dataSnapshot.child(uid).child("profile").child("userEmail").getValue() + "");
                currentUserPhoneText.setText(dataSnapshot.child(uid).child("profile").child("userPhone").getValue() + "");
                currentUserAddressText.setText(dataSnapshot.child(uid).child("profile").child("userAddress").getValue() + "");
                progressBar.setVisibility(View.INVISIBLE);
                buttonSignOut.setEnabled(true);
                imageAsset.setEnabled(true);
                editUserButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        myRef.addValueEventListener(postListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_userpage);
        uid = FirebaseHelper.getmFirebaseAuth().getUid();
        currentUserIdText = (TextView) findViewById(R.id.uidField);
        currentUserNameText = (TextView) findViewById(R.id.fullNameField);
        currentUserEmailText = (TextView) findViewById(R.id.mailField);
        currentUserPhoneText = (TextView) findViewById(R.id.phoneField);
        currentUserAddressText = (TextView) findViewById(R.id.adresField);
        editUserButton = (Button) findViewById(R.id.buttonEditUser);
        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        imageAsset = (ImageView)findViewById(R.id.edit_info_button);
        editDialog = new Dialog(UserPageActivity.this);
        editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        buttonSignOut.setEnabled(false);
        imageAsset.setEnabled(false);
        editUserButton.setEnabled(false);
        while(uid == null);
        this.initializeScreen();
        //TODO: imageAsset.setEnable eklendi
        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSignOut.setEnabled(false);
                editUserButton.setEnabled(false);
                imageAsset.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(UserPageActivity.this, EditUserActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                UserPageActivity.this.finish();
            }
        });
        //TODO: imageAsset.setEnable eklendi
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSignOut.setEnabled(false);
                editUserButton.setEnabled(false);
                imageAsset.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                FirebaseHelper.signOut();
                Intent goLoginPage = new Intent(UserPageActivity.this, MainActivity.class);
                startActivity(goLoginPage);
                finish();
            }
        });

        imageAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog.setContentView(R.layout.edit_user_popup);
                editAccountButton = (Button) editDialog.findViewById(R.id.change_pass_email);
                editProfileButton = (Button) editDialog.findViewById(R.id.change_profile_info);
                editProfileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editProfileButton.setEnabled(false);
                        editAccountButton.setEnabled(false);
                        editUserButton.setEnabled(false);
                        buttonSignOut.setEnabled(false);
                        editDialog.dismiss();
                        Intent intent = new Intent(UserPageActivity.this , UpdateUserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                editAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editProfileButton.setEnabled(false);
                        editAccountButton.setEnabled(false);
                        editUserButton.setEnabled(false);
                        buttonSignOut.setEnabled(false);
                        editDialog.dismiss();
                        Intent intent = new Intent(UserPageActivity.this , UpdateUserAccountActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                editDialog.show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        editUserButton.setEnabled(true);
        buttonSignOut.setEnabled(true);
        super.onBackPressed();
    }
}
