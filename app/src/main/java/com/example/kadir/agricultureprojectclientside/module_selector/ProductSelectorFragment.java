package com.example.kadir.agricultureprojectclientside.module_selector;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kadir.agricultureprojectclientside.FirebaseHelper;
import com.example.kadir.agricultureprojectclientside.Interfaces.OnGetFarmSensorData;
import com.example.kadir.agricultureprojectclientside.R;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Product;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ProductData;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;

public class ProductSelectorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, ph.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private OnSelectedItemCallback on_item_selected_callback;

    private RecyclerView recycler_view;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layout_manager;

    private EditText product_search_text;

    private ArrayList<ProductData> product_data;

    public ProductSelectorFragment() {

    }

    public static ProductSelectorFragment newInstance(String param1, String param2) {
        ProductSelectorFragment fragment = new ProductSelectorFragment();
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
        View v = inflater.inflate(R.layout.fragment_product_selector, container, false);

        recycler_view = v.findViewById(R.id.modlue_list_view);
        product_search_text = v.findViewById(R.id.product_search_text_view);

        product_search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ArrayList<ProductData> filtered_Data= new ArrayList<>();

                for(ProductData p : product_data){
                    if(p.product_name.toLowerCase().contains(s.toString().toLowerCase())){
                        filtered_Data.add(p);
                    }
                }
                ((ProductSelectorListAdapter)adapter).update_list(filtered_Data);
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recycler_view.setHasFixedSize(true);

        // use soil_temperature linear layout manager
        layout_manager = new LinearLayoutManager(getContext());
        recycler_view.setLayoutManager(layout_manager);
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

    public OnSelectedItemCallback getOn_item_selected_callback() {
        return on_item_selected_callback;
    }

    public void setOn_item_selected_callback(OnSelectedItemCallback on_item_selected_callback) {
        this.on_item_selected_callback = on_item_selected_callback;
        //  adapter.notifyDataSetChanged();
    }

    public interface OnSelectedItemCallback {
        void on_selected_item_callback(ProductData moduleData);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <soil_temperature href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</soil_temperature> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void load_product_data() {
        FirebaseHelper.getAllProductData(new FirebaseHelper.OnProductDataLoaded() {
            @Override
            public void productDataLoaded(ArrayList<ProductData> productData) {
                product_data = productData;
                adapter = new ProductSelectorListAdapter(productData, on_item_selected_callback, getFragmentManager());
                recycler_view.setAdapter(adapter);
            }
        });
    }
}
