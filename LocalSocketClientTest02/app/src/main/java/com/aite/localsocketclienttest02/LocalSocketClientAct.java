package com.aite.localsocketclienttest02;

import android.app.Activity;
import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aite.mylib.manager.LocalServerSocketManager;
import com.aite.mylib.model.LocalSocketConst;
import com.aite.mylib.model.PacketData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端代码
 */
public class LocalSocketClientAct extends Activity implements View.OnClickListener {
    private static final String TAG = "LocalSocketClientAct";
    private TextView tv;
    private EditText etSend;
    private EditText etIP;
    private Button btnSend;
    private Button btnStart;
    private Button btnStop;

    private Handler mHandler;
    private LocalSocket localSocket;
    private String str = "";
    boolean isRunning = false;
    private OutputStream os;
    private InputStream is;
    public String socketId;
    private StartThread st;
    private ReceiveThread rt;
    private Thread receiveThread;
    private Thread sendThread;

    private List<PacketData> sendData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client);

        initView();
        initData();
        initListener();
    }

    private void initView() {

        tv = (TextView) findViewById(R.id.tv);
        etSend = (EditText) findViewById(R.id.et_send);
        etIP = (EditText) findViewById(R.id.et_ip);

        btnSend = (Button) findViewById(R.id.btn_send);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
    }

    private void initData() {
        localSocket = new LocalSocket();
        sendData = new ArrayList<>();
        socketId = getSocketId();
        setButtonOnStartState(true);  //设置按键状态为可开始连接
        mHandler = new ClientHandler(this, getMainLooper());  //实例化Handler，用于进程间的通信
    }

    private void initListener() {

        btnSend.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setButtonOnStartState(boolean flag) {//设置按钮的状态

        btnSend.setEnabled(!flag);
        btnStop.setEnabled(!flag);
        btnStart.setEnabled(flag);
        etIP.setEnabled(flag);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                //按下开始连接按键即开始StartThread线程
                st = new StartThread();
                st.start();
                setButtonOnStartState(false);  //设置按键状态为不可开始连接
                break;
            case R.id.btn_send:
                // 发送请求数据
                sendData.add(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_JSON).setContent((etSend.getText().toString())));
                mHandler.sendEmptyMessage(ClientHandler.SEND_SATA);
                break;
            case R.id.btn_stop:
                isRunning = false;
                setButtonOnStartState(true);//设置按键状态为不可开始连接
                try {
                    localSocket.close();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    displayToast("未连接成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private class StartThread extends Thread {
        @Override
        public void run() {
            try {
                localSocket = new LocalSocket();
                LocalSocketAddress localSocketAddress = new LocalSocketAddress(LocalServerSocketManager.SERVICE_NAME);
                localSocket.connect(localSocketAddress);  // 连接服务socket
                isRunning = true;
                is = localSocket.getInputStream();
                os = localSocket.getOutputStream();
                sendData.add(new PacketData().setType(LocalSocketConst.TYPE_LOGIN).setContent(socketId));
                //启动接收数据的线程
                createReceiveThread();
                createSendThread();
                if (localSocket.isConnected()) { //成功连接获取socket对象则发送成功消息
                    mHandler.sendEmptyMessage(ClientHandler.CONNECT_SUCCESS);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        isRunning = false; //防止服务器端关闭导致客户端读到空指针而导致程序崩溃
        //发送信息通知用户客户端已关闭
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (localSocket != null) {
            try {
                localSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        is = null;
        os = null;
        localSocket = null;
    }

    private void createReceiveThread() {
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        PacketData receive = PacketData.readPacketData(is);
                        if (receive.type == LocalSocketConst.TYPE_CLOSE) {
                            Log.d(TAG, "receive type close");
                            close();
                        } else {
                            str = receive.content;
                            //用Handler把读取到的信息发到主线程
                            Message msg = Message.obtain();
                            msg.what = ClientHandler.RECEIVE_DATA;
                            msg.obj = str;
                            mHandler.sendMessage(msg);

                            try {
                                sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        close();
                        mHandler.sendEmptyMessage(ClientHandler.DISCONNECT);
                        e.printStackTrace();
                    }
                }
                //发送信息通知用户客户端已关闭
                mHandler.sendEmptyMessage(ClientHandler.DISCONNECT);
            }
        };
        receiveThread.start();
    }

    private void createSendThread() {
        sendThread = new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        if (sendData.size() > 0) {
                            PacketData data = sendData.remove(0);
                            Log.d(TAG,  getSocketId() + data.toString());
                            byte[] datas = new byte[1];
                            datas[0] = data.type;
                            os.write(datas);
                            if (data.type != LocalSocketConst.TYPE_CLOSE) {
                                os.write(data.getContent());
                            }
                            os.flush();
                        }
                        if(sendData.size()==0){
                            try {
                                sleep(50000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        sendThread.start();
    }

    private class ReceiveThread extends Thread {
        private InputStream is;

        ReceiveThread(LocalSocket socket) throws IOException {
            is = socket.getInputStream();
        }

        @Override
        public void run() {
            super.run();
        }
    }

    class ClientHandler extends Handler {
        static final int CONNECT_SUCCESS = 1;
        static final int DISCONNECT = 2;
        static final int RECEIVE_DATA = 3;
        static final int SEND_SATA = 4;

        final WeakReference<Context> mContext;

        ClientHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    displayToast("连接成功");
                    tv.setText("已经连接");
                    break;
                case DISCONNECT:
                    displayToast("连接已断开");
                    tv.setText(null);
                    setButtonOnStartState(true);//设置按键状态为可开始
                    break;
                case RECEIVE_DATA:
                    String str = (String) msg.obj;
                    System.out.println(msg.obj);
                    tv.setText(str);
                    break;
                case SEND_SATA:
                    etSend.setText("");
                    break;
            }
        }
    }

    private String getSocketId() {
       return "LocalSocketClient02";
    }
}
