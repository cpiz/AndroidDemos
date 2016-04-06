package com.cpiz.android.playground.OkHttpSpeedLimit;

import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.playground.PlayHelper;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                OkHttpClient client = new OkHttpClient.Builder()
                        .addNetworkInterceptor(new StethoInterceptor())
                        .build();

                String imagePath = PlayHelper.getLatestPicture(OkHttpTestActivity.this);

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", imagePath, LimitSpeedRequestBody.create(MediaType.parse("*/*"), new File(imagePath)))
                        .build();
                Request request = new Request.Builder()
                        .url("http://172.25.55.8/temp/")
                        .post(body)
                        .build();
                try {
                    appendLine(String.format("Start upload file [%s]", imagePath));
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    appendLine(String.format("Upload file [%s] success", imagePath));
                } catch (IOException ex) {
                    Log.e(TAG, String.format("Upload file [%s] failed", imagePath), ex);
                }
            }
        }).start();
    }
}
