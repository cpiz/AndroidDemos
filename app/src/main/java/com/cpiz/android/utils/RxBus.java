package com.cpiz.android.utils;

import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.components.RxActivity;
import com.trello.rxlifecycle.components.RxDialogFragment;
import com.trello.rxlifecycle.components.RxFragment;

import java.security.InvalidParameterException;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * 基于Rx的事件总线
 * 内置了一个默认实例便于全局使用，也可通过create创建新实例在自定义范围内使用
 * <p/>
 * Created by caijw on 2015/8/31.
 */
public class RxBus {
    private final SerializedSubject<Object, Object> mSubject;

    private final static RxBus mDefault = new RxBus();

    private RxBus() {
        mSubject = new SerializedSubject<>(PublishSubject.create());
    }

    /**
     * 获得默认总线实例
     *
     * @return
     */
    public static RxBus getDefault() {
        return mDefault;
    }

    /**
     * 创建一个新总线实例
     *
     * @return
     */
    public static RxBus create() {
        return new RxBus();
    }

    /**
     * 向总线填入一个事件对象
     *
     * @param event
     */
    public void post(Object event) {
        mSubject.onNext(event);
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 注意：订阅该事件源后必须自行调用 unsubscribe 进行释放，避免内存泄露
     *
     * @param cls 要过滤的事件对象类型
     * @return 事件源Observable
     */
    public <T> Observable<T> register(final Class<T> cls) {
        return mSubject.filter(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return cls.isInstance(o);
            }
        }).cast(cls);
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 activity onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param activity 绑定订阅的生命周期到一个 activity 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnActivity(final Class<T> cls, final RxActivity activity) {
        if (activity == null) {
            throw new InvalidParameterException("activity can not be null");
        }

        return register(cls).compose(RxLifecycle.<T>bindUntilActivityEvent(activity.lifecycle(), ActivityEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls      要过滤的事件载体类型
     * @param fragment 绑定订阅的生命周期到一个 fragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnFragment(final Class<T> cls, final RxFragment fragment) {
        if (fragment == null) {
            throw new InvalidParameterException("fragment can not be null");
        }

        return register(cls).compose(RxLifecycle.<T>bindUntilFragmentEvent(fragment.lifecycle(), FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 使用该函数可以不用调用者管理 Subscription 的退订，在 fragment onDestroy 时将自动销毁。
     *
     * @param cls         要过滤的事件载体类型
     * @param dlgFragment 绑定订阅的生命周期到一个 DialogFragment 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnDialogFragment(final Class<T> cls, final RxDialogFragment dlgFragment) {
        if (dlgFragment == null) {
            throw new InvalidParameterException("dlgFragment can not be null");
        }

        return register(cls).compose(RxLifecycle.<T>bindUntilFragmentEvent(dlgFragment.lifecycle(), FragmentEvent.DESTROY));
    }

    /**
     * 过滤事件类型，获得一个可订阅的事件源
     * 不同于 register，该函数可以不用调用者管理 Subscription 的退订，在view detached时将自动销毁。
     * 注意：该函数必须在UI现场调用
     *
     * @param cls  要过滤的事件载体类型
     * @param view 绑定订阅的生命周期到一个 view 上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnView(final Class<T> cls, final View view) {
        if (view == null) {
            throw new InvalidParameterException("view can not be null");
        }

        return register(cls).takeUntil(RxView.detaches(view));
    }
}
