package com.cpiz.android.playground.EventBusTest;

import com.cpiz.android.playground.BaseTestActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by caijw on 2015/10/16.
 *
 * @see <a href="https://github.com/greenrobot/EventBus/blob/master/HOWTO.md">EventBus How-To</a>
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

    /**
     * Subscriber will be called in the same thread, which is posting the event. This is the default. Event delivery
     * implies the least overhead because it avoids thread switching completely. Thus this is the recommended mode for
     * simple tasks that are known to complete is a very short time without requiring the main thread. Event handlers
     * using this mode must return quickly to avoid blocking the posting thread, which may be the main thread.
     */
    @Subscribe
    public void onTest(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        appendLine(String.format("tid: %d, on %s thread", tid, "event"));
        appendLine(String.format("tid: %d, on %s thread, done!", tid, "event"));
    }

    /**
     * Subscriber will be called in Android's main thread (sometimes referred to as UI thread). If the posting thread is
     * the main thread, event handler methods will be called directly. Event handlers using this mode must return
     * quickly to avoid blocking the main thread.
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTestMainThread(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        appendLine(String.format("tid: %d, on %s thread", tid, "main"));
        appendLine(String.format("tid: %d, on %s thread, done!", tid, "main"));
    }

    /**
     * Subscriber will be called in a background thread. If posting thread is not the main thread, event handler methods
     * will be called directly in the posting thread. If the posting thread is the main thread, EventBus uses a single
     * background thread, that will deliver all its events sequentially. Event handlers using this mode should try to
     * return quickly to avoid blocking the background thread.
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTestBackground(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        appendLine(String.format("tid: %d, on %s thread", tid, "background"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        appendLine(String.format("tid: %d, on %s thread, done!", tid, "background"));
    }

    /**
     * Event handler methods are called in a separate thread. This is always independent from the posting thread and the
     * main thread. Posting events never wait for event handler methods using this mode. Event handler methods should
     * use this mode if their execution might take some time, e.g. for network access. Avoid triggering a large number
     * of long running asynchronous handler methods at the same time to limit the number of concurrent threads. EventBus
     * uses a thread pool to efficiently reuse threads from completed asynchronous event handler notifications.
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTestAsync(final Integer msg) {
        final long tid = Thread.currentThread().getId();
        appendLine(String.format("tid: %d, on %s thread", tid, "new"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        appendLine(String.format("tid: %d, on %s thread, done!", tid, "new"));
    }
}
