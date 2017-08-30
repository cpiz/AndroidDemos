package com.cpiz.android.playground.RxBusTest;

import android.os.Bundle;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxBus;

/**
 * 实现一个基于RxBus的事件总线
 * <p>
 * Created by caijw on 2015/8/31.
 */
public class RxBusTestActivity extends BaseTestActivity {
    private static final String TAG = "RxBusTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getEdit().setText("Click button to post test events, " +
                "register and filter the event type, " +
                "then append the event content into edit text\n" +
                "--------------------\n");

        RxBus.getDefault().post(new TestEvent("test 0"));   // post before register

        RxBus.getDefault().register(TestEvent.class, this)
                .doOnSubscribe((disposable) -> Log.i(TAG, "registerOnActivity doOnSubscribe"))
                .doOnDispose(() -> Log.i(TAG, "registerOnActivity doOnDispose"))
                .doOnTerminate(() -> Log.i(TAG, "registerOnActivity doOnTerminate"))
                .subscribe(testEvent -> appendLine(String.format("Got event[%s] registerOnActivity", testEvent.getEvent())));

        RxBus.getDefault().register(TestEvent.class, getLeftBtn())
                .doOnSubscribe((disposable) -> Log.i(TAG, "registerOnView doOnSubscribe"))
                .doOnDispose(() -> Log.i(TAG, "registerOnView doOnDispose"))
                .doOnTerminate(() -> Log.i(TAG, "registerOnView doOnTerminate"))
                .subscribe(testEvent -> appendLine(String.format("Got event[%s] registerOnView", testEvent.getEvent())),
                        throwable -> {
                        });
    }

    @Override
    public void onLeftClick() {
        RxBus.getDefault().post(new TestEvent("test 1"));
        RxBus.getDefault().post(null);
        RxBus.getDefault().post(new TestEvent("test 2"));
    }

    private class TestEvent {
        private String mEvent;

        TestEvent(String event) {
            this.mEvent = event;
        }

        String getEvent() {
            return mEvent;
        }
    }
}
