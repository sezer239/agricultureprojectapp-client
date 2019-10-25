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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.Adapter.PopupRecyclerViewAdapter;
import com.example.kadir.agricultureprojectclientside.ShortCut.ShortCut;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ProductData;
import com.example.kadir.agricultureprojectclientside.module_selector.ProductSelectorFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SezerMainActivity extends AppCompatActivity implements ProductSelectorFragment.OnFragmentInteractionListener{



    //VIEWS
    FarmEditView edit_farm;
    TextView moduleId, airHum, airTemp, soilHum, soilTemp, ph;

    //DATA
    Farm farm;
    String user_id;

    //DATABASE
    DatabaseReference firebase_ref;

    //BUTTONS
    Button save_button;
    Button clean_button;
    Button toggle_button;

    //DIALOG AND PRODUCT SELECTION STUFF
    Dialog farm_name_dialog, sensor_info_dialog;
    RecyclerView product_selection_recycler_view;
    PopupRecyclerViewAdapter product_selection_recycler_view_adapter;

    FragmentTransaction ft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sezer_main);

        edit_farm = findViewById(R.id.edit_view);
        toggle_button = findViewById(R.id.toggle_button);
        clean_button = findViewById(R.id.clean_button);
        save_button = findViewById(R.id.save_button);

        edit_farm.on_display_phase_change = new FarmEditView.OnDisplayPhaseChange() {
            @Override
            public void on_display_phase_change(FarmEditView.DisplayPhases prev_phase, FarmEditView.DisplayPhases current_phase) {
                if(current_phase == FarmEditView.DisplayPhases.DISPLAY_PRODUCTS){
                    clean_button.setEnabled(true);
                }else{
                    clean_button.setEnabled(false);
                }
            }
        };

        edit_farm.on_long_click_listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (edit_farm.current_edit_phase() == FarmEditView.EditPhases.PRODUCT_PLACEMENT_PHASE &&
                        edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_PRODUCTS  &&
                        edit_farm.allow_putting_module()
                        ) {
                    spawn_product_selection_fragment();
                }else if(edit_farm.current_edit_phase() == FarmEditView.EditPhases.PRODUCT_PLACEMENT_PHASE &&
                        edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_PRODUCTS  &&
                        edit_farm.can_remove_module()){
                    edit_farm.remove_data_last_selected_place();
                }else if(edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_FIXES && edit_farm.can_fix_cell()){
                    show_fixup_module_dialog();
                }
                return true;
            }
        };


        edit_farm.on_module_click_listener = new FarmEditView.OnModuleClick() {
            @Override
            public void on_module_click(String module_id) {
                show_sensor_info_popup(module_id);
            }
        };

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_farm.save_farm();

                FirebaseHelper.putFarmInfo(FirebaseAuth.getInstance().getUid(),farm,farm.farm_id);
                Intent intent = new Intent(SezerMainActivity.this, EditUserActivity.class);
                startActivity(intent);
                finish();
            }
        });

        clean_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_farm.clean();
            }
        });

        toggle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_farm.next_display_phase();
                update_the_buttons_with_correct_text_according_to_current_phase();
            }
        });

        Bundle extras = getIntent().getExtras();
        user_id = extras.getString("userID");
        farm = (Farm) extras.getSerializable("Farm");

        if (farm == null) {
            farm = new Farm();
            farm.size = 16;
            farm_name_dialog = new Dialog(this);
            show_popup();
        }

        edit_farm.load_farm(farm);

        update_the_buttons_with_correct_text_according_to_current_phase();
    }

    //TODO: RENAME THIS
    public void update_the_buttons_with_correct_text_according_to_current_phase(){
        if(edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_PRODUCTS){
            toggle_button.setText(R.string.show_product);
            toggle_button.setBackgroundResource(R.drawable.rounded_products_button);
        }else if(edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_MODULES){
            toggle_button.setText(R.string.show_modules);
            toggle_button.setBackgroundResource(R.drawable.rounded_modules_button);
        }else if(edit_farm.current_display_phase() == FarmEditView.DisplayPhases.DISPLAY_FIXES){
            toggle_button.setText(R.string.show_fixes);
            toggle_button.setBackgroundResource(R.drawable.rounded_fixes_button);
        }
    }

    public void spawn_product_selection_fragment() {
        ft = getSupportFragmentManager().beginTransaction();
        ProductSelectorFragment module_selector_fragment = new ProductSelectorFragment();

        module_selector_fragment.setOn_item_selected_callback(new ProductSelectorFragment.OnSelectedItemCallback() {
            @Override
            public void on_selected_item_callback(ProductData productData) {
                edit_farm.put_data_onto_last_selected_place(productData);
                ft = null;
            }
        });

        module_selector_fragment.load_product_data();

        ft.replace(R.id.container, module_selector_fragment);
        ft.addToBackStack(null);
        ft.commit();
    }


    @Override
    public void onBackPressed() {
        if(ft == null){
            Intent intent = new Intent(SezerMainActivity.this, EditUserActivity.class);
            startActivity(intent);
            this.finish();
        }else{
            ft.addToBackStack(null);
            ft = null;
        }
        super.onBackPressed();
    }

    //show the info about the given sensor in a dialog window
    private void show_sensor_info_popup(final String module_Id){
        sensor_info_dialog = new Dialog(SezerMainActivity.this);
        sensor_info_dialog.setContentView(R.layout.sensor_info_popup);

        sensor_info_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        moduleId = sensor_info_dialog.findViewById(R.id.moduleId);
        airHum = sensor_info_dialog.findViewById(R.id.air_hum);
        airTemp = sensor_info_dialog.findViewById(R.id.air_temp);
        soilHum = sensor_info_dialog.findViewById(R.id.soil_hum);
        soilTemp = sensor_info_dialog.findViewById(R.id.soil_temp);
        ph = sensor_info_dialog.findViewById(R.id.ph);

        firebase_ref = FirebaseDatabase.getInstance().getReference("User").child(user_id)
                .child("Modules").child(farm.farm_id).child("ModuleOfFarms");

        firebase_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    if((d.child("moduleId").getValue() + "").equals(module_Id)){
                        moduleId.setText("Modul No : " + module_Id);
                        airHum.setText("Hava Nem: " + ((d.child("ahWorking").getValue() + "" ).equals("true") ? d.child("airHumidity").getValue():"Çalışmıyor"));
                        airTemp.setText("Hava Sıcaklık: " + ((d.child("atWorking").getValue() + "" ).equals("true") ? d.child("airTemperature").getValue():"Çalışmıyor"));
                        soilHum.setText("Toprak Nem : " + ((d.child("shWorking").getValue() + "" ).equals("true") ? d.child("soilHumidity").getValue():"Çalışmıyor"));
                        soilTemp.setText("Toprak Sıcaklık : " + ((d.child("stWorking").getValue() + "" ).equals("true") ? d.child("soilTemperature").getValue():"Çalışmıyor"));
                        ph.setText("ph: " + ((d.child("phWorking").getValue() + "" ).equals("true") ? d.child("ph").getValue():"Çalışmıyor"));
                        sensor_info_dialog.show();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //show a fixup module dialog
    private void show_fixup_module_dialog(){
        final Dialog d = new Dialog(SezerMainActivity.this);
        d.setContentView(R.layout.fixup_confirm_popup);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button yes_butt = d.findViewById(R.id.check_button);
        Button no_butt = d.findViewById(R.id.reject_button);

        yes_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_farm.fixup_cell_in_last_selected_place();
                d.dismiss();
            }
        });

        no_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();
    }

    private void show_popup() {

        farm_name_dialog.setContentView(R.layout.popup_recyclerview);
        product_selection_recycler_view = (RecyclerView) farm_name_dialog.findViewById(R.id.popup_rec_view);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        lm.scrollToPosition(0);
        product_selection_recycler_view.setHasFixedSize(true);
        product_selection_recycler_view.setLayoutManager(lm);

        firebase_ref = FirebaseDatabase.getInstance().getReference("User").child(user_id);
        firebase_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> fname = new ArrayList<>();
                for(DataSnapshot d : dataSnapshot.child("Modules").getChildren()){
                    String a = d.getKey() + "";
                    fname.add(a);
                }
                for(DataSnapshot d : dataSnapshot.child("Farms").getChildren()){
                    if(fname.contains(d.getKey()+"")){
                        fname.remove(d.getKey()+"");
                    }
                }
                if(fname.size() == 0){
                    Intent intent = new Intent(SezerMainActivity.this, EditUserActivity.class);
                    startActivity(intent);
                    ShortCut.displayMessageToast(SezerMainActivity.this, "Oluşturulabilecek arazi yoktur. Lütfen arazi silip tekrar deneyiniz.");
                    SezerMainActivity.this.finish();
                }
                product_selection_recycler_view_adapter = new PopupRecyclerViewAdapter(SezerMainActivity.this, fname, new OnClickFarmName() {
                    @Override
                    public void onClickFarmName(String farmName) {
                        edit_farm.getEdited_farm().farm_id = farmName;
                        farm_name_dialog.dismiss();
                    }
                });
                product_selection_recycler_view.setAdapter(product_selection_recycler_view_adapter);
                farm_name_dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
    public interface OnClickFarmName{
        public void onClickFarmName(String farmName);
    }
}