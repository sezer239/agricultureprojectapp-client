package com.example.kadir.agricultureprojectclientside.Unused;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.kadir.agricultureprojectclientside.FarmEditView;
import com.example.kadir.agricultureprojectclientside.FirebaseHelper;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmCallback;
import com.example.kadir.agricultureprojectclientside.R;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CardViewFarmActivity extends AppCompatActivity {

    private ArrayList<FarmEditView> mFarmCard;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view_farm);

        initializeArray();

        RecyclerView farmRecyclerView = (RecyclerView)findViewById(R.id.activity_card_view_list);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(CardViewFarmActivity.this);
        farmRecyclerView.setLayoutManager(lm);





    }

    private void initializeArray(){
        mFarmCard = new ArrayList<FarmEditView>();
        FirebaseHelper.getUserAllFarm(FirebaseAuth.getInstance().getUid(), new OnGetFarmCallback() {
            @Override
            public void onGetFarmCallback(ArrayList<Farm> farmdata) {
                for(final Farm f : farmdata){
                    FarmEditView initfarmCard = new FarmEditView(CardViewFarmActivity.this);
                    initfarmCard.load_farm(f,true);
//                    initfarmCard.setMaxWidth(1000);
                    initfarmCard.setMinimumWidth(1000);
//                    initfarmCard.setMaxHeight(1000);
                    initfarmCard.setMinimumHeight(1000);
                    initfarmCard.setBackgroundColor(Color.WHITE);

                    mFarmCard.add(initfarmCard);
                }
            }
        });
    }
}