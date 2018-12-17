package com.example.dell.productstock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<ProductUpload> productData;
    private eventCallBack myEventCallBack;



    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView product_image;
        TextView product_name,product_quantity_in_stock,product_import_date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            product_image = itemView.findViewById(R.id.drink_image);
            product_name =  itemView.findViewById(R.id.text_product_name);
            product_quantity_in_stock = itemView.findViewById(R.id.text_quantity_left_in_stock);
            product_import_date = itemView.findViewById(R.id.text_import_date);
        }
    }
    public MyAdapter(List<ProductUpload> pData, eventCallBack eCallBack){
        this.productData = pData;
        this.myEventCallBack = eCallBack;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.stock_recyclerview_row,viewGroup,false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final ProductUpload productUpload = productData.get(position);
        viewHolder.product_name.setText("Product Name: "+productUpload.getpName());
        viewHolder.product_quantity_in_stock.setText("Quantity: "+productUpload.getpQuantity());
        viewHolder.product_import_date.setText("Import Date: "+productUpload.getpImportDate());
        Picasso.get().load(productUpload.getpImageUrl()).fit().centerCrop().into(viewHolder.product_image);
        viewHolder.product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEventCallBack.onImageClickListener(productUpload);
            }
        });
    }
    @Override
    public int getItemCount() {
        return productData.size();
    }
    public interface eventCallBack{
        void onImageClickListener(ProductUpload productUpload1);
    }

    public List<ProductUpload> getData() {
        return this.productData;
    }
}
