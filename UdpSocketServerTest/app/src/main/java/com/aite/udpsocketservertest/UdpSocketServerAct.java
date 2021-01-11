package com.aite.udpsocketservertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aite.udplib.socket.ServerUdpSocket;

public class UdpSocketServerAct extends AppCompatActivity {
    private static final String TAG = "UdpSocketServerAct";

    private Button mAcceptLinkBtn;

    private ServerUdpSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udp_socket_server_act);
        initView();
        initData();
        initListeners();
    }

    private void initView() {
        mAcceptLinkBtn = (Button) findViewById(R.id.bt_wait_link);
    }

    private void initData() {
    }

    private void initListeners() {
        mAcceptLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket = new ServerUdpSocket(UdpSocketServerAct.this);
                socket.startUdpSocket();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}