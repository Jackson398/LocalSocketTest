package com.aite.udpsocketservertest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aite.commonlib.base.BaseAct;
import com.aite.commonlib.contract.UdpSocketContract;
import com.aite.udplib.api.UdpApi;
import com.aite.udplib.api.UdpScheduler;
import com.aite.udplib.socket.ServerUdpSocket;
import com.aite.udpsocketservertest.databinding.UdpSocketServerActBinding;
import com.aite.udpsocketservertest.presenter.UdpSocketServerPresenter;

public class UdpSocketServerAct extends BaseAct implements UdpSocketContract.View  {
    private static final String TAG = "UdpSocketServerAct";

    private UdpSocketServerActBinding mBinding;

    private UdpSocketServerPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btWaitLink.setOnClickListener(v -> {
            showBaseProgressDlg("正在等待设备udp连接");
            mPresenter.startBuildLink();
        });
        mBinding.btSend.setOnClickListener(v -> {
            mPresenter.sendMessage(UdpScheduler.getPostMessage(UdpApi.Companion.sendJson(), mBinding.etMessage.getText().toString(), 1));
            mBinding.etMessage.setText("");
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.udp_socket_server_act;
    }

    @Override
    public void setPanelMsg(String appendMsg) {
        mBinding.recvDataId.append(appendMsg + "\n");
    }

    @Override
    public void setBuildLinkBtnState(boolean enable) {
        if (enable) {
            mBinding.btWaitLink.setAlpha(1.0f);
            mBinding.btWaitLink.setEnabled(true);
        } else {
            mBinding.btWaitLink.setAlpha(0.7f);
            mBinding.btWaitLink.setEnabled(false);
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
    public void setPresenter(UdpSocketContract.Presenter presenter) {
        mPresenter = new UdpSocketServerPresenter(this);
    }

    @Override
    protected void initBinding(ViewDataBinding binding) {
        mBinding = (UdpSocketServerActBinding) binding;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new UdpSocketServerPresenter(this);
        mPresenter.install(this);
    }

    @Override
    public void closeBaseProgressDlg() {
        super.closeBaseProgressDlg();
    }

    @Override
    public void showBaseProgressDlg(String msg) {
        super.showBaseProgressDlg(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}