package com.aite.udpsocketclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aite.udplib.api.UdpApi;
import com.aite.udplib.api.UdpScheduler;
import com.aite.udplib.socket.ClientUdpSocket;

public class UdpSocketClientAct extends AppCompatActivity {
    private final static String TAG = "UdpSocketClientAct";

    private ClientUdpSocket socket;

    private Button mBuildLinkBtn;

    private Button mSendMessageBtn;

    private EditText mInputMessageEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udp_socket_client_act);
        initView();
        initData();
        initListeners();
    }

    private void initView() {
        mBuildLinkBtn = (Button) findViewById(R.id.bt_connect);
        mInputMessageEt = (EditText) findViewById(R.id.et_message);
        mSendMessageBtn = (Button) findViewById(R.id.bt_send);
        mBuildLinkBtn.setEnabled(true);
        mSendMessageBtn.setEnabled(false);
        mInputMessageEt.setEnabled(false);
    }

    private void initData() {
    }

    private void initListeners() {
        mBuildLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket = new ClientUdpSocket(UdpSocketClientAct.this);
                socket.startUdpSocket();
            }
        });
        mSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.sendMessage(UdpScheduler.getPostMessage(UdpApi.Companion.sendJson(), mInputMessageEt.getText().toString(), 1));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.stopUdpSocket();
    }
}