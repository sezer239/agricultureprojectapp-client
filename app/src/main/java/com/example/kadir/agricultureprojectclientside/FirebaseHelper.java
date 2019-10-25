package com.example.kadir.agricultureprojectclientside;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.kadir.agricultureprojectclientside.Interfaces.Callback;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmCallback;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmSensorData;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnLoginUserCallback;
import com.example.kadir.agricultureprojectclientside.ShortCut.ShortCut;
import com.example.kadir.agricultureprojectclientside.ShortCut.User;

import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ProductData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    public static Farm farms;

    public String prev_user_id;

    public FirebaseAuth.AuthStateListener mAuthListener;

    private static FirebaseDatabase mDatabase;

    public static FirebaseAuth mFirebaseAuth;

    public static FirebaseAuth getmFirebaseAuth() {
        return mFirebaseAuth = FirebaseAuth.getInstance();
    }
//TODO: metot static yapıldı.kadir
    //user id alma
    public static String getFirebaseUserAuthID() {
        String userID = null;
        mFirebaseAuth = getmFirebaseAuth();
        if (mFirebaseAuth.getCurrentUser() != null) {
            userID = mFirebaseAuth.getCurrentUser().getUid();
        }
        return userID;
    }

    //login olan user varsa user sayfasını yükler
    public void checkUserLogin(final Context context) {
        mFirebaseAuth = this.getmFirebaseAuth();
        if (mFirebaseAuth.getCurrentUser() != null) {
            Intent userIntent = new Intent(context, UserPageActivity.class);
            userIntent.putExtra("Id", prev_user_id);
            context.startActivity(userIntent);
        }
    }

    //login olan kullanıcının hesabında bir değişiklik yapıldığında burası kullanılarak algılanacak ve logout yapılacak.
    // user = null ise değişiklik yapılmıştır.
    public void isCurrentlyUserLogin(final Context context) {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent userIntent = new Intent(context, UserPageActivity.class);
                    context.startActivity(userIntent);
                } else {
//                    Intent loginIntent = new Intent(context, LoginPageActivity.class); ///finish(); ekleyip dene..
//                    context.startActivity(loginIntent);
                }
            }
        };
    }

