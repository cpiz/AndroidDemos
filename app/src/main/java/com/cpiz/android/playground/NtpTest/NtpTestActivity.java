package com.cpiz.android.playground.NtpTest;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;

import org.joda.time.DateTime;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by caijw on 2015/9/27.
 */
public class NtpTestActivity extends BaseTestActivity {
    private static final String TAG = "NtpTestActivity";
    private static String[] NTP_SERVERS = new String[]{
            "0.asia.pool.ntp.org",
            "ntp.sjtu.edu.cn",
            "202.120.2.101",
            "s1a.time.edu.cn",
            "s1a.time.edu.cn",
            "1.asia.pool.ntp.org",
            "s2k.time.edu.cn",
            "s2k.time.edu.cn",
            "s2g.time.edu.cn",
            "3.cn.pool.ntp.org",
            "s2g.time.edu.cn",
            "cn.pool.ntp.org",
            "0.cn.pool.ntp.org",
            "1.cn.pool.ntp.org",
            "s2c.time.edu.cn",
            "timekeeper.isi.edu",
            "s2f.time.edu.cn",
            "time.nist.gov",
            "utcnist.colorado.edu",
            "time-b.timefreq.bldrdoc.gov",
            "time-nw.nist.gov",
            "ntp0.fau.de",
            "time-b.nist.gov",
            "time.nist.gov",
            "time-a.nist.gov",
            "swisstime.ethz.ch",
            "time-a.timefreq.bldrdoc.gov",
            "time-nw.nist.gov",
            "usno.pa-x.dec.com",
    };

    private SntpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new SntpClient();
    }

    @Override
    public void onLeftClick() {
        Observable.fromArray(NTP_SERVERS)
                .flatMap(new Function<String, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(@NonNull String s) throws Exception {
                        return Observable.create(e -> e.onNext(mClient.requestTime(s, 2000)));
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                            if (aBoolean) {
                                long now = mClient.getNtpTime() + SystemClock.elapsedRealtime() - mClient.getNtpTimeReference();
                                appendLine((new DateTime(now)).toString("YYYY-MM-dd HH:mm:ss.SSS"));
                            } else {
                                appendLine("Sync time failed");
                            }
                        }
                        , throwable -> {
                            appendLine("Sync time error");
                            Log.e(TAG, "Sync time error", throwable);
                        }
                );
    }
}
