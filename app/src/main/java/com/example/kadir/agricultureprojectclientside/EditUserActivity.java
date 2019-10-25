package com.example.kadir.agricultureprojectclientside;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.kadir.agricultureprojectclientside.Adapter.CustomAdapter;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmCallback;
import com.example.kadir.agricultureprojectclientside.ShortCut.User;

import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditUserActivity extends AppCompatActivity implements SensorsOverviewFragment.OnFragmentInteractionListener {


    private ArrayList<Farm> UserFarmData;
    private User user = new User();
    private ArrayList<FarmEditView> mFarmCard;
    private FarmEditView farmoutlineview;


    private String mCurrentEditUserId;
    private FirebaseHelper firebaseHelper;
    private String Name, LastName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("User");

    private FragmentTransaction ft;

    CustomAdapter customAdapter;
    Dialog checkDialog;
    RecyclerView farmRecyclerView;

    public void updateCurrentUserData() {

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = mCurrentEditUserId;
                Name = dataSnapshot.child(uid).child("profile").child("userName").getValue() + "";
                LastName = dataSnapshot.child(uid).child("profile").child("userLastname").getValue() + "";
                String id = dataSnapshot.child(uid).getKey().toString();
                String email = dataSnapshot.child(uid).child("profile").child("userEmail").getValue() + "";
                String phone = dataSnapshot.child(uid).child("profile").child("userPhone").getValue() + "";
                String address = dataSnapshot.child(uid).child("profile").child("userAddress").getValue() + "";
                user.setUserId(uid);
                user.setUserName(Name);
                user.setUserLastname(LastName);
                user.setUserEmail(email);
                user.setUserPhone(phone);
                user.setUserAddress(address);
                setTitle(Name + " " + LastName);
                //Log.v("ASD",Name+" "+LastName + "  , " + uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // myRef.addValueEventListener(postListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_user);
        Toolbar mToolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mCurrentEditUserId = FirebaseAuth.getInstance().getUid();
        mFarmCard = new ArrayList<FarmEditView>();
        UserFarmData = new ArrayList<Farm>();
        farmRecyclerView = (RecyclerView) findViewById(R.id.farm_image_recyclerview);
        LinearLayoutManager lm = new LinearLayoutManager(EditUserActivity.this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        lm.scrollToPosition(0);
        farmRecyclerView.setLayoutManager(lm);
        farmRecyclerView.setHasFixedSize(true);
        farmRecyclerView.setLayoutManager(lm);
        while(mCurrentEditUserId == null);
        FirebaseHelper.getUserAllFarm(mCurrentEditUserId, new OnGetFarmCallback() {
            @Override
            public void onGetFarmCallback(ArrayList<Farm> farmdata) { ////Canlı görüntü sağlayabilmek için interface kullandık
                initializeFarmImage(farmdata);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(100, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                String farm_id = customAdapter.getFarmAt(viewHolder.getAdapterPosition()).getEdited_farm().farm_id;
                Bundle bundle = new Bundle();
                bundle.putString("FarmId", farm_id);
                SensorsOverviewFragment sensorFragment = new SensorsOverviewFragment();
                sensorFragment.setArguments(bundle);
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container_sensors_overview, sensorFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        }).attachToRecyclerView(farmRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {

        if(ft == null){
            Intent intent = new Intent(EditUserActivity.this, UserPageActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            super.onBackPressed();
            this.finish();
        }else{
            Intent intent = new Intent(EditUserActivity.this, EditUserActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            finish();

        }
    }

    void initializeFarmImage(ArrayList<Farm> farmdata) {
        Log.v("ASD", "farmdata size " + farmdata.size());
        mFarmCard.clear();
        for (final Farm f : farmdata) {                          /////eğer kullanmasaydık data canlı kalırdı fakat resimler bir defa oluşturulurdu
            FarmEditView initfarmCard = new FarmEditView(EditUserActivity.this);
            initfarmCard.load_farm(f, true);
            initfarmCard.setMinimumWidth(1000);
            initfarmCard.setMinimumHeight(1000);
            initfarmCard.setMaxWidth(1000);
            initfarmCard.setMaxHeight(1000);
//            initfarmCard.setBackgroundColor(Color.WHITE);
            mFarmCard.add(initfarmCard);
        }
        customAdapter = new CustomAdapter(mFarmCard, EditUserActivity.this, mCurrentEditUserId);
        farmRecyclerView.setAdapter(customAdapter);

//        customAdapter.updateReceiptsList(mFarmCard);
        updateCurrentUserData();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
