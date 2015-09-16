package com.cpiz.android.playground;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caijw on 2015/9/15.
 */
public abstract class BaseListActivity extends ListActivity {
    private static final String TAG = "BaseListActivity";

    private List<TestAction> mActions = new ArrayList<>();

    abstract public List<TestAction> getActions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        mActions = getActions();
        if (mActions == null) {
            Log.w(TAG, "Null actions");
            return;
        }

        setListAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mActions.size();
            }

            @Override
            public TestAction getItem(int position) {
                return mActions.get(position);
            }

            @Override
            public long getItemId(int position) {
                return mActions.get(position).hashCode();
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = new Button(BaseListActivity.this);
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
