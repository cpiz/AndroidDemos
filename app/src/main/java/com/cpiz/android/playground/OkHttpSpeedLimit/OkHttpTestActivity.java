package com.cpiz.android.playground.OkHttpSpeedLimit;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

/**
 * Created by caijw on 2015/9/7.
 */
public class OkHttpTestActivity extends BaseTestActivity {
    private static final String TAG = "OkHttpTestActivity";

    @Override
    public void onLeftClick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                client.networkInterceptors().add(new StethoInterceptor());

                String imagePath = getRecentPicture();

                RequestBody body = new MultipartBuilder()
                        .addFormDataPart("file", imagePath, LimitRequestBody.create(MediaType.parse("*/*"), new File(imagePath)))
                        .type(MultipartBuilder.FORM)
                        .build();

                Request request = new Request.Builder()
                        .url("http://172.23.32.17:8080/temp/")
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Log.i(TAG, String.format("Upload file [%s] success", imagePath));
                } catch (IOException ex) {
                    Log.e(TAG, String.format("Upload file [%s] failed", imagePath), ex);
                }
            }
        }).start();
    }

    public String getRecentPicture() {
        String filePath = null;

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        // Put it in the image view
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(1);
        }
        cursor.close();

        return filePath;
    }
}
