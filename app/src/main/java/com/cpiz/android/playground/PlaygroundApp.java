package com.cpiz.android.playground;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;

import net.danlew.android.joda.JodaTimeAndroid;


/**
 * Playground Application
 * <p/>
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
        instance = this;

        JodaTimeAndroid.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        Log.i(TAG, "onCreate");
    }
}
