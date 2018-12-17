package com.example.dell.productstock;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UpdateProduct extends AppCompatActivity {
    private ImageView imageView;
    private EditText textProductName,textProductQuantity,textImportDate;
    private Button buttonUpdate;
    private ProgressBar progressBar;
    private TextView textInfo;
    String url = "", pName = "", pQuantity = "", pImportDate = "", fileName = "", fileID = "";
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private StorageTask storageTask;
    private FirebaseAuth firebaseAuth;
    private final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);
        imageView = findViewById(R.id.img_product_image_1);
        textProductName = findViewById(R.id.text_product_name_1);
        textProductQuantity = findViewById(R.id.text_product_quantity_in_stock_1);
        textImportDate = findViewById(R.id.text_import_date_1);
        buttonUpdate = findViewById(R.id.button_update_1);
        progressBar = findViewById(R.id.progress_bar_4);
        textInfo = findViewById(R.id.text_info);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        url = getIntent().getStringExtra("imageURL");
        pName = getIntent().getStringExtra("pName");
        pQuantity = getIntent().getStringExtra("pQuantity");
        pImportDate = getIntent().getStringExtra("pImportDate");
        fileName = getIntent().getStringExtra("fileName");
        fileID = getIntent().getStringExtra("fileID");

        Picasso.get().load(url).fit().centerCrop().into(imageView);
        textProductName.setText(pName);
        textProductQuantity.setText(pQuantity);
        textImportDate.setText(pImportDate);
        buttonUpdate.setEnabled(false);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFileFromDevice();
                buttonUpdate.setEnabled(true);
                textInfo.setVisibility(View.INVISIBLE);
            }
        });
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProductData();
                buttonUpdate.setEnabled(false);
                textInfo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateProductData() {
        progressBar.setVisibility(View.VISIBLE);
        //final String filename = System.currentTimeMillis()+"."+getFileExtension(imageUri);
        storageReference = FirebaseStorage.getInstance().getReference("ProductImages/").child(userID).child(fileName);
        if (imageUri != null){
            storageTask = storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            FirebaseStorage.getInstance()
                                    .getReference()
                                    .child("ProductImages")
                                    .child(userID)
                                    .child(fileName)
                                    .getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            insertToRealTimeDatabase(task.getResult().toString(),fileName);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getApplicationContext(),"Fail to " +
                                                    "get Image URL",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        }
    }

    private void insertToRealTimeDatabase(final String getUrl, String fileName){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductData")
                .child(userID)
                .child(fileID);
        ProductUpload productUpload = new ProductUpload(getUrl, textProductName.getText().toString().trim(),
                textProductQuantity.getText().toString().trim(), textImportDate.getText().toString().trim(), fileName, fileID);
        databaseReference.child(fileID).setValue(productUpload);
        FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductData")
                .child(userID)
                .child(fileID)
                .setValue(productUpload)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String msg = "successfull save data to cloud storage";
                        Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        String msg = "Fail to save data to cloud storage";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void chooseFileFromDevice() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).fit().centerCrop().into(imageView);
        }
        else {
            Toast.makeText(getApplicationContext(),"No file selected",Toast.LENGTH_SHORT).show();
        }
    }
    /*private String getFileExtension(Uri imageUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(imageUri));
    }*/
}
