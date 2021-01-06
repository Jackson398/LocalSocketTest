package com.aite.localsocketservertest.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aite.localsocketservertest.R;
import com.aite.mylib.manager.LocalServerSocketManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalSocketServerAct extends Activity {
    private static final String TAG = "LocalSocketServerAct";
    private Button btnAcept = null;
    private ListView mListView;
    private boolean isRunning;
    private List<Map<String, Object>> mData = new ArrayList<>();
    private SimpleAdapter mAdapter;
    private Handler mHandler = null;
    private LocalServerSocket serverSocket;
    private AcceptThread mAcceptThread;
    private LocalServerSocketManager.LocalSocketManager localSocketManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        btnAcept = (Button) findViewById(R.id.btn_accept);
        mListView = (ListView) findViewById(R.id.lv_connected_client);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mData.get(position);
                Intent intent = new Intent();
                intent.setClass(LocalSocketServerAct.this, LocalSocketServerItemAct.class);
                intent.putExtra("name", (String) map.get("content"));
                startActivity(intent);
            }
        });
    }

    private void initData() {
        mHandler = new ServerHandler(this, getMainLooper());
        mAdapter = new SimpleAdapter(this, mData, R.layout.list_item, new String[]{"image", "content"}, new int[]{R.id.iv_device_connected, R.id.tv_device_connected_name});
        mListView.setAdapter(mAdapter);
    }

    private void initListener() {
        btnAcept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始监听线程，监听客户端连接
                mAcceptThread = new AcceptThread();
                isRunning = true;
                mAcceptThread.start();
                btnAcept.setEnabled(false);
            }
        });
    }

    //定义监听客户端连接的线程
    private class AcceptThread extends Thread {
        @Override
        public void run() {
            try {
                isRunning = true;
                serverSocket = new LocalServerSocket(LocalServerSocketManager.SERVICE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
                isRunning = false;
            }

            while (isRunning) {
                Log.d(TAG, "wait for new client coming !");
                try {
                    LocalSocket interactClientSocket = serverSocket.accept();
                    Log.d(TAG, "accept one socket");
                    localSocketManager = LocalServerSocketManager.getInstance().addLocalSocket(interactClientSocket);
                    localSocketManager.setDataUpdateListener(new LocalServerSocketManager.DataUpdateListener() {
                        @Override
                        public void receive(String msg) {
                        }

                        @Override
                        public void login(String name) {
                            Log.d(TAG, "login name=" + name);
                            Message msg = Message.obtain();
                            msg.what = ServerHandler.CONNECT_SUCCESS;
                            msg.obj = name;//获取客户端IP地址
                            mHandler.sendMessage(msg);//返回连接成功的信息
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServerHandler extends Handler {
        static final int CONNECT_SUCCESS = 1;
        static final int DISCONNECT = 2;
        final WeakReference<Context> mContext;

        ServerHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    Map<String, Object> map = new HashMap<>();
                    map.put("image", R.mipmap.ic_launcher);
                    map.put("content", msg.obj);
                    mData.add(map);
                    mAdapter.notifyDataSetChanged();
                    displayToast("连接成功");
                    break;
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
