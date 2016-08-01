package cz.uhk.cityunavigate.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import cz.uhk.cityunavigate.R;

/**
 * Utility methods
 */
public class Util {

    public static final int REQUEST_ACTIVITY_PICK_PHOTO = 111;

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            Log.e("MD5", "Error hashing to MD5, no such algorithm");
            return "";
        }
    }

    /**
     * Prepare progressDialog
     * @param context
     * @param resourceString
     * @return
     */
    public static ProgressDialog progressDialog(Context context, int resourceString) {
        return progressDialog(context, context.getResources().getString(resourceString));
    }

    /**
     * Prepare progressDialog
     * @param context
     * @param text
     * @return
     */
    public static ProgressDialog progressDialog(Context context, String text) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static Promise<Uri> uploadPicture(final Activity activity, final ContentResolver contentResolver, final Uri uri, final String directory, final int width) throws IOException {

        final Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);

        final PromiseImpl<Uri> promiseThumbnail = new PromiseImpl<>();

        final ProgressDialog progressDialog = Util.progressDialog(activity, R.string.firebase_picture_uploading);
        progressDialog.show();

        new AsyncTask<Bitmap, Void, Void>() {

            @Override
            protected Void doInBackground(Bitmap... bitmaps) {

                Bitmap bitmap = bitmaps[0];

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                int height = (int) (width / ratio);

                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 60, byteArrayOutputStream);

                byte[] bytes = byteArrayOutputStream.toByteArray();

                bitmap.recycle();

                final StorageReference storageReference = FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child(directory)
                        .child(UUID.randomUUID().toString() + ".png");

                UploadTask uploadTask = storageReference.putBytes(bytes);
                uploadTask
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull final Exception exception) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                                promiseThumbnail.reject(exception);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(activity, "Success", Toast.LENGTH_LONG).show();
                                    }
                                });

                                promiseThumbnail.resolve(Uri.parse(taskSnapshot.getMetadata().getReference().toString()));
                            }
                        });

                return null;
            }

        }.execute(bitmap);

        return promiseThumbnail;
    }

}
