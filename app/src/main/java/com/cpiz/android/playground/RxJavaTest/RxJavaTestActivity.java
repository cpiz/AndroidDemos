package com.cpiz.android.playground.RxJavaTest;

import android.os.Bundle;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 *
 * Created by caijw on 2017/2/9.
 */

public class RxJavaTestActivity extends BaseTestActivity {
    private static final String TAG = "RxJavaTestActivity";
    private PublishSubject<Integer> mSubject = null;
    private Disposable mDisposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLeftClick() {
        if (mDisposable == null || mDisposable.isDisposed()) {
            mSubject = PublishSubject.create();
            mDisposable = mSubject.subscribe(
                    x -> {
                        Log.i(TAG, x.toString());
                        appendLine(x.toString());
                    },
                    err -> Log.e(TAG, "error", err),
                    () -> Log.i(TAG, "complete"));
        }

        Log.i(TAG, "hasThrowable: " + mSubject.hasThrowable());


        mSubject.onNext(1);
        mSubject.onNext(2);
        mSubject.onError(new Throwable("error 12"));

        mSubject.onNext(3); // will not be processed
        mSubject.onNext(4);
//        mSubject.onError(new Throwable("error 34")); // Will be error
        mSubject.onNext(5);
        mSubject.onNext(6);
    }
}
