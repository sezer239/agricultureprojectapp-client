package com.example.kadir.agricultureprojectclientside.Interfaces;


import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;

import java.util.ArrayList;

public interface OnGetFarmCallback {
    public void onGetFarmCallback(ArrayList<Farm> farmdata);
}
