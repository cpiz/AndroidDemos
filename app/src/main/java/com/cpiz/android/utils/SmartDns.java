package com.cpiz.android.utils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.cpiz.android.common.OkHttpHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Dns;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 智能DNS
 * 优先使用HttpDns并使用缓存优化的DNS方案
 * 缓存有效期5分钟，发生网络切换时将清空所有缓存，但最终查找失败时会尝试fallback到过期缓存中查找
 * Created by caijw on 2015/12/31.
 */
public enum SmartDns implements Dns {
    Instance;

    private static final String TAG = "SmartDns";
    private static final String HTTPDNS_API_ENDPOINT = "http://119.29.29.29";   // DnsPod
    private static Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final long LOOKUP_TIMEOUT_MS = 4500;
    private static final long TASK_WAIT_TIMEOUT_MS = 5000;
    private static final long CACHE_EXPIRE_TIME_MS = 5 * 60 * 1000;

    private final HttpDnsService mHttpDnsService;
    private final ConcurrentHashMap<String, InetAddressCache> mAddressCaches = new ConcurrentHashMap<>();
    private BroadcastReceiver mNetworkChangedReceiver = null;
    private Func1<Throwable, Observable<? extends List<InetAddress>>> mResumeError = null;

    class InetAddressCache {
        long updateTime;
        List<InetAddress> addresses;

        InetAddressCache(List<InetAddress> addresses) {
            this.updateTime = SystemClock.elapsedRealtime();
            this.addresses = addresses;
        }

        boolean isExpired() {
            return SystemClock.elapsedRealtime() > this.updateTime + CACHE_EXPIRE_TIME_MS;
        }

        void setExpire() {
            this.updateTime = 0;
        }
    }

    SmartDns() {
        mResumeError = throwable -> Observable.empty();
        mHttpDnsService = new RxServiceBuilder()
                .client(OkHttpHelper.newClient())
                .baseUrl(HTTPDNS_API_ENDPOINT)
                .create(HttpDnsService.class);
    }

