package com.aite.mylib.manager;

import android.annotation.SuppressLint;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.text.TextUtils;
import android.util.Log;

import com.aite.mylib.model.Common;
import com.aite.mylib.model.LocalSocketConst;
import com.aite.mylib.model.PacketData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalServerSocketManager {
    private static final String TAG = "LocalServerSocketManager";

    private static LocalServerSocketManager sInstance;

    private static final Object LOCK = new Object();

    public static final String SERVICE_NAME = "server_name";

    private LocalServerSocket mServerSocket;

    private boolean isisRunning = false;

    private Map<String, LocalSocketManager> mLocalSocketMangerMap;

    private List<LocalSocketManager> mLocalSocketManagerList;

    public static LocalServerSocketManager getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new LocalServerSocketManager();
                }
            }
        }
        return sInstance;
    }

    private LocalServerSocketManager() {
        mLocalSocketMangerMap = new HashMap<>(Common.DEFAULT_ARRAY_LIST_SIZE);
        mLocalSocketManagerList = new ArrayList<>(Common.DEFAULT_ARRAY_LIST_SIZE);
        new Thread() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                try {
                    isisRunning = true;
                    mServerSocket = new LocalServerSocket(SERVICE_NAME);
                } catch (IOException exception) {
                    Log.e(TAG, "constructor error, msg=" + exception.getMessage());
                    isisRunning = false;
                }

                while (isisRunning) {
                    Log.d(TAG, "wait for new client coming !");
                    try {
                        LocalSocket interactClientSocket = mServerSocket.accept();
                        Log.d(TAG, "accept one socket");
                        mLocalSocketManagerList.add(new LocalSocketManager(interactClientSocket));
                    } catch (IOException exception) {
                        Log.e(TAG, "constructor error, msg=" + exception.getMessage());
                    }
                }
            }
        }.start();
    }

    public void addLocalSocketManager(String name, LocalSocketManager localSocketManager) {
        LocalSocketManager last = mLocalSocketMangerMap.get(name);
        if (last != null && last != localSocketManager) {
            last.sendClose();
        }
        mLocalSocketMangerMap.put(name, localSocketManager);
    }

    public void addLocalSocket(LocalSocket interactClientSocket) {
        mLocalSocketManagerList.add(new LocalSocketManager(interactClientSocket));
    }

    public void sendJson(String json) {
//       Set<String> keys = managers.keySet();
//        for(String key:keys){
//            SocketManager socketManager = managers.get(key);
//            socketManager.sendJson(json);
//        }
        for (LocalSocketManager socketManager : mLocalSocketManagerList) {
            socketManager.sendJson(json);
        }
    }

    public void closeAll() {
        Set<String> keys = mLocalSocketMangerMap.keySet();
        for (String key : keys) {
            LocalSocketManager socketManager = mLocalSocketMangerMap.get(key);
            socketManager.sendClose();
            mLocalSocketManagerList.remove(socketManager);
        }
        mLocalSocketMangerMap.clear();
        for (LocalSocketManager socketManager : mLocalSocketManagerList) {
            socketManager.sendClose();
        }
        mLocalSocketManagerList.clear();
    }

    public void sendXml(String xml) {
        Set<String> keys = mLocalSocketMangerMap.keySet();
        for (String key : keys) {
            LocalSocketManager socketManager = mLocalSocketMangerMap.get(key);
            socketManager.sendXml(xml);
        }
    }

    public class LocalSocketManager {
        private static final String TAG = "LocalSocketManager";

        private LocalSocket mLocalSocket;

        private OutputStream mOutputStream;

        private InputStream mInputStream;

        private Thread mReceiveThread;

        private Thread mSendThread;

        private boolean isisRunning;

        private List<PacketData> mPacketDataList;

        private String mFrom;

        public LocalSocketManager(LocalSocket localSocket) {
            this.mLocalSocket = localSocket;
            isisRunning = true;
            mPacketDataList = new ArrayList<>(Common.DEFAULT_ARRAY_LIST_SIZE);
            try {
                mOutputStream = localSocket.getOutputStream();
                mInputStream = localSocket.getInputStream();
                createReceiveThread();
                createSendThread();
            } catch (IOException exception) {
                Log.e(TAG, "LocalSocketManager error, msg=" + exception.getMessage());
                isisRunning = false;
            }
        }

        private void createReceiveThread() {
            mReceiveThread = new Thread() {
                @Override
                public void run() {
                    while (isisRunning) {
                        try {
                            PacketData receiveData = PacketData.readPacketData(mInputStream);
                            if (receiveData.type == LocalSocketConst.TYPE_LOGIN) {
                                mFrom = receiveData.content;
                                Log.d(TAG, "createReceiveThread, from=" + mFrom);
                                addLocalSocketManager(receiveData.content, LocalSocketManager.this);
                            } else if (receiveData.type == LocalSocketConst.TYPE_CLOSE) {
                                close();
                            } else {
                                sendJson("service of " + receiveData.content);
                            }
                        } catch (IOException exception) {
                            Log.e(TAG, "createReceiveThread error, msg=" + exception.getMessage());
                        }
                    }
                }
            };
            mReceiveThread.start();
        }

        private void close() {
            if (TextUtils.isEmpty(mFrom)) {
                mLocalSocketMangerMap.remove(mFrom);
            }
            isisRunning = false;
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mLocalSocket != null) {
                try {
                    mLocalSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mInputStream = null;
            mOutputStream = null;
            mLocalSocket = null;
        }

        private void createSendThread() {
            mSendThread = new Thread() {
                @Override
                public void run() {
                    while (isisRunning) {
                        try {
                            if (mPacketDataList.size() > 0) {
                                PacketData data = mPacketDataList.remove(0);
                                byte[] datas = new byte[1];
                                datas[0] = data.type;
                                mOutputStream.write(datas);
                                if (data.type != LocalSocketConst.TYPE_CLOSE) {
                                    mOutputStream.write(data.getContent());
                                }
                                mOutputStream.flush();
                            }
                            if (mPacketDataList.size() == 0) {
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
            mSendThread.start();
        }

        public void sendJson(String json) {
            if (TextUtils.isEmpty(json)) {
                return;
            }
            sendPacket(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_JSON).setContent(json));
        }

        public void sendXml(String xml) {
            if (TextUtils.isEmpty(xml)) {
                return;
            }
            sendPacket(new PacketData().setType(LocalSocketConst.TYPE_CONTENT_XML).setContent(xml));
        }

        public void sendClose() {
            sendPacket(new PacketData().setType(LocalSocketConst.TYPE_CLOSE));
        }

        public void sendPacket(PacketData data) {
            mPacketDataList.add(data);
            if (mPacketDataList.size() == 1) {
                mSendThread.interrupt();
            }
        }
    }
}
