package com.cpiz.android.playground.RxBusTest;

import android.os.Bundle;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxBus;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * 实现一个基于RxBus的事件总线
 *
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

        RxBus.getDefault().registerOnActivity(TestEvent.class, this)
                .doOnSubscribe(() -> Log.i(TAG, "registerOnActivity doOnSubscribe"))
                .doOnUnsubscribe(() -> Log.i(TAG, "registerOnActivity doOnUnsubscribe"))
                .doOnTerminate(() -> Log.i(TAG, "registerOnActivity doOnTerminate"))
                .subscribe(testEvent -> {
                    appendLine(String.format("Got event[%s] registerOnActivity", testEvent.getEvent()));
                });

        RxBus.getDefault().registerOnView(TestEvent.class, getLeftBtn())
                .doOnSubscribe(() -> Log.i(TAG, "registerOnView doOnSubscribe"))
                .doOnUnsubscribe(() -> Log.i(TAG, "registerOnView doOnUnsubscribe"))
                .doOnTerminate(() -> Log.i(TAG, "registerOnView doOnTerminate"))
                .subscribe(testEvent -> {
                    appendLine(String.format("Got event[%s] registerOnView", testEvent.getEvent()));
                }, throwable -> {
                });
    }

    @Override
    public void onLeftClick() {
        RxBus.getDefault().post(new TestEvent("test 1"));
        RxBus.getDefault().post(null);
        RxBus.getDefault().post(new TestEvent("test 2"));
    }

    class TestEvent {
        private String mEvent;

        public TestEvent(String event) {
            this.mEvent = event;
        }

        public String getEvent() {
            return mEvent;
        }
    }
}
