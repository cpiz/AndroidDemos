package com.cpiz.android.playground.view;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.cpiz.android.playground.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    class TestAction {
        public TestAction(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }

        public TestAction(final Class<?> cls) {
            this.name = cls.getSimpleName();
            this.action = new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, cls));
                }
            };
        }

        String name;
        Runnable action;
    }

    private List<TestAction> mTestActions = new ArrayList<>();
    BaseAdapter adapter = new BaseAdapter() {
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
            btn.setText(getItem(position).name);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getItem(position).action.run();
                }
            });

            return btn;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initTestActions();
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * 在这定义要试验的功能，或启动新的 ACTIVITY
     */
    private void initTestActions() {
        mTestActions.add(new TestAction(MainActivity.class));
        mTestActions.add(new TestAction(RxBusTestActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
