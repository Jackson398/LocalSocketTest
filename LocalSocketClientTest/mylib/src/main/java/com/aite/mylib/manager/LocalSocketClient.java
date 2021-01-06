package com.aite.mylib.manager;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.text.TextUtils;
import android.util.Log;

import com.aite.mylib.model.LocalSocketConst;
import com.aite.mylib.model.PacketData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocalSocketClient {
    private static final String TAG = "LocalSocketClient";

    LocalSocket localSocket;

    public static int id = -1;

    public String socketId;

    private OutputStream outputStream;

    private InputStream inputStream;

    private Thread receiveThread;

    private Thread sendThread;

    private boolean isisRunning;

    private List<PacketData> sendData;

    public LocalSocketClient() {
        localSocket = new LocalSocket();
        socketId = getSocketId();
        sendData = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                LocalSocketAddress localSocketAddress = new LocalSocketAddress(LocalServerSocketManager.SERVICE_NAME);
                try {
                    localSocket.connect(localSocketAddress);
                    isisRunning = true;
                    inputStream = localSocket.getInputStream();
                    outputStream = localSocket.getOutputStream();
                    sendData.add(new PacketData().setType(LocalSocketConst.TYPE_LOGIN).setContent(socketId));
                    createReceiveThread();
                    createSendThread();
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }.start();
    }

    private void createReceiveThread() {
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (isisRunning) {
                    try {
                        PacketData receive = PacketData.readPacketData(inputStream);
                        if (receive.type == LocalSocketConst.TYPE_CLOSE) {
                            Log.d("LocalClient", "receive type close");
                            close();
                        } else {
                            sendJson("service of " + receive.content);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        close();
                    }
                }
            }
        };
        receiveThread.start();
    }

    private void close() {
        isisRunning = false;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
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
        inputStream = null;
        outputStream = null;
        localSocket = null;
    }

    private void createSendThread() {
        sendThread = new Thread() {
            @Override
            public void run() {
                while (isisRunning) {
                    try {
                        if (sendData.size() > 0) {
                            PacketData data = sendData.remove(0);
                            byte[] datas = new byte[1];
                            datas[0] = data.type;
                            outputStream.write(datas);
                            if (data.type != LocalSocketConst.TYPE_CLOSE) {
                                outputStream.write(data.getContent());
                            }
                            outputStream.flush();
                        }
                        if (sendData.size() == 0) {
                            try {
                                sleep(50000);
                            } catch (InterruptedException e) {
//                                e.printStackTrace();
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

    public void sendJson(String json){
        if(TextUtils.isEmpty(json)){
            return;
        }
        sendData.add(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_JSON).setContent(json));
        if(sendData.size()==1){
            sendThread.interrupt();
        }
    }

    public void sendXml(String xml){
        if(TextUtils.isEmpty(xml)){
            return;
        }
        sendData.add(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_XML).setContent(xml));
        if(sendData.size()==1){
            sendThread.interrupt();
        }
    }

    private static String getSocketId() {
        if (id < 0) {
            id = new Random().nextInt(10000);
        }
        id++;
        return "local_client_" + id;
    }
}
