package com.cpiz.android.playground.MessTest;

import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


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
        Consumer<Long> consumer = aLong -> Log.d(TAG, String.format("aLone = %d", aLong));
        Observable.interval(1, TimeUnit.SECONDS).subscribe(consumer);
        Observable.interval(2, TimeUnit.SECONDS).subscribe(consumer);
        Observable.interval(3, TimeUnit.SECONDS).subscribe(consumer);
    }
}
