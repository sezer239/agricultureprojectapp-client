package com.example.kadir.agricultureprojectclientside.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.FarmEditView;
import com.example.kadir.agricultureprojectclientside.FirebaseHelper;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmCallback;
import com.example.kadir.agricultureprojectclientside.R;
import com.example.kadir.agricultureprojectclientside.SezerMainActivity;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<FarmEditView> farmOutlineView;
    LayoutInflater layoutInflater;
    Context context;
    String currentUserId;

    public CustomAdapter(ArrayList<FarmEditView> farmOutlineView, Context context, String currentUserId) {
        this.context = context;
        this.farmOutlineView = farmOutlineView;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.edit_user_page_recycler_view_row, viewGroup, false);
        ViewHolder view_holder = new ViewHolder(v);

        return view_holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.linearLayout.addView(farmOutlineView.get(i));
        viewHolder.textView.setText(farmOutlineView.get(i).getEdited_farm().farm_id + "");

        farmOutlineView.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("CCC", "Onclick çalışıyor");
                FirebaseHelper.getUserAllFarmSingle(currentUserId, new OnGetFarmCallback() {
                    @Override
                    public void onGetFarmCallback(ArrayList<Farm> farmdata) {

                        Bundle extras = new Bundle();
                        extras.putSerializable("Farm", farmdata.get(i));
                        extras.putString("userID", currentUserId);
                        Intent intent = new Intent(context, SezerMainActivity.class);
                        intent.putExtras(extras);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    }
                });

            }
        });
    }

    public FarmEditView getFarmAt(int position) {
        return farmOutlineView.get(position);
    }

    @Override
    public int getItemCount() {
        return farmOutlineView.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linear_recyclerview_rows);
            textView = itemView.findViewById(R.id.farm_name_txt);
        }
    }


}
