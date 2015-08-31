package com.cpiz.android.utils;

import android.view.View;

import rx.Observable;
import rx.android.view.ViewObservable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * 基于Rx的事件总线
 * 内置了一个默认总线便于全局使用，也可以通过create创建新的，在指定访问内使用
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
     * 获得默认事件总线
     *
     * @return
     */
    public static RxBus getDefault() {
        return mDefault;
    }

    /**
     * 创建一个新的事件总线
     *
     * @return
     */
    public static RxBus create() {
        return new RxBus();
    }

    /**
     * 向事件总线塞入一个事件对象
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
     * 不同于 register，该函数可以不用调用者管理 Subscription 的退订，在view生命detached时将自动销毁。
     * 注意：该函数必须在UI现场调用
     *
     * @param cls  要过滤的事件载体类型
     * @param view 绑定订阅的生命周期到一个activity上
     * @return 事件源Observable
     */
    public <T> Observable<T> registerOnView(final Class<T> cls, final View view) {
        return ViewObservable.bindView(view, register(cls));
    }
}
