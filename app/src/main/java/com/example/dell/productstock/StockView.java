package com.example.dell.productstock;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class StockView extends AppCompatActivity implements MyAdapter.eventCallBack {

    private Button button_add_data;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private List<ProductUpload> myProduct;
    private String info = "Click on image to update or delete product...";
    private String userID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_view);
        progressBar = findViewById(R.id.progress_bar_4);
        recyclerView = findViewById(R.id.my_stock_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myProduct = new ArrayList<>();
        progressBar.setVisibility(View.INVISIBLE);
        myAdapter = new MyAdapter(myProduct,StockView.this);
        recyclerView.setAdapter(myAdapter);
        Toast.makeText(StockView.this,info,Toast.LENGTH_LONG).show();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("ProductData").child(userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myAdapter.getData().clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ProductUpload upload = dataSnapshot1.getValue(ProductUpload.class);
                    myProduct.add(upload);
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.INVISIBLE);
                myAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        button_add_data = findViewById(R.id.button_add_data);
        button_add_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockView.this,AddNewProduct.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onImageClickListener(final ProductUpload productUpload1) {
        String dialogTitle = "Product Name: "+productUpload1.getpName() +"\n"+
                "Quantity: " + productUpload1.getpQuantity() +"\n"+
                "Import Data: "+productUpload1.getpImportDate();
        AlertDialog.Builder buttonDialog = new AlertDialog.Builder(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        Button buttonUpdate = new Button(this);
        buttonUpdate.setText("Update");
        Button buttonDelete = new Button(this);
        buttonDelete.setText("Delete");
        linearLayout.addView(buttonUpdate);
        linearLayout.addView(buttonDelete);
        buttonDialog.setMessage(dialogTitle);
        buttonDialog.setView(linearLayout);
        buttonDialog.show();
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageReference = FirebaseStorage.getInstance().getReference("ProductImages/").child(userID).child(productUpload1.getFileName());
                storageReference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Success...",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                databaseReference = FirebaseDatabase.getInstance().getReference("ProductData").child(userID).child(productUpload1.getpID());
                databaseReference
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                String msg = "Success";
                                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String msg = "Fail";
                                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockView.this, UpdateProduct.class);
                intent.putExtra("imageURL", productUpload1.getpImageUrl());
                intent.putExtra("pName", productUpload1.getpName());
                intent.putExtra("pQuantity", productUpload1.getpQuantity());
                intent.putExtra("pImportDate", productUpload1.getpImportDate());
                intent.putExtra("fileName",productUpload1.getFileName());
                intent.putExtra("fileID",productUpload1.getpID());
                startActivity(intent);
            }
        });
    }
}
