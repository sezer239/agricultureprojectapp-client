package com.example.kadir.agricultureprojectclientside.module_selector;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kadir.agricultureprojectclientside.R;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ModuleData;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Product;
import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.ProductData;

import java.util.ArrayList;


public class ProductSelectorListAdapter extends RecyclerView.Adapter<ProductSelectorListAdapter.ModuleViewHolder> {
    private ArrayList<ProductData> dataset;
    private ProductSelectorFragment.OnSelectedItemCallback callback;
    private FragmentManager fm;

    // Provide soil_temperature suitable constructor (depends on the kind of dataset)
    public ProductSelectorListAdapter(ArrayList<ProductData> dataset, ProductSelectorFragment.OnSelectedItemCallback cb , FragmentManager fm) {
        this.dataset = dataset;
        this.callback = cb;
        this.fm = fm;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup parent,
                                               final int viewType) {
        // create soil_temperature new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_text_view, parent, false);
        ModuleViewHolder vh = new ModuleViewHolder(v);
        return vh;
    }

    // Replace the contents of soil_temperature view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ModuleViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ProductData m = dataset.get(position);
        holder.productData = m;
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.on_selected_item_callback(dataset.get(position));
                fm.popBackStack();
            }
        });

        holder.mTextView.setText(m.product_name + "");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void update_list(ArrayList<ProductData> pd){
        dataset = pd;
        notifyDataSetChanged();
    }

    // Provide soil_temperature reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for soil_temperature data item in soil_temperature view holder
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ProductData productData;
//        public TextView text_detay;

        public ModuleViewHolder(View v ) {
            super(v);
            mTextView = v.findViewById(R.id.module_text);
        }
    }
}