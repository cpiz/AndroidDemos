package com.cpiz.android.playground;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private static final String TAG = "MainActivity";

    private List<TestAction> mTestActions = new ArrayList<>();

    private void loadAppActivities() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = packageInfo.activities;
            for (ActivityInfo activity : activities) {
                try {
                    Log.i(TAG, String.format("add activity: %s", activity.name));
                    mTestActions.add(new TestAction(Class.forName(activity.name)));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        loadAppActivities();

        setListAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mTestActions.size();
            }

            @Override
            public TestAction getItem(int position) {
                return mTestActions.get(position);
            }

            @Override
            public long getItemId(int position) {
                return mTestActions.get(position).hashCode();
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = new Button(MainActivity.this);
                }
                Button btn = (Button) convertView;
                btn.setText(getItem(position).getName());
                btn.setAllCaps(false);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getItem(position).getAction().run();
                    }
                });

                return btn;
            }
        });
    }
}
