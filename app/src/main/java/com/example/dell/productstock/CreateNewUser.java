package com.example.dell.productstock;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

public class CreateNewUser extends AppCompatActivity {
    private EditText newEmail, newPassword;
    private Button buttonCreateAccount;
    private ImageView imageGoogleAccount;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private StorageTask storageTask;
    private static final int RC_SIGN_IN = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_user);
        newEmail = findViewById(R.id.text_new_email);
        newPassword = findViewById(R.id.text_new_password);
        buttonCreateAccount = findViewById(R.id.button_create_account);
        imageGoogleAccount = findViewById(R.id.image_google_logo);
        progressBar = findViewById(R.id.progress_bar_2);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.api))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
        imageGoogleAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogleAccount();
            }
        });
    }

    private void signInWithGoogleAccount() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                Toast.makeText(getApplicationContext(),"Failed to sign in with google account.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CreateNewUser.this, StockView.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(CreateNewUser.this, "sign in completed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CreateNewUser.this, "sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewAccount() {
        progressBar.setVisibility(View.VISIBLE);
        final String email = newEmail.getText().toString().trim();
        String password = newPassword.getText().toString().trim();
        if (!email.isEmpty() && !password.isEmpty()){
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.INVISIBLE);
                                String msg = "Congratulations, account was successfully created!";
                                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).cancel();
                                Intent intent = new Intent(CreateNewUser.this, StockView.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                progressBar.setVisibility(View.INVISIBLE);
                                new AlertDialog.Builder(CreateNewUser.this)
                                        .setTitle("Alert: ")
                                        .setMessage("Please make sure you have enter a valid email and password."+
                                                "\n"+"Your password must contain at least 6 characters or numbers")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                                newEmail.setText("");
                                                newPassword.setText("");
                                                newEmail.requestFocus();
                                            }
                                        })
                                        .show();
                            }
                        }
                    });

        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            String errorMsg = "Make sure you've correctly fill in the required information!";
            new AlertDialog.Builder(CreateNewUser.this)
                    .setTitle("Error: ")
                    .setMessage(errorMsg)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }
    }

}
