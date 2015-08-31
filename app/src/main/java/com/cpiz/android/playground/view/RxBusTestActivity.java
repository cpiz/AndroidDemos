package com.cpiz.android.playground.view;

import android.os.Bundle;
import android.util.Log;

import com.cpiz.android.utils.RxBus;

import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by caijw on 2015/8/31.
 */
public class RxBusTestActivity extends SimpleTestActivity {
    private static final String TAG = "RxBusTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getEditText().setText("Click button to emit test events, " +
                "register and filter the event type, " +
                "then append the event content into edit text\n" +
                "--------------------\n");

        RxBus.getDefault().post(new TestEvent("test 0"));   // post before register

        ViewObservable.clicks(getButton())
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        RxBus.getDefault().post(new TestEvent("test 1"));
                        RxBus.getDefault().post(null);
                        RxBus.getDefault().post(new TestEvent("test 2"));
                    }
                });

        RxBus.getDefault().registerOnView(TestEvent.class, getButton())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "doOnSubscribe");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "doOnUnsubscribe");
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "doOnTerminate");
                    }
                })
                .subscribe(new Action1<TestEvent>() {
                    @Override
                    public void call(TestEvent testEvent) {
                        getEditText().append(String.format("Got event[%s]\n", ((TestEvent) testEvent).getEvent()));
                    }
                });
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