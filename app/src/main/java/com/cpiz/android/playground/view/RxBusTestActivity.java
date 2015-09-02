package com.cpiz.android.playground.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxBus;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by caijw on 2015/8/31.
 */
public class RxBusTestActivity extends BaseTestActivity {
    private static final String TAG = "RxBusTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getEditText().setText("Click button to post test events, " +
                "register and filter the event type, " +
                "then append the event content into edit text\n" +
                "--------------------\n");

        RxBus.getDefault().post(new TestEvent("test 0"));   // post before register

        RxBus.getDefault().registerOnActivity(TestEvent.class, this)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnActivity doOnSubscribe");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnActivity doOnUnsubscribe");
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnActivity doOnTerminate");
                    }
                })
                .subscribe(new Action1<TestEvent>() {
                    @Override
                    public void call(TestEvent testEvent) {
                        appendLine(String.format("Got event[%s] registerOnActivity", testEvent.getEvent()));
                    }
                });

        RxBus.getDefault().registerOnView(TestEvent.class, getButton())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnView doOnSubscribe");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnView doOnUnsubscribe");
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "registerOnView doOnTerminate");
                    }
                })
                .subscribe(new Action1<TestEvent>() {
                    @Override
                    public void call(TestEvent testEvent) {
                        appendLine(String.format("Got event[%s] registerOnView", testEvent.getEvent()));
                    }
                });
    }

    @Override
    public void onClick(View v) {
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
