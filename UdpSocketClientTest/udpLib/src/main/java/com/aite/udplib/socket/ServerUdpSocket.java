package com.aite.udplib.socket;

import android.content.Context;
import android.util.Log;

import com.aite.udplib.api.UdpApi;
import com.aite.udplib.api.UdpScheduler;
import com.aite.udplib.data.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerUdpSocket {
    private static final String TAG = "ServerUdpSocket";

    // 单个CPU线程池大小
    private static final int POOL_SIZE = 5;

    private static final int BUFFER_LENGTH = 1024;
    private byte[] receiveByte = new byte[BUFFER_LENGTH];

    // 端口号
    public static final int SERVER_PORT = 65432;

    private volatile boolean isThreadRunning = false;

    private Context mContext;
    private DatagramSocket server;
    private DatagramPacket receivePacket;

    private long lastReceiveTime = 0;
    private static final long TIME_OUT = 120 * 1000;

    // 每隔6s发送一个心跳包判断接收设备是否在线
    private static final long HEARTBEAT_MESSAGE_DURATION = 6 * 1000;

    private ExecutorService mThreadPool;
    private Thread serverThread;
    private User localUser;
    private User remoteUser;

    public ServerUdpSocket(Context context) {
        this.mContext = context;

        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * POOL_SIZE);
        // 记录创建对象时的时间
        lastReceiveTime = System.currentTimeMillis();

//        createUser();
    }

    public void startUdpSocket() {
        if (server != null) return;
        try {
            // 表明这个 Socket 在设置的端口上监听数据。
            server = new DatagramSocket(SERVER_PORT);

            if (receivePacket == null) {
                // 创建接受数据的 packet
                receivePacket = new DatagramPacket(receiveByte, BUFFER_LENGTH);
            }

            startSocketThread();
        } catch (SocketException e) {
            Log.e(TAG, "startUdpSocket fail, msg=" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 开启发送数据的线程
     */
    private void startSocketThread() {
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "serverThread is running...");
                receiveMessage();
            }
        });
        isThreadRunning = true;
        serverThread.start();
    }

    /**
     * 处理接受到的消息
     */
    private void receiveMessage() {
        while (isThreadRunning) {
            try {
                if (server != null) {
                    server.receive(receivePacket);
                }
                lastReceiveTime = System.currentTimeMillis();
                Log.d(TAG, "receive packet success...");
            } catch (IOException e) {
                Log.e(TAG, "UDP数据包接收失败！线程停止");
                stopUdpSocket();
                e.printStackTrace();
                return;
            }

            if (receivePacket == null || receivePacket.getLength() == 0) {
                Log.e(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }

            String strReceive = new String(receivePacket.getData(), 0, receivePacket.getLength());
            Log.d(TAG, strReceive + " from " + receivePacket.getAddress().getHostAddress() + ":" + receivePacket.getPort());
            UdpScheduler.notifyLiveDataChanged(UdpApi.Companion.sendJson(), strReceive);

            //解析接收到的 json 信息

            // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
            if (receivePacket != null) {
                receivePacket.setLength(BUFFER_LENGTH);
            }
        }
    }

    public void stopUdpSocket() {
        isThreadRunning = false;
        receivePacket = null;
        if (serverThread != null) {
            serverThread.interrupt();
        }
        if (server != null) {
            server.close();
            server = null;
        }
    }
}
