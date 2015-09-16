package com.cpiz.android.playground;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseListActivity {
    private static final String TAG = "MainActivity";

    @Override
    public List<TestAction> getActions() {
        /**
         * 通过 AndroidManifest.xml 配置，自动为每一个Activity增加入口
         */
        List<TestAction> actions = new ArrayList<>();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = packageInfo.activities;
            for (ActivityInfo activity : activities) {
                try {
                    Log.i(TAG, String.format("add activity: %s", activity.name));
                    actions.add(new TestAction(Class.forName(activity.name)));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return actions;
    }
}
