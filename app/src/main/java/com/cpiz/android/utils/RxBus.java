package com.cpiz.android.utils;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import com.trello.rxlifecycle2.components.RxActivity;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;


/**
 * 基于Rx的事件总线
 * 内置了一个默认实例便于全局使用，也可通过create创建新实例在自定义范围内使用
 * <p/>
 * Created by caijw on 2015/8/31.
 */
@SuppressWarnings("unused")
public class RxBus {
    private static final String TAG = "RxBus";
    private final static RxBus mDefault = new RxBus(0, "Default");
    private final Relay<Object> mRelay;

    private final int mMaxBufferSize;
    private final String mName;

    public String getName() {
        return mName;
    }

    private RxBus(final int maxBufferSize, @NonNull final String name) {
        mMaxBufferSize = maxBufferSize;
        mName = name;
        mRelay = PublishRelay.create().toSerialized();
    }

    /**
     * 获得默认总线实例
     *
     * @return 默认事件总线实例
     */
    public static RxBus getDefault() {
        return mDefault;
    }

    /**
     * 创建一个新总线实例
     *
     * @return 新事件总线实例
     */
    public static RxBus create(final int maxBufferSize,
                               @NonNull final String name) {
        return new RxBus(maxBufferSize, name);
    }

    /**
     * 向总线填入一个事件对象
     *
     * @param event 事件对象
     */
    public void post(Object event) {
        if (event == null) {
            Log.e(TAG, "event is null, can not be post!");
            return;
        }
        mRelay.accept(event);
    }

    /**
     * 向总线填入一个事件对象，延迟发送
     *
     * @param event     事件对象
     * @param milliSecs 延迟毫秒时间
     */
    public void postDelay(final Object event, long milliSecs) {
        Observable.timer(milliSecs, TimeUnit.MILLISECONDS)
                .subscribe(
                        aLong -> mRelay.accept(event),
                        throwable -> Log.e(TAG, "Post Delay failed.", throwable));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 注意：订阅该事件源后必须自行调用 unsubscribe 进行释放，避免内存泄露
     *
     * @param cls 要过滤的事件对象类型
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls) {
        if (mMaxBufferSize > 0) {
            return mRelay.toFlowable(BackpressureStrategy.BUFFER)
                    .filter(cls::isInstance)
                    .onBackpressureBuffer(mMaxBufferSize)
                    .cast(cls).toObservable();
        }
        return mRelay.filter(cls::isInstance).cast(cls);
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls               要过滤的事件载体类型
     * @param lifecycleActivity 绑定订阅的生命周期到一个 activity 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls, final RxFragmentActivity lifecycleActivity) {
        if (lifecycleActivity == null) {
            throw new InvalidParameterException("lifecycleActivity can not be null");
        }

        return register(cls).compose(RxLifecycle.bindUntilEvent(
                lifecycleActivity.lifecycle(),
                ActivityEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls               要过滤的事件载体类型
     * @param lifecycleActivity 绑定订阅的生命周期到一个 activity 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls, final RxActivity lifecycleActivity) {
        if (lifecycleActivity == null) {
            throw new InvalidParameterException("lifecycleActivity can not be null");
        }

        return register(cls).compose(
                RxLifecycle.bindUntilEvent(
                        lifecycleActivity.lifecycle(),
                        ActivityEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls               要过滤的事件载体类型
     * @param lifecycleFragment 绑定订阅的生命周期到一个 fragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls,
                                      final com.trello.rxlifecycle2.components.support.RxFragment lifecycleFragment) {
        if (lifecycleFragment == null) {
            throw new InvalidParameterException("lifecycleFragment can not be null");
        }

        return register(cls).compose(
                RxLifecycle.bindUntilEvent(lifecycleFragment.lifecycle(),
                        FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls               要过滤的事件载体类型
     * @param lifecycleFragment 绑定订阅的生命周期到一个 fragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls,
                                      final com.trello.rxlifecycle2.components.RxFragment lifecycleFragment) {
        if (lifecycleFragment == null) {
            throw new InvalidParameterException("lifecycleFragment can not be null");
        }

        return register(cls).compose(RxLifecycle.bindUntilEvent(lifecycleFragment.lifecycle(), FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls               要过滤的事件载体类型
     * @param lifecycleFragment 绑定订阅的生命周期到一个 DialogFragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls,
                                      final com.trello.rxlifecycle2.components.RxDialogFragment lifecycleFragment) {
        if (lifecycleFragment == null) {
            throw new InvalidParameterException("lifecycleFragment can not be null");
        }

        return register(cls).compose(RxLifecycle.bindUntilEvent(lifecycleFragment.lifecycle(), FragmentEvent.DESTROY));
    }

    public <T> Observable<T> register(final Class<T> cls,
                                      final com.trello.rxlifecycle2.components.support.RxDialogFragment lifecycleFragment) {
        if (lifecycleFragment == null) {
            throw new InvalidParameterException("lifecycleFragment can not be null");
        }

        return register(cls).compose(RxLifecycle.bindUntilEvent(lifecycleFragment.lifecycle(), FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 不同于 register，该函数可以不用调用者管理 Subscription 的退订，在view detached时将自动销毁。
     * 注意：该函数必须在UI现场调用
     *
     * @param cls           要过滤的事件载体类型
     * @param lifecycleView 绑定订阅的生命周期到一个 view 上
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls, final View lifecycleView) {
        if (lifecycleView == null) {
            throw new InvalidParameterException("view can not be null");
        }

        return register(cls).compose(RxLifecycleAndroid.bindView(lifecycleView));
    }
}
