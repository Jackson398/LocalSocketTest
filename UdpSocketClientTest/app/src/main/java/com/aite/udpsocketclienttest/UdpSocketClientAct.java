package com.aite.udpsocketclienttest;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import android.os.Bundle;

import com.aite.commonlib.base.BaseAct;
import com.aite.commonlib.contract.UdpSocketContract;
import com.aite.udplib.api.UdpApi;
import com.aite.udplib.api.UdpScheduler;
import com.aite.udpsocketclienttest.databinding.UdpSocketClientActBinding;
import com.aite.udpsocketclienttest.presenter.UdpSocketClientPresenter;

public class UdpSocketClientAct extends BaseAct implements UdpSocketContract.View {
    private final static String TAG = "UdpSocketClientAct";

    private UdpSocketClientActBinding mBinding;

    private UdpSocketClientPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btBuildConnect.setOnClickListener(v -> {
            showBaseProgressDlg("正在udp连接");
            mPresenter.startBuildLink();
        });
        mBinding.btSend.setOnClickListener(v -> {
            mPresenter.sendMessage(UdpScheduler.getPostMessage(UdpApi.Companion.sendJson(), mBinding.etMessage.getText().toString(), 1));
            mBinding.etMessage.setText("");
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.udp_socket_client_act;
    }

    @Override
    protected void initBinding(ViewDataBinding binding) {
        mBinding = (UdpSocketClientActBinding) binding;
    }

    @Override
    public void setPresenter(UdpSocketContract.Presenter presenter) {
        mPresenter = new UdpSocketClientPresenter(this);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new UdpSocketClientPresenter(this);
        mPresenter.install(this);
    }

    @Override
    public void setPanelMsg(String appendMsg) {
        mBinding.recvDataId.append(appendMsg + "\n");
    }

    @Override
    public void showBaseProgressDlg(String msg) {
        super.showBaseProgressDlg(msg);
    }

    @Override
    public void setBuildLinkBtnState(boolean enable) {
        if (enable) {
            mBinding.btBuildConnect.setAlpha(1.0f);
            mBinding.btBuildConnect.setEnabled(true);
        } else {
            mBinding.btBuildConnect.setAlpha(0.7f);
            mBinding.btBuildConnect.setEnabled(false);
        }
    }

    @Override
    public void closeProgressDlg() {
        closeBaseProgressDlg();
    }

    @Override
    public void onTimeOut() {
        setBuildLinkBtnState(true);
        closeProgressDlg();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}