package com.cpiz.android.utils;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 快速创建Retrofit服务的的辅助方法
 * <p/>
 * Created by ruoshili on 7/16/15.
 */
@SuppressWarnings("unused")
public final class RxServiceBuilder {
    private static final String TAG = "RxServiceBuilder";
    private static final Gson DEFAULT_GSON = new Gson();

    private OkHttpClient mOkHttpClient = null;
    private String mBaseUrl = "http://localhost";
    private Gson mGson = DEFAULT_GSON;

    public RxServiceBuilder() {
    }

    public RxServiceBuilder client(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        return this;
    }

    public RxServiceBuilder baseUrl(String defaultBaseUrl) {
        mBaseUrl = defaultBaseUrl;
        return this;
    }

    public RxServiceBuilder gson(Gson gson) {
        mGson = gson;
        return this;
    }

    public <TService> TService create(final Class<? extends TService> serviceClass) {
        return create(serviceClass, GsonConverterFactory.create(mGson));
    }

    public <TService> TService create(final Class<? extends TService> serviceClass, final Gson gson) {
        return create(serviceClass, GsonConverterFactory.create(gson));
    }

    public <TService> TService create(final Class<? extends TService> serviceClass, final Converter.Factory converter) {
        return create(serviceClass, mBaseUrl, converter);
    }

    public <TService> TService create(final Class<? extends TService> serviceClass, final String baseUrl) {
        return create(serviceClass, baseUrl, GsonConverterFactory.create(mGson));
    }

    public <TService> TService create(final Class<? extends TService> serviceClass, final String baseUrl, final Converter.Factory converter) {
        final Retrofit retrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(converter)
                .build();
        return retrofit.create(serviceClass);
    }
}
