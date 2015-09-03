package com.cpiz.android.utils;

import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStreamReader;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

/**
 * 快速创建Retrofit服务的的辅助方法
 * <p/>
 * Created by ruoshili on 7/16/15.
 */
public final class RxServicesFactory {
    private static final String TAG = RxServicesFactory.class.getSimpleName();

    private static final String DEFAULT_ENDPOINT = "https://api.github.com";

    public static <TService> TService getService(Class<? extends TService> serviceClass) {
        return getService(serviceClass, new GsonConverter(new Gson()), new DefaultErrorHandler());
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, Gson gson) {
        return getService(serviceClass, new GsonConverter(gson), new DefaultErrorHandler());
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, Gson gson, ErrorHandler errorHandler) {
        return getService(serviceClass, new GsonConverter(gson), errorHandler);
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, ErrorHandler errorHandler) {
        return getService(serviceClass, new GsonConverter(new Gson()), errorHandler);
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, Converter converter) {
        return getService(serviceClass, converter, new DefaultErrorHandler());
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, Converter converter, ErrorHandler errorHandler) {
        return getService(serviceClass, DEFAULT_ENDPOINT, converter, errorHandler);
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, String endpoint) {
        return getService(serviceClass, endpoint, new GsonConverter(new Gson()), new DefaultErrorHandler());
    }

    public static <TService> TService getService(Class<? extends TService> serviceClass, String endpoint, Converter converter, ErrorHandler errorHandler) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(converter)
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String message) {
                        Log.v(TAG, message);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient())
                .setErrorHandler(errorHandler)
                .build();

        return restAdapter.create(serviceClass);
    }

    private static class DefaultErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            Log.e(TAG, String.format("access %s failed.", cause.getUrl()), cause);

            try {
                Response r = cause.getResponse();
                if (r == null) {
                    Log.e(TAG, String.format("Response is null for accessing %s.", cause.getUrl()));
                    return cause;
                }

                TypedInput body = r.getBody();
                if (body != null) {
                    StringBuilder logBuilder = new StringBuilder(1024 + (int) body.length());
                    logBuilder.append("Url: ").append(r.getUrl()).append("\n");
                    logBuilder.append("Error code: ").append(r.getStatus()).append("\n");

                    char[] buffer = new char[1024];
                    InputStreamReader reader = null;

                    try {
                        reader = new InputStreamReader(body.in());
                        do {
                            int count = reader.read(buffer, 0, 1024);

                            if (count < 0) {
                                break;
                            }

                            logBuilder.append(buffer, 0, count);

                            Log.e(TAG, logBuilder.toString(), cause);
                        } while (true);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "handleError failed.", e);
            }

            return cause;
        }
    }

}
