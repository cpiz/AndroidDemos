package com.cpiz.android.playground;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by caijw on 2015/9/1.
 */
public class PlaygroundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
