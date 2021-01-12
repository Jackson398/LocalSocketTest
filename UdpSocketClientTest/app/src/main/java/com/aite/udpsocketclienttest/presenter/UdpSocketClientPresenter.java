package com.aite.udpsocketclienttest.presenter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aite.commonlib.contract.UdpSocketContract;
import com.aite.udpsocketclienttest.model.UdpSocketClientModel;

public class UdpSocketClientPresenter implements UdpSocketContract.Presenter {
    private UdpSocketContract.View mView;
    private UdpSocketClientModel mModel;

    public UdpSocketClientPresenter(UdpSocketContract.View view) {
        mView = view;
        mModel = new UdpSocketClientModel(this);
    }

    @Override
    public void install(@NonNull Context context) {
        mModel.install(context);
    }

    @Override
    public void startBuildLink() {
        mView.setBuildLinkBtnState(false);
        mModel.startBuildLink();
        mModel.loadTimeOut();
    }

    @Override
    public void buildLinkSuccess() {
        mView.closeProgressDlg();
        mView.setPanelMsg("连接中...");
    }

    @Override
    public void sendMessage(String msg) {
        mModel.sendMessage(msg);
    }

    @Override
    public void recvMessage(String msg) {
       mView.setPanelMsg(msg);
    }

    @Override
    public void disconnect() {
        mView.setBuildLinkBtnState(true);
        mView.setPanelMsg("连接断开");
    }

    @Override
    public void uninstall() {
        mModel.uninstall();
    }

    public void onTimeOut() {
        mView.onTimeOut();
    }
}
