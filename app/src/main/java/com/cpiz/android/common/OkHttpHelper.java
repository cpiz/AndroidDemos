package com.cpiz.android.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cpiz.android.playground.PlaygroundApp;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by caijw on 2016/4/3.
 */
public class OkHttpHelper {
    private static final String TAG = "OkHttpHelper";
    private static final String HEADER_REQ_ID = "ReqId";
    private static final AtomicLong SeqGenerator = new AtomicLong(0);

    private static final HttpLoggingInterceptor LogInterceptor = new HttpLoggingInterceptor(message -> Log.v(TAG, message));
    static {
        LogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private static final Interceptor RequestInterceptor = chain -> {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo == null || !(networkInfo.isConnected() || (networkInfo.isAvailable() && networkInfo.isConnectedOrConnecting()))) {
            throw new ConnectException("Network is disconnected");
        }

        // 为每一个请求唯一reqId
        final String reqId = String.format("%05d.%s", SeqGenerator.getAndIncrement(), networkInfo.getTypeName());

        final Request preRequest = chain.request();
        Request request = preRequest.newBuilder()
                .url(preRequest.url())
                .addHeader(HEADER_REQ_ID, reqId)
                .build();

        long beginTime = System.nanoTime();
        onBeforeRequest(reqId, request);

        try {
            Response response = chain.proceed(request);
            onAfterRequest(reqId, request, response, beginTime, System.nanoTime());
            return response;
        } catch (IOException ex) {
            onRequestError(reqId, request, beginTime, System.nanoTime(), ex);
            throw ex;
        }
    };

    public static OkHttpClient newClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(RequestInterceptor)
                .addInterceptor(LogInterceptor)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    private static NetworkInfo getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PlaygroundApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    private static void onBeforeRequest(final String reqId, final Request request) {
        Log.v(TAG, String.format("Before--> %s|%s %s", reqId, request.method(), request.url().toString()));
    }

    private static void onRequestError(final String reqId, final Request request, long beginNanoTime, long endNanoTime, final Exception ex) {
        final String method = request.method();
        final String url = request.url().toString();
        Log.e(TAG, String.format("Error<-- %s|%s %s in %.1fms", reqId, method, url, (endNanoTime - beginNanoTime) / 1e6d), ex);
    }

    private static void onAfterRequest(final String reqId, final Request request, final Response response, long beginNanoTime, long endNanoTime) {
        final String method = request.method();
        final String url = request.url().toString();
        final String message = response.message();
        final int httpCode = response.code();

        Log.println(httpCode >= 400 ? Log.WARN : Log.VERBOSE, TAG, String.format("After<-- %s|%s %s, code:%d, message:'%s', cost %.1fms",
                reqId, method, url, httpCode, message, (endNanoTime - beginNanoTime) / 1e6d));
    }
}
