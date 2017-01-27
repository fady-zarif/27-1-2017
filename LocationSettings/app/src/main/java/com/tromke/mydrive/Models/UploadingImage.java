package com.tromke.mydrive.Models;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Created by foda_ on 2017-01-19.
 */

public class UploadingImage {
    StorageReference storageRef;
    ProgressDialog progressDialog;

    public void UpdateImage(final String Doc, final Context context, final Bitmap bitmap, final Uri uri, final ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://driver-app-b6825.appspot.com");
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading .. ");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] array = stream.toByteArray();
        Random random = new Random();
        int x = random.nextInt(20500);
        UploadTask uploadTask = storageRef.child("driver/" + uri.getLastPathSegment() + x).putBytes(array);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                updatefirebase(Doc,taskSnapshot.getDownloadUrl().toString(), imageView, bitmap, context);
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatefirebase(final String Doc, final String updateUrl, final ImageView imageView, final Bitmap bitmap, final Context context) {
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Driver");

        Query query = reference.orderByChild("UUID").equalTo(Uid);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                reference.child(key).child(Doc).setValue(updateUrl);
                imageView.setImageBitmap(bitmap);
                progressDialog.dismiss();
                Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
