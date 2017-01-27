package com.tromke.mydrive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.tromke.mydrive.Models.DriverData;
import com.tromke.mydrive.Models.ImageCompress;
import com.tromke.mydrive.Models.UploadingImage;
import com.tromke.mydrive.util.Config;

public class Driver_Profile extends AppCompatActivity {
    ImageView ProfileImage;
    TextView driverEmail, driverName, driverPhone;
    Uri uri;
    ImageCompress imageCompress;
    Bitmap bitmap;
    ImageView imageView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageCompress = new ImageCompress();

            if (requestCode == 1) {
                uri = data.getData();
                bitmap = imageCompress.compressImage(uri.toString(), getApplicationContext());
            } else {
                uri = Uri.parse(data.getExtras().get("data").toString());
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            imageView = (ImageView) findViewById(Config.Image);
            UploadingImage uploadingImage = new UploadingImage();
            uploadingImage.UpdateImage(Config.ImageDocName, Driver_Profile.this, bitmap, uri, imageView);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED
                    &&
                    checkSelfPermission(Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                    ) {
                Log.v("w", "Permission is granted");
                return true;
            } else {

                Log.v("w", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.CAMERA}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("w", "Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver__profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
        DriverData driverData = getIntent().getParcelableExtra("driver_profile");
        ProfileImage = (ImageView) findViewById(R.id.driver_profile);
        driverEmail = (TextView) findViewById(R.id.driver_email);
        driverName = (TextView) findViewById(R.id.driver_name);
        driverPhone = (TextView) findViewById(R.id.driver_phone);


        //get driver data from parcelable and set data
        try {
            driverEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
            driverName.setText(driverData.getName().toString());
            driverPhone.setText(driverData.getPhone().toString());
            Picasso.with(getApplicationContext()).load(driverData.getProfileImage()).resize(150, 150).placeholder(R.drawable.profile)
                    .into(ProfileImage);
        } catch (Exception ex) {

        }

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    Config.Image = R.id.driver_profile;
                    Config.ImageDocName = "profileImage";
                    GalleryOrCamera(Driver_Profile.this);
                }
            }
        });
    }

    public void GalleryOrCamera(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.galleryorcamera, null);
        builder.setView(root);
        builder.setTitle("Upload With");
        final AlertDialog builder1 = builder.show();
        ImageButton slc_Camera = (ImageButton) root.findViewById(R.id.slc_camera);
        ImageButton slc_Gallery = (ImageButton) root.findViewById(R.id.slc_gallery);
        slc_Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                setResult(RESULT_OK, intent);
                startActivityForResult(intent, 1);
                builder1.dismiss();
            }
        });
        slc_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 2);
                builder1.dismiss();
            }
        });
    }

}
