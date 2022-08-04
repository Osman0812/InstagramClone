package com.example.instagramclone.view.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionResultLauncher;
    Bitmap image;
    ActivityUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        resultLauncher();

        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

    }
    public void uploadClick(View view){

        if (imageData!= null){

            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".jpg";

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Download url
                    StorageReference newReference = firebaseStorage.getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            String comment = binding.commentText.getText().toString();

                            FirebaseUser user = auth.getCurrentUser();
                            String email = user.getEmail();
                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent = new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    Toast.makeText(UploadActivity.this,"Succeed",Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   Toast.makeText(UploadActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.upload_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.quit){
            Intent intent = new Intent(this,FeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void selectImage(View view){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Needed permission for access",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //permission
                        permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            }else {
                //permission
                permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else {
            //goto gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }

    }



    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / height;

        if (bitmapRatio > 1){
            // Landscape

            width = maximumSize;
            height = (int) bitmapRatio / width;

        }else {
            // Portrait
            height = maximumSize;
            width = (int) bitmapRatio * height;

        }

        return Bitmap.createScaledBitmap(image,width,height,true);

    }

    public void resultLauncher(){

        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //permission granted
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                }else {
                    //permission denied
                    Toast.makeText(UploadActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                }
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent intentFromGallery = result.getData();
                    if (intentFromGallery != null){
                       imageData = intentFromGallery.getData();
                       try {
                           if (Build.VERSION.SDK_INT >= 28){
                               ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                               image = ImageDecoder.decodeBitmap(source);
                           }else {
                               image = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                           }
                           binding.imageView.setImageBitmap(image);
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                    }
                }
            }
        });

    }
}