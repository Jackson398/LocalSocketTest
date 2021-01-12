package com.aite.udplib.rx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 只会把在订阅发生的时间点之后来自原始Observable的数据发射给观察者
 *
 * @author renjianan
 */
public class RxBus {
    private volatile static RxBus mDefaultInstance;
    private final Subject<Object> mBus;

    private final Map<Class<?>, Object> mStickyEventMap;

    private RxBus() {
        mBus = PublishSubject.create().toSerialized();
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    public static RxBus getInstance() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    /**
     * 发送事件
     */
    public void post(Object event) {
        mBus.onNext(event);
    }

    /**
     * 使用Rxlifecycle解决RxJava引起的内存泄漏
     */
    public <T> Observable<T> toObservable(final Class<T> eventType) {
        return mBus.ofType(eventType);
    }
//
//    /**
//     * 使用Rxlifecycle解决RxJava引起的内存泄漏
//     */
//    public <T> Observable<T> toObservable(LifecycleOwner owner, final Class<T> eventType) {
//        LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(owner);
//        return mBus.ofType(eventType).compose(provider.<T>bindToLifecycle());
//    }


    /**
     * 判断是否有订阅者
     */
    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    public void reset() {
        mDefaultInstance = null;
    }


    //////////////////Stciky相关//////////////////////

    /**
     * 发送一个新Sticky事件
     */
    public void postSticky(Object event) {
        synchronized (mStickyEventMap) {
            mStickyEventMap.put(event.getClass(), event);
        }
        post(event);
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     * 使用Rxlifecycle解决RxJava引起的内存泄漏
     */
//    public <T> Observable<T> toObservableSticky(LifecycleOwner owner, final Class<T> eventType) {
//        synchronized (mStickyEventMap) {
//            LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(owner);
//            Observable<T> observable = mBus.ofType(eventType).compose(provider.<T>bindToLifecycle());
//            final Object event = mStickyEventMap.get(eventType);
//
//            if (event != null) {
//                return observable.mergeWith(Observable.create(new ObservableOnSubscribe<T>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<T> subscriber) throws Exception {
//                        subscriber.onNext(eventType.cast(event));
//                    }
//                }));
//            } else {
//                return observable;
//            }
//        }
//    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     * 使用Rxlifecycle解决RxJava引起的内存泄漏
     */
    public <T> Observable<T> toObservableSticky(final Class<T> eventType) {
        synchronized (mStickyEventMap) {
            Observable<T> observable = mBus.ofType(eventType);
            final Object event = mStickyEventMap.get(eventType);
            if (event != null) {
                return observable.mergeWith(Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> subscriber) throws Exception {
                        subscriber.onNext(eventType.cast(event));
                    }
                }));
            } else {
                return observable;
            }
        }
    }

    /**
     * 根据eventType获取Sticky事件
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            return eventType.cast(mStickyEventMap.get(eventType));
        }
    }

    /**
     * 移除指定eventType的Sticky事件
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (mStickyEventMap) {
            return eventType.cast(mStickyEventMap.remove(eventType));
        }
    }

    /**
     * 移除所有的Sticky事件
     */
    public void removeAllStickyEvents() {
        synchronized (mStickyEventMap) {
            mStickyEventMap.clear();
        }
    }
}
