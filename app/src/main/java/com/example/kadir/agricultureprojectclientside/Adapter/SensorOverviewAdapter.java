package com.example.kadir.agricultureprojectclientside.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.FirebaseHelper;
import com.example.kadir.agricultureprojectclientside.R;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;

import java.util.ArrayList;

public class SensorOverviewAdapter extends RecyclerView.Adapter<SensorOverviewAdapter.ViewHolder> {
    String farm_id;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<ModuleData> moduleData;
    public SensorOverviewAdapter(String farm_id, Context context, ArrayList<ModuleData> moduleData){
        this.context = context;
        this.farm_id = farm_id;
        this.moduleData = moduleData;
    }
    @NonNull
    @Override
    public SensorOverviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.sensors_overview_recview_row, viewGroup, false);
        SensorOverviewAdapter.ViewHolder view_holder = new SensorOverviewAdapter.ViewHolder(v);

        return view_holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SensorOverviewAdapter.ViewHolder viewHolder, int i) {

        viewHolder.moduleId.setText("Modul No : "                 +   moduleData.get(i).module_id);
        viewHolder.airHum.  setText("Hava Nem        : "          + ((moduleData.get(i).ah_working) == true ? moduleData.get(i).air_humidity     + "%"       : "Çalışmıyor"));
        viewHolder.airTemp. setText("Hava sıcaklık   : "          + ((moduleData.get(i).at_working) == true ? moduleData.get(i).air_temperature  + " \u2103" : "Çalışmıyor"));
        viewHolder.soilHum. setText("Toprak nemi     : "          + ((moduleData.get(i).sh_working) == true ? moduleData.get(i).soil_humidity    + "%"       : "Çalışmıyor"));
        viewHolder.soilTemp.setText("Toprak sıcaklık: "           + ((moduleData.get(i).st_working) == true ? moduleData.get(i).soil_temperature + " \u2103" : "Çalışmıyor"));
        viewHolder.ph.      setText("ph                       : " + ((moduleData.get(i).ph_working) == true ? moduleData.get(i).ph                           : "Çalışmıyor"));
        viewHolder.linearLayout.setTag(viewHolder);

    }

    @Override
    public int getItemCount() {
        return moduleData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView moduleId, airHum, airTemp, soilHum, soilTemp, ph;
        LinearLayout linearLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleId = itemView.findViewById(R.id.moduleId);
            airHum = itemView.findViewById(R.id.air_hum);
            airTemp = itemView.findViewById(R.id.air_temp);
            soilHum = itemView.findViewById(R.id.soil_hum);
            soilTemp = itemView.findViewById(R.id.soil_temp);
            ph = itemView.findViewById(R.id.ph);
            linearLayout = itemView.findViewById(R.id.sensor_overview_row);
        }
    }
}
