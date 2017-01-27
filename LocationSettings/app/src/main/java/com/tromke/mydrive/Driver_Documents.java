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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tromke.mydrive.Models.DriverData;
import com.tromke.mydrive.Models.ImageCompress;
import com.tromke.mydrive.Models.UploadingImage;
import com.tromke.mydrive.util.Config;

public class Driver_Documents extends AppCompatActivity {
    ImageView DL, RC, Insurance, TaxPaid;
    ImageButton slc_Camera, slc_Gallery;
    public final String TAG = "Exception";
    Button btn_cancel;
    TextView DocOwner;
    ImageCompress imageCompress;
    Uri uri;
    Bitmap bitmap;
    ImageView imageView;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 || requestCode == 2) {
            imageCompress = new ImageCompress();

//            Log.d("FadyResult", data.getData().toString());
            if (requestCode == 1) {
                uri = data.getData();
                bitmap = imageCompress.compressImage(uri.toString(), getApplicationContext());
            } else if (requestCode == 2) {
                uri = Uri.parse(data.getExtras().get("data").toString());
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            imageView = (ImageView) findViewById(Config.Image);
            UploadingImage uploadingImage = new UploadingImage();
            uploadingImage.UpdateImage(Config.ImageDocName, Driver_Documents.this, bitmap, uri, imageView);
        }
    }


    // for versions Marchmilo and above
    // Permission for access storage
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
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
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
        setContentView(R.layout.activity_driver__documents);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Documents");
        DL = (ImageView) findViewById(R.id.DL_Image);
        RC = (ImageView) findViewById(R.id.RC_Image);
        Insurance = (ImageView) findViewById(R.id.Insurance_Image);
        TaxPaid = (ImageView) findViewById(R.id.Tax_Paid);
        DocOwner = (TextView) findViewById(R.id.documents_owner);
        final DriverData driverData = getIntent().getParcelableExtra("driver_documents");
        try {
            //get Driver DL image
            DocOwner.setText(driverData.getName().toString());
            Picasso.with(getApplicationContext()).load(driverData.getDLImage()).resize(150, 150).placeholder(R.drawable.loading_image)
                    .into(DL);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        try {
            //get Driver RC image
            Picasso.with(getApplicationContext()).load(driverData.getRCImage()).resize(150, 150).placeholder(R.drawable.loading_image)
                    .into(RC);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        try {
            //get driver Tax image
            Picasso.with(getApplicationContext()).load(driverData.getTaxPaidImage()).resize(150, 150).placeholder(R.drawable.loading_image)
                    .into(TaxPaid);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        try {
            //get Driver Insurance image
            Picasso.with(getApplicationContext()).load(driverData.getInsuranceImage()).resize(150, 150).placeholder(R.drawable.loading_image)
                    .into(Insurance);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }


        DL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                show_in_dialog(DL);
                return true;
            }
        });
        DL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    GalleryOrCamera(Driver_Documents.this);
                    Config.Image = R.id.DL_Image;
                    Config.ImageDocName = "DLImage";
                }
            }
        });
        RC.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                show_in_dialog(RC);
                return true;
            }
        });
        RC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    GalleryOrCamera(Driver_Documents.this);
                    Config.Image = R.id.RC_Image;
                    Config.ImageDocName = "RCImage";
                }
            }
        });
        Insurance.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                show_in_dialog(Insurance);
                return true;
            }
        });
        Insurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    GalleryOrCamera(Driver_Documents.this);
                    Config.Image = R.id.Insurance_Image;
                    Config.ImageDocName = "insuranceImage";
                }
            }
        });
        TaxPaid.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                show_in_dialog(TaxPaid);
                return true;
            }
        });
        TaxPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    GalleryOrCamera(Driver_Documents.this);
                    Config.Image = R.id.Tax_Paid;
                    Config.ImageDocName = "taxPaidImage";
                }
            }
        });


    }

    public void GalleryOrCamera(Context context) {
        // Alert dialog to make user choose method to upload image
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.galleryorcamera, null);
        builder.setView(root);
        builder.setTitle("Upload With");
        final AlertDialog builder1 = builder.show();
        slc_Camera = (ImageButton) root.findViewById(R.id.slc_camera);
        slc_Gallery = (ImageButton) root.findViewById(R.id.slc_gallery);
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
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 2);
                builder1.dismiss();
            }
        });
    }

    // show adialog with image
    public void show_in_dialog(ImageView img) {
        //dialog to show image in the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Driver_Documents.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.maximiz, null);
        //   LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView myimage = (ImageView) view.findViewById(R.id.myImageView);
        //  Picasso.with(getApplicationContext()).load(img).resize(400, 300).placeholder(R.drawable.loading_image).into(myimage);
        myimage.setScaleType(ImageView.ScaleType.FIT_XY);
        myimage.setImageDrawable(img.getDrawable());
        builder.setView(view);
        builder.show();
    }
}
