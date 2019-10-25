package com.example.kadir.agricultureprojectclientside.datatypes.farmdata;

import com.example.kadir.agricultureprojectclientside.datatypes.math.Vector2;

import java.io.Serializable;
import java.util.ArrayList;

//A CLASS THAT CAN BE USED FOR DRAWING
public class Product implements Serializable {
    public ProductData product_data;
    public ArrayList<Vector2> points;

    public Product(){
        points = new ArrayList<>();
        product_data = new ProductData();
    }


    @Override
    public boolean equals(Object obj) {
        //TODO: MAKE THIS MORE READLABLELEJAJDOAIW
        if(obj instanceof Product){
            Product m = (Product)obj;
            boolean flag = false;
            for(Vector2 v1 : m.points){
                flag = false;
                for(Vector2 v2 : points){
                    if(v1.equals(v2)){
                        flag = true;
                    }
                }
                if(flag == false){
                    return  false;
                }
            }

            return true;
        }
        return false;
    }

}