    public void init(Application app) {
        if (app != null && mNetworkChangedReceiver == null) {
            mNetworkChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                        Log.d(TAG, String.format("Network changed, clear %d caches", mAddressCaches.size()));

                        for (InetAddressCache cache : mAddressCaches.values()) {
                            cache.setExpire();
                        }
                    }
                }
            };

            final IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            app.registerReceiver(mNetworkChangedReceiver, filter);
        }
    }

    @Override
    public List<InetAddress> lookup(final String hostname) throws UnknownHostException {
        Log.d(TAG, String.format("----> '%s'", hostname));

        // 优先查询本地缓存
        InetAddressCache cache = mAddressCaches.get(hostname);
        if (cache != null && !cache.isExpired()) {
            Log.d(TAG, String.format("<---- '%s'(cache)", cache.addresses));
            return cache.addresses;
        }

        // 并行查询
        final List<InetAddress> ret = new ArrayList<>();
        lookupViaConcurrentDns(hostname)
                .subscribe(addresses -> {
                    // 更新缓存
                    mAddressCaches.put(hostname, new InetAddressCache(addresses));
                    synchronized (ret) {
                        ret.addAll(addresses);
                        ret.notifyAll();
                    }
                }, throwable -> {
                    synchronized (ret) {
                        ret.notifyAll();
                    }
                }, () -> {
                    synchronized (ret) {
                        ret.notifyAll();
                    }
                });

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (ret) {
            if (!ret.isEmpty()) {
                return ret;
            }

            try {
                ret.wait(TASK_WAIT_TIMEOUT_MS);
            } catch (Throwable throwable) {
                Log.e(TAG, "", throwable);
            }

            if (ret.isEmpty()) {
                if (cache != null) {
                    // 若实在找不到，使用过期的缓存地址
                    Log.w(TAG, String.format("<---- '%s'(expired)", cache.addresses));
                    return cache.addresses;
                }
                Log.w(TAG, String.format("<---- '%s'(empty)", hostname));
                throw new UnknownHostException(String.format("Unable to resolve host '%s' by SmartHost", hostname));
            }

            Log.d(TAG, String.format("<---- '%s'", ret));
            return ret;
        }
    }

    private Observable<List<InetAddress>> lookupViaConcurrentDns(final String hostname) {
        Observable<List<InetAddress>> lookupViaSys = lookupViaSysDns(hostname)
                /*.doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.v(TAG, String.format("lookupViaSysDns onCall, hostname: '%s'", hostname));
                    }
                }).lift(new OperatorDoOnEach<>(new Subscriber<List<InetAddress>>() {
                    @Override
                    public void onNext(List<InetAddress> addresses) {
                        Log.v(TAG, String.format("lookupViaSysDns onNext, addresses: '%s'", addresses));
                    }

                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "lookupViaSysDns onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "lookupViaSysDns onError", e);
                    }
                }))*/;

        Observable<List<InetAddress>> lookupViaHttp = lookupViaHttpDns(hostname)
                /*.doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.v(TAG, String.format("lookupViaHttpDns onCall, hostname: '%s'", hostname));
                    }
                }).lift(new OperatorDoOnEach<>(new Subscriber<List<InetAddress>>() {
                    @Override
                    public void onNext(List<InetAddress> addresses) {
                        Log.v(TAG, String.format("lookupViaHttpDns onNext, addresses: '%s'", addresses));
                    }

                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "lookupViaHttpDns onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "lookupViaHttpDns onError", e);
                    }
                }))*/;

        return Observable.concatEager(lookupViaHttp, lookupViaSys)
                .first(addresses -> addresses != null && !addresses.isEmpty())
                /*.doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.v(TAG, String.format("lookupViaConcurrentDns onCall, hostname: '%s'", hostname));
                    }
                }).lift(new OperatorDoOnEach<>(new Subscriber<List<InetAddress>>() {
                    @Override
                    public void onNext(List<InetAddress> addresses) {
                        Log.v(TAG, String.format("lookupViaConcurrentDns onNext, addresses: '%s'", addresses));
                    }

                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "lookupViaConcurrentDns onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "lookupViaConcurrentDns onError", e);
                    }
                }))*/;
    }

    private Observable<List<InetAddress>> lookupViaSysDns(final String hostname) {
        return Observable.create(new Observable.OnSubscribe<List<InetAddress>>() {
            @Override
            public void call(Subscriber<? super List<InetAddress>> subscriber) {
                try {
                    List<InetAddress> ret = SYSTEM.lookup(hostname);
                    Log.d(TAG, String.format("lookupViaSysDns %s", ret));
                    subscriber.onNext(ret);
                    subscriber.onCompleted();
                } catch (UnknownHostException e) {
                    subscriber.onError(e);
                }
            }
        })
                .timeout(LOOKUP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .onErrorResumeNext(mResumeError)
                .subscribeOn(Schedulers.io());
    }

    private Observable<List<InetAddress>> lookupViaHttpDns(final String hostname) {
        if (!TextUtils.isEmpty(hostname)) {
            Matcher hrefMatcher = IP_PATTERN.matcher(hostname);
            if (hrefMatcher.matches()) {
                Log.d(TAG, String.format("lookupViaHttpDns, no need for ip(%s)", hostname));
                return Observable.empty();
            }
        }

        return mHttpDnsService
                .lookup(hostname)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .timeout(LOOKUP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .flatMap(response -> {
                    final int status = response.code();
                    if (status < 200 || status >= 300) {
                        Log.w(TAG, "response status not ok, status: " + status);
                        return Observable.empty();
                    }

                    final ResponseBody data = response.body();
                    if (data == null || data.contentLength() == 0) {
                        Log.w(TAG, "lookupViaHttpDns, empty result");
                        return Observable.empty();
                    }

                    try {
                        final List<InetAddress> ret = new ArrayList<>();
                        String[] ipArray = data.string().split(";");
                        for (String ip : ipArray) {
                            InetAddress address = InetAddress.getByName(ip);
                            ret.add(InetAddress.getByAddress(hostname, address.getAddress()));
                        }
                        Log.d(TAG, String.format("lookupViaHttpDns %s", ret));
                        return Observable.just(ret);
                    } catch (Throwable e) {
                        Log.w(TAG, "lookupViaHttpDns, invalid result", e);
                        return Observable.empty();
                    }
                })
                .onErrorResumeNext(mResumeError);
    }

    private interface HttpDnsService {
        @GET("/d")
        Observable<Response<ResponseBody>> lookup(
                @Query("dn") final String name
        );
    }
}
