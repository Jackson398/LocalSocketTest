package com.aite.commonlib.base;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

public abstract class BaseAct extends FragmentActivity {
    private ViewDataBinding mBind;

    private ProgressDialog mLoadProgressDlg;

    protected abstract int getLayoutId();

    protected abstract void initBinding(ViewDataBinding binding);

    protected abstract void initPresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = DataBindingUtil.setContentView(this, getLayoutId());
        initBinding(mBind);
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBaseProgressDlg();
    }

    public void closeBaseProgressDlg() {
        if (mLoadProgressDlg != null) {
            mLoadProgressDlg.dismiss();
        }
    }

    public void showBaseProgressDlg(String msg) {
        if (mLoadProgressDlg == null) {
            mLoadProgressDlg = new ProgressDialog(this);
            mLoadProgressDlg.setCancelable(false);
        } else if (mLoadProgressDlg.isShowing()) {
            return;
        }
        mLoadProgressDlg.setMessage(msg);
        mLoadProgressDlg.show();
    }
}
