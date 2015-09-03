package com.cpiz.android.playground;

import android.app.Application;

import com.cpiz.android.playground.JsonMessageTest.JsonMessageTestActivity;
import com.cpiz.android.playground.RxBusTest.RxBusTestActivity;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caijw on 2015/9/1.
 */
public class PlaygroundApp extends Application {
    private static PlaygroundApp instance;

    private List<TestAction> mTestActions = new ArrayList<>();

    public static PlaygroundApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        instance = this;

        initTestActions();
    }

    public List<TestAction> getTestActions() {
        return mTestActions;
    }

    /**
     * 在这定义要试验的功能，或启动新的 ACTIVITY
     */
    private void initTestActions() {
        mTestActions.add(new TestAction(RxBusTestActivity.class));
        mTestActions.add(new TestAction(JsonMessageTestActivity.class));
    }
}