//TODO: static metot yapıldı
    public static void signOut() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth != null) {
            mFirebaseAuth.signOut();
        }
    }


    public void loginUser(final Context context, String email, String password, final OnLoginUserCallback callback) {
        mFirebaseAuth = this.getmFirebaseAuth();
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("User");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(mFirebaseAuth.getUid())) {
                                        if (callback != null)
                                            callback.onCorrectUserCallback();
                                        Intent intent = new Intent(context, UserPageActivity.class);
                                        intent.putExtra("Id", mFirebaseAuth.getUid());
                                        context.startActivity(intent);
                                        ((Activity) context).finish();
                                    } else {
                                        if (callback != null)
                                            callback.onWrongUserCallback();
                                        //NOT USER
                                        ShortCut.displayMessageToast(context, "Invalid Account!");
                                        getmFirebaseAuth().signOut();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    //NOTUSED
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.onWrongUserCallback();
                            ShortCut.displayMessageToast(context, "Invalid Email or Password!");
                        }
                    }
                });
    }


    public void putProfileInfo(User user, String UserId) {
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference Users = mDatabase.getReference("User");
        Map<String, User> userInfo = new HashMap<>();
        userInfo.put("profile", user);
        Users.child(UserId).child("profile").setValue(user);
    }

    public static void putFarmInfo(String currentUserId, Farm farm, String farmId) {
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference Farm = mDatabase.getReference("User");
        Map<String, Object> FarmData = new HashMap<>();
        FarmData.put(farmId, farm);
        Farm.child(currentUserId).child("Farms").child(farmId).setValue(FarmData);
    }

    public static void getUserFarmSensorData(String currentUserId, final String farmName,
                                             final OnGetFarmSensorData callback) {
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("User").child(currentUserId).child("Modules").child(farmName).child("ModuleOfFarms");
        myRef.keepSynced(true);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v("ASD", "FB CALSIYOR");
                ArrayList<ModuleData> moduleData = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    ModuleData md = new ModuleData();
                    md.ph = Float.parseFloat(d.child("ph").getValue() + "");
                    md.air_humidity = Float.parseFloat(d.child("airHumidity").getValue() + "");
                    md.air_temperature = Float.parseFloat(d.child("airTemperature").getValue() + "");
                    md.soil_humidity = Float.parseFloat(d.child("soilHumidity").getValue() + "");
                    md.soil_temperature = Float.parseFloat(d.child("soilTemperature").getValue() + "");
                    md.module_id = d.child("moduleId").getValue() + "";
                    md.farm_name = farmName;
                    md.ah_working = (d.child("ahWorking").getValue() + "").equals("true");
                    md.at_working = (d.child("atWorking").getValue() + "").equals("true");
                    md.st_working = (d.child("stWorking").getValue() + "").equals("true");
                    md.sh_working = (d.child("shWorking").getValue() + "").equals("true");
                    md.ph_working = (d.child("phWorking").getValue() + "").equals("true");

                    moduleData.add(md);
                }
                callback.onFarmSensorData(moduleData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        myRef.addValueEventListener(postListener);
    }

    public static void getUserAllFarmSingle(final String currentUserId, final OnGetFarmCallback callback) {
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("User").child(currentUserId).child("Farms");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Farm> farmData = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    // [START_EXCLUDE]
                    Farm f = d.getChildren().iterator().next().getValue(Farm.class);
                    Log.v("aka", f.farm_id);
                    farmData.add(f);
                    // [END_EXCLUDE]
                }

                callback.onGetFarmCallback(farmData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getAllProductData(final OnProductDataLoaded callback) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductData");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ProductData> productData = new ArrayList<>();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    ProductData pd = new ProductData();
                    pd.product_id = d.child("product_id").getValue() + "";
                    pd.product_name = d.child("product_name").getValue() + "";
                    productData.add(pd);
                }
                callback.productDataLoaded(productData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void getUserAllFarm(final String currentUserId, final OnGetFarmCallback callback) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("User").child(currentUserId).child("Farms");
        Log.v("aaaaa", currentUserId);
        ValueEventListener postlistener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v("TTT", "getUserAllFarm is CALLED");
                ArrayList<Farm> farmData = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    // [START_EXCLUDE]
                    Farm f = d.getChildren().iterator().next().getValue(Farm.class);
                    Log.v("aka", f.farm_id);
                    farmData.add(f);
                    // [END_EXCLUDE]
                }
                callback.onGetFarmCallback(farmData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        while(myRef == null);
        myRef.addValueEventListener(postlistener);
    }


    public interface OnProductDataLoaded {
        void productDataLoaded(ArrayList<ProductData> productData);
    }


    public static void updatePassword(final Context context, String email, String password, final String newPassword , final OnUpdateUserPassword callback){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                callback.OnUserPasswordUpdatedSucceded();
                            }else{
                                callback.OnUserPasswordUpdatedFailed();
                                ShortCut.displayMessageToast(context, "En az 6 karakter girmelisiniz");
                            }
                        }
                    });
                }else{
                    ShortCut.displayMessageToast(context, "Geçersiz e-posta veya şifre");
                    callback.OnUserPasswordUpdatedFailed();
                }
            }
        });
    }

    public static void updateEmail(final Context context, final String email, String password, final String newEmail) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.updateEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ShortCut.displayMessageToast(context, "E-posta adresi güncellendi");
                                            } else {
                                                ShortCut.displayMessageToast(context, "Yeni e-posta geçersizdir");
                                            }
                                        }
                                    });
                        } else {
                            ShortCut.displayMessageToast(context, "Geçersiz şifre veya e-posta");
                        }
                    }
                });

    }
    public void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                        }
                        else
                        {

                        }
                    }
                });
    }

    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.isEmailVerified())
        {

        }
        else
        {
            FirebaseAuth.getInstance().signOut();
        }
    }

    public interface OnUpdateUserPassword{
        void OnUserPasswordUpdatedSucceded();
        void OnUserPasswordUpdatedFailed();
    }

    public interface OnUpdateUserMail{
        void OnUserMailUpdatedSucceded();
        void OnUserMailUpdatedFailed();
    }
}
