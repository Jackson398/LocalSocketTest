package com.aite.udpsocketclienttest.model;

import android.content.Context;

import com.aite.commonlib.base.BaseModel;
import com.aite.udplib.data.PacketData;
import com.aite.udplib.rx.RxBus;
import com.aite.udplib.socket.ClientUdpSocket;
import com.aite.udpsocketclienttest.presenter.UdpSocketClientPresenter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UdpSocketClientModel extends BaseModel<UdpSocketClientPresenter> {
    private Disposable mDisposable;

    private ClientUdpSocket socket;

    private Context mCotext;

    private boolean isConnecting = false;

    public UdpSocketClientModel(UdpSocketClientPresenter presenter) {
        super(presenter);
    }

    @Override
    public void install(Context context) {
        super.install(context);
        mCotext = context;
    }

    public void startBuildLink() {
        Disposable disposable = RxBus.getInstance().toObservable(PacketData.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.data.type == 0 && !isConnecting) { // 心跳包
                        mPresenter.buildLinkSuccess();
                        isConnecting = true;
                    } else if (result.data.type == 1) { // 普通消息
                        mPresenter.recvMessage(result.data.content);
                    } else if (result.data.type == 2) {
                        isConnecting = false;
                        mPresenter.disconnect();
                    } //兜底
                    if (mDisposable != null && !mDisposable.isDisposed()) {
                        mDisposable.dispose();
                    }
                });
        addDisposable(disposable);
        socket = new ClientUdpSocket(mContext);
        socket.startUdpSocket();
    }

    public void sendMessage(String msg) {
        socket.sendMessage(msg);
    }

    public void loadTimeOut() {
        mDisposable = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    mPresenter.onTimeOut();
                });
    }

    @Override
    public void uninstall() {
        super.uninstall();
        isConnecting = false;
        socket.stopUdpSocket();
    }
}
