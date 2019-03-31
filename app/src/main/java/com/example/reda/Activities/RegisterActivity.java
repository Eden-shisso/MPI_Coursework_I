package com.example.reda.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reda.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.ProgressDialog.show;

public class RegisterActivity extends AppCompatActivity {

    ImageView  ImgUserPhoto;
    static int PReqCode=1;
    static int REQUESCODE=1;


    private static final int IMAGE_PICK_CODE=1000;

    Uri pickedImgUri ;


    private EditText userEmail,userPassword,userPassword2,userName;
    private ProgressBar loadingProgress;
    private Button regbtn;

    private FirebaseAuth mAuth;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textView=findViewById(R.id.tv_goback);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, loginActivity.class));
            }
        });


        //inu views firebase

        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2=findViewById(R.id.regPassword2);
        userName= findViewById(R.id.regName);
        loadingProgress=findViewById(R.id.progressBar);

        regbtn= findViewById(R.id.regbtn);

        loadingProgress.setVisibility(View.INVISIBLE);


        mAuth= FirebaseAuth.getInstance();



        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regbtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email= userEmail.getText().toString();
                final String password= userPassword.getText().toString();
                final  String password2 = userPassword2.getText().toString();
                final String name = userName.getText().toString();


                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || !password.equals(password2)){

                    //somrthing goes wrong : all fields must be filled
                    //we need to display an error message

                    showMessage("Please Verify all fields");
                    regbtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);


                }
                else {

                    //everything is ok and all fields are filled now we can start creating user account
                    //CreateUserAccount method will try to create the user if the email is valid
                    CreateUserAccount(email,name,password);


                }
            }
        });


        //inu views

        ImgUserPhoto = findViewById(R.id.regadd);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22){
                    checkAndRequestForPermission();
                }else {
                    openGallery();
                }

            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {

        //this method create user account with specific email and password
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // user account create activity
                    showMessage("Account created");
                    //after create user account we need to update his profile picture and name
                    updateUserInfo(name ,pickedImgUri,mAuth.getCurrentUser());


                }else {
                    // account creation failed
                    showMessage("account creation failed" + task.getException().getMessage());
                    regbtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }

            }
        });




    }
    //update user photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
    //first we need to upload user photo to firebase storage and get url
   StorageReference mstorage = FirebaseStorage.getInstance().getReference().child("users_photo");
   final StorageReference imageFilePath = mstorage.child(pickedImgUri.getLastPathSegment());
   imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
       @Override
       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
      // image upload successfully
      //now we can get our image url

           imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
               @Override
               public void onSuccess(Uri uri) {
                   //url contain image url

                   UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                           .setDisplayName(name)
                           .setPhotoUri(uri)
                           .build();


                   currentUser.updateProfile(profileUpdate)
                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       //user info updated successfully
                                       showMessage("Resgister Complete");
                                       updateUI();
                                   }
                               }
                           });
               }
           });

       }
   });



    }

    private void updateUI() {
    Intent homeActivity = new Intent(getApplicationContext(),HomeActivity.class);
    startActivity(homeActivity);
    finish();

    }

    //simplify method to show Toast message
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();


    }

    private void openGallery() {

        //TODO: open gallery intent and wait for user to pick an image!


        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            //set image to image view
            ImgUserPhoto.setImageURI(data.getData());
        }

    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this,  "Please accept for required permission", Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);
            }

        }else {
            openGallery();
        }


    }
}
