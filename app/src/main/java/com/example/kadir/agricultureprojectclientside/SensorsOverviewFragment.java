package com.example.kadir.agricultureprojectclientside;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.kadir.agricultureprojectclientside.Adapter.SensorOverviewAdapter;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmSensorData;
import com.example.kadir.agricultureprojectclientside.ShortCut.OnSwipeTouchListener;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorsOverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorsOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorsOverviewFragment extends Fragment {


    RecyclerView sensorOverviewRecyclerView;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SensorsOverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SensorsOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorsOverviewFragment newInstance(String param1, String param2) {
        SensorsOverviewFragment fragment = new SensorsOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_sensors_overview, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        final String farmId = getArguments().getString("FarmId");
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        sensorOverviewRecyclerView = (RecyclerView) v.findViewById(R.id.sensors_overview_recyclerview);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        lm.scrollToPosition(0);
        sensorOverviewRecyclerView.setLayoutManager(lm);
        sensorOverviewRecyclerView.setHasFixedSize(true);
        sensorOverviewRecyclerView.setLayoutManager(lm);

        FirebaseHelper.getUserFarmSensorData(FirebaseHelper.getFirebaseUserAuthID(), farmId, new OnGetFarmSensorData() {
            @Override
            public void onFarmSensorData(ArrayList<ModuleData> moduleData) {

                SensorOverviewAdapter adapter = new SensorOverviewAdapter(farmId, getActivity(), moduleData);
                sensorOverviewRecyclerView.setAdapter(adapter);
            }

        });
        toolbar.setTitle(farmId + " Modülleri");
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
