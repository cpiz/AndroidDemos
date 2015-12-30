package com.cpiz.android.playground.MessTest;

import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;

import java.util.concurrent.TimeUnit;

import rx.Subscriber;

/**
 * Created by caijw on 2015/10/16.
 *
 * @see <a href="#https://github.com/greenrobot/EventBus/blob/master/HOWTO.md">EventBus How-To</a>
 */
public class MessTestActivity extends BaseTestActivity {
    private static final String TAG = "MessTestActivity";

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLeftClick() {
        Subscriber<Long> subscriber = new Subscriber<Long>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError", e);
            }

            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, String.format("aLone = %d", aLong));
            }
        };

        rx.Observable.interval(1, TimeUnit.SECONDS).subscribe(subscriber);
        rx.Observable.interval(2, TimeUnit.SECONDS).subscribe(subscriber);
        rx.Observable.interval(3, TimeUnit.SECONDS).subscribe(subscriber);
    }
}
