package com.example.kadir.agricultureprojectclientside.Interfaces;


import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;

import java.util.ArrayList;

public interface OnGetFarmSensorData {
    public void onFarmSensorData(ArrayList<ModuleData> moduleData);
}
