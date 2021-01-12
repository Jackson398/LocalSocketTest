package com.aite.commonlib.base;

import android.content.Context;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseModel<T> {
    private CompositeDisposable mCompositeDisposable;

    protected Context mContext;

    protected T mPresenter;

    public BaseModel(T presenter) {
        mPresenter = presenter;
    }

    public void install(Context context) {
        mContext = context;
        mCompositeDisposable = new CompositeDisposable();
    }

    public boolean addDisposable(Disposable disposable) {
        if (disposable == null) {
            return false;
        }
        return mCompositeDisposable.add(disposable);
    }

    public void uninstall() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }
}
