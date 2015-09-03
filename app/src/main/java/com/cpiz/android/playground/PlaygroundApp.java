package com.cpiz.android.playground;

import android.app.Application;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Playground Application
 *
 * Created by caijw on 2015/9/1.
 */
public class PlaygroundApp extends Application {
    private static final String TAG = "PlaygroundApp";

    private static PlaygroundApp instance;

    public static PlaygroundApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        instance = this;

        Log.i(TAG, "onCreate");
    }
}
