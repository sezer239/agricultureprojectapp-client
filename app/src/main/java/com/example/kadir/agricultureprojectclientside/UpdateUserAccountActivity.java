package com.example.kadir.agricultureprojectclientside;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.ShortCut.ShortCut;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateUserAccountActivity extends AppCompatActivity {

    //BUTTONS
    private Button cancel_button;
    private Button confirm_button;

    //EDIT TEXT FIELDS
    private EditText mPass;
    private EditText mNewPass;

    //TETXVIEW
    private TextView mMail;


    //PROGRES BARR
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_account);

        //PROGRES BARR
        progress_bar = findViewById(R.id.progress_bar_edituser_account);

        //BUTTON INIT
        cancel_button = findViewById(R.id.buttonCancel);
        confirm_button = findViewById(R.id.buttonUpdate);

        //EDITTEXT INIT
        mMail    = (TextView) findViewById(R.id.e_mailEditText);
        mPass    = (EditText) findViewById(R.id.passwEditText);
        mNewPass = (EditText) findViewById(R.id.newPassEditText);
        progress_bar.setVisibility(View.VISIBLE);
        mPass.setEnabled(false);
        mNewPass.setEnabled(false);
        cancel_button.setEnabled(false);
        confirm_button.setEnabled(false);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getUid()).child("profile");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMail.setText( dataSnapshot.child("userEmail").getValue() + "");
                progress_bar.setVisibility(View.INVISIBLE);
                mPass.setEnabled(true);
                mNewPass.setEnabled(true);
                cancel_button.setEnabled(true);
                confirm_button.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm_button.setEnabled(false);
                cancel_button.setEnabled(false);
                progress_bar.setVisibility(View.VISIBLE);

                String newPass = mNewPass.getText().toString();
                String oldPass = mPass   .getText().toString();
                String mail    = mMail   .getText().toString();
//TODO: Şifre değiştirilme başarılı olduğunda oluşan hatadan dolayı signout yapıldı olarak değiştirildi ve yönlendirme olarak mainactivity e yönlendirildi. FirebaseHelper signout methodu ve içindeki mFirebaseAuth değişkeni static yapıldı. Kadir
//TODO: Alanlar boş olunca butonları etkinleştirme eklendi. Kadir
                if (oldPass.isEmpty() || newPass.isEmpty() || mail.isEmpty()) {
                    ShortCut.displayMessageToast(UpdateUserAccountActivity.this , "Lütfen bütün alanları doldurun");
                    progress_bar.setVisibility(View.INVISIBLE);
                    confirm_button.setEnabled(true);
                    cancel_button.setEnabled(true);
                } else {
                    FirebaseHelper.updatePassword(UpdateUserAccountActivity.this, mail, oldPass, newPass, new FirebaseHelper.OnUpdateUserPassword() {
                        @Override
                        public void OnUserPasswordUpdatedSucceded() {
                            ShortCut.displayMessageToast(UpdateUserAccountActivity.this , "Şifre değiştirme başarılı");
                            FirebaseHelper.signOut();
                            Intent intent = new Intent(UpdateUserAccountActivity.this , MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void OnUserPasswordUpdatedFailed() {
                            confirm_button.setEnabled(true);
                            cancel_button.setEnabled(true);
                            mPass.setEnabled(true);
                            mNewPass.setEnabled(true);
                            progress_bar.setVisibility(View.INVISIBLE);

                        }
                    });
                }
            }
        });


        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel_button.setEnabled(false);
                confirm_button.setEnabled(false);
                Intent intent = new Intent(UpdateUserAccountActivity.this , UserPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UpdateUserAccountActivity.this , UserPageActivity.class);
        startActivity(intent);
        finish();
    }
}
