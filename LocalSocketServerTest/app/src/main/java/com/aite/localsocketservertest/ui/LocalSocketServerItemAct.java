package com.aite.localsocketservertest.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aite.localsocketservertest.R;
import com.aite.mylib.manager.LocalServerSocketManager;
import com.aite.mylib.model.LocalSocketConst;
import com.aite.mylib.model.PacketData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LocalSocketServerItemAct extends Activity {
    private static final String TAG = "LocalSocketServerItemAct";
    private TextView tvState = null;
    private TextView tvIp = null;
    private EditText etSend = null;
    private Button btnSend = null;

    private Handler mHandler = null;
    private LocalServerSocketManager.LocalSocketManager localSocketManager;
    private LocalServerSocketManager.DataUpdateListener mListener;

    class ServerHandler extends Handler {

        static final int CONNECT_SUCCESS = 1;
        static final int DISCONNECT = 2;
        static final int RECEIVE_DATA = 3;
        static final int SEND_SATA = 4;

        final WeakReference<Context> mContext;

        ServerHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVE_DATA:
                    String str = (String) msg.obj;
                    tvState.setText(str);
                    break;

                case CONNECT_SUCCESS:
                    tvIp.setText("客户端" + msg.obj + "已连接");
                    displayToast("连接成功");
                    break;

                case DISCONNECT:
                    displayToast("连接已断开");
                    //清空TextView
                    tvState.setText(null);
                    tvIp.setText(null);
                    btnSend.setEnabled(false);
                    break;

                case SEND_SATA:
                    etSend.setText("");
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        initView();
        initData();
        initListener();
    }

    private void initView() {

        tvIp = (TextView) findViewById(R.id.tv_ip);
        etSend = (EditText) findViewById(R.id.et_send);
        tvState = (TextView) findViewById(R.id.tv_state);
        btnSend = (Button) findViewById(R.id.btn_send);
        findViewById(R.id.btn_accept).setVisibility(View.GONE);

        btnSend.setEnabled(true);
    }


    private void initData() {
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        localSocketManager = LocalServerSocketManager.getInstance().getLocalSocketManager(name);
        mHandler = new ServerHandler(this, getMainLooper());
    }

    private void initListener() {
        //发送数据按钮
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送请求数据
                localSocketManager.sendPacket(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_JSON).setContent((etSend.getText().toString())));
                mHandler.sendEmptyMessage(ServerHandler.SEND_SATA);
            }
        });
        final LocalServerSocketManager.DataUpdateListener oldListener = localSocketManager.getDataUpdateListener();
        if (oldListener == null) {
            mListener = new LocalServerSocketManager.DataUpdateListener() {
                @Override
                public void login(String name) {
                }

                @Override
                public void receive(String response) {
                    //用Handler把读取到的信息发到主线程
                    Message msg = Message.obtain();
                    msg.what = ServerHandler.RECEIVE_DATA;
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                }
            };
        } else {
            mListener = new LocalServerSocketManager.DataUpdateListener() {
                @Override
                public void login(String name) {
                    oldListener.login(name);
                }

                @Override
                public void receive(String response) {
                    //用Handler把读取到的信息发到主线程
                    Message msg = Message.obtain();
                    msg.what = ServerHandler.RECEIVE_DATA;
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                }
            };
        }
        localSocketManager.setDataUpdateListener(mListener);
    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);//清空消息队列，防止Handler强引用导致内存泄漏
    }
}
