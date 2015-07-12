package com.cpiz.android.demos.view;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.cpiz.androidplayground.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    class DemoAction {
        public DemoAction(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }

        String name;
        Runnable action;
    }

    private List<DemoAction> demoActions = new ArrayList<>();
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return demoActions.size();
        }

        @Override
        public DemoAction getItem(int position) {
            return demoActions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return demoActions.get(position).hashCode();
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
        initDemoActivities();
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    /**
     * 在这定义要试验的功能，或启动新的SUB DEMO ACTIVITY
     */
    private void initDemoActivities() {
        demoActions.add(new DemoAction("MainActivity", new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        }));
    }
}
