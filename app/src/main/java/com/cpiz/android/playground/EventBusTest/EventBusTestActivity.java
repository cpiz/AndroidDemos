package com.cpiz.android.playground.EventBusTest;

import com.cpiz.android.playground.BaseTestActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by caijw on 2015/10/16.
 *
 * @see <a href="#https://github.com/greenrobot/EventBus/blob/master/HOWTO.md">EventBus How-To</a>
 */
public class EventBusTestActivity extends BaseTestActivity {
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onLeftClick() {
        EventBus.getDefault().post("Hello world!");
        EventBus.getDefault().post(1);
    }

    public void onEvent(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLine(String.format("tid: %d, on %s thread", tid, "event"));
            }
        });
    }

    public void onEventMainThread(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLine(String.format("tid: %d, on %s thread", tid, "main"));
            }
        });
    }

    public void onEventBackgroundThread(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLine(String.format("tid: %d, on %s thread", tid, "background"));
            }
        });
    }

    public void onEventAsync(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLine(String.format("tid: %d, on %s thread", tid, "new"));
            }
        });
    }
}
