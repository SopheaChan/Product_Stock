package com.example.dell.productstock;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class AddNewProduct extends AppCompatActivity {

    private EditText pName,pQuantity,pImportDate;
    private ImageView pImageView;
    private Button buttonUpload;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private StorageTask storageTask;
    private int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_product);

        pName = findViewById(R.id.text_product_name);
        pQuantity = findViewById(R.id.text_product_quantity_in_stock);
        pImportDate = findViewById(R.id.text_import_date);
        pImageView = findViewById(R.id.img_product_image);
        buttonUpload = findViewById(R.id.button_upload);
        progressBar = findViewById(R.id.progress_bar_3);
        progressBar.setVisibility(View.INVISIBLE);
        buttonUpload.setEnabled(false);
        pImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFileFromDevice();
                buttonUpload.setEnabled(true);
            }
        });
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDataToStorage();
                buttonUpload.setEnabled(false);
            }
        });
    }
    public void uploadDataToStorage() {
        progressBar.setVisibility(View.VISIBLE);
        final String fileName = System.currentTimeMillis()+"."+getFileExtension(imageUri);
        firebaseAuth = FirebaseAuth.getInstance();
        final String userID = firebaseAuth.getUid();
        if (imageUri != null){
            storageReference = FirebaseStorage.getInstance()
                    .getReference()
                    .child("ProductImages")
                    .child(userID)
                    .child(fileName);
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
                                            insertToRealTimeDatabase(task.getResult().toString(),fileName, userID);
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
    private void insertToRealTimeDatabase(final String getUrl, String fileName, String uID){
        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductData")
                .child(uID);
        String productID = databaseReference.push().getKey();
        ProductUpload productUpload = new ProductUpload(getUrl, pName.getText().toString().trim(),
                pQuantity.getText().toString().trim(), pImportDate.getText().toString().trim(), fileName, productID);
        databaseReference.child(productID).setValue(productUpload);
        FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductData")
                .child(uID)
                .child(productID)
                .setValue(productUpload)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.INVISIBLE);
                        String msg = "successfull save data to cloud storage";
                        Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        String msg = "Fail to save data to cloud storage";
                        pName.setText("");
                        pQuantity.setText("");
                        pImportDate.setText("");
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
            Picasso.get().load(imageUri).fit().centerCrop().into(pImageView);
        }
    }
    private String getFileExtension(Uri imageUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(imageUri));
    }
}
