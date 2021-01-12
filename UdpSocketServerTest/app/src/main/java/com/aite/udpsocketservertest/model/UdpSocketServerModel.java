package com.aite.udpsocketservertest.model;

import android.content.Context;

import com.aite.commonlib.base.BaseModel;
import com.aite.udplib.data.PacketData;
import com.aite.udplib.rx.RxBus;
import com.aite.udplib.socket.ServerUdpSocket;
import com.aite.udpsocketservertest.presenter.UdpSocketServerPresenter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UdpSocketServerModel extends BaseModel<UdpSocketServerPresenter> {
    private Disposable mDisposable;

    private ServerUdpSocket socket;

    private Context mCotext;

    private boolean isConnecting = false;

    public UdpSocketServerModel(UdpSocketServerPresenter presenter) {
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
                    } else if (result.data.type == 2) { // 断开连接
                        isConnecting = false;
                        mPresenter.disconnect();
                    } //兜底
                    if (mDisposable != null && !mDisposable.isDisposed()) {
                        mDisposable.dispose();
                    }
                });
        addDisposable(disposable);
        socket = new ServerUdpSocket(mContext);
        socket.startUdpSocket();
    }

    public void sendMessage(String msg) {
        // todo
    }

    public void loadTimeOut() {
        mDisposable = Observable.timer(10, TimeUnit.MINUTES)
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
