package com.example.kadir.agricultureprojectclientside;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.kadir.agricultureprojectclientside.Unused.FirstFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LoginPageFragment.OnFragmentInteractionListener {

    FirebaseHelper firebaseHelper;
    Dialog dialogNoConn;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseHelper = new FirebaseHelper();
        dialogNoConn = new Dialog(MainActivity.this);
        dialogNoConn.setContentView(R.layout.internet_connection_popup);
        button = (Button) dialogNoConn.findViewById(R.id.button_internet);
        dialogNoConn.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (isOnline()) {
            if (firebaseHelper.getFirebaseUserAuthID() == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, new LoginPageFragment());
                ft.addToBackStack(null);
                ft.setTransition(1);
                ft.commit();
            } else {
                Log.v("ASD", firebaseHelper.getFirebaseUserAuthID());
                firebaseHelper.checkUserLogin(MainActivity.this);
                this.finish();
            }
        } else {

            dialogNoConn.show();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogNoConn.dismiss();
                    finish();
                }
            });

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }
}