package com.example.firebasesample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private Button upload_Btn;
    private Uri imagsUri;
    private Bitmap Images;
    private ImageView imageView;
    private EditText filename;
    private TextView upload;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private DatabaseReference mDatabasereferance;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btn=findViewById(R.id.choice_image);
        imageView=findViewById(R.id.image_view);
        filename=findViewById(R.id.edittext);
        progressBar=findViewById(R.id.Progres_Bar);
        upload=findViewById(R.id.upload_textview);
        upload_Btn=findViewById(R.id.upload_button);
        storageReference= FirebaseStorage.getInstance().getReference("Uploads");
        mDatabasereferance= FirebaseDatabase.getInstance().getReference("uploads");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
//                }else{
//                    Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                    startActivityForResult(intent,2);
//                }
                fileChooser();
            }
        });
        upload_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadTask!=null && uploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "Uploded progres Please wait", Toast.LENGTH_LONG).show();
                }else{
                    UploadFile();
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),Imges.class);
                startActivity(intent);
            }
        });
    }
    public void fileChooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){

            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                System.out.println("Calisti");
                if (grantResults.length>0){
                    Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(intent,2);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (data!=null){
                if (resultCode==RESULT_OK){
                    imagsUri=data.getData();
                    Picasso.with(MainActivity.this).load(imagsUri).into(imageView);
                }
            }
        }
    }
    private String getFileextension(Uri uri){
        ContentResolver cR=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void UploadFile(){
        if (imagsUri!=null){
            final StorageReference fileReferance=storageReference.child(System.currentTimeMillis()+"."+getFileextension(imagsUri));

            uploadTask = fileReferance.putFile(imagsUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        fileReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url  = uri.toString();
                                Log.d("url", "onSuccess: "+url);
                                Toast.makeText(MainActivity.this, "Succes", Toast.LENGTH_SHORT).show();
                                String uploadID=mDatabasereferance.push().getKey();
                                Map<String,String> map = new HashMap<>();
                                map.put("name",filename.getText().toString());
                                map.put("url",url);
                                mDatabasereferance.child(uploadID).setValue(map);
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("error to upload file", "onFailure: "+e.getLocalizedMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress=(100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                }
            });;




        }else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }
}