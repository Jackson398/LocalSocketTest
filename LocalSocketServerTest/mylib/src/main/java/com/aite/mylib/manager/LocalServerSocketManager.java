package com.aite.mylib.manager;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.text.TextUtils;
import android.util.Log;

import com.aite.mylib.model.Common;
import com.aite.mylib.model.LocalSocketConst;
import com.aite.mylib.model.PacketData;
import com.aite.mylib.utils.DataUtils;

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

    private boolean isRunning = false;

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
    }

    public void addLocalSocketManager(String name, LocalSocketManager localSocketManager) {
        LocalSocketManager last = mLocalSocketMangerMap.get(name);
        if (last != null && last != localSocketManager) {
            last.sendClose();
        }
        mLocalSocketMangerMap.put(name, localSocketManager);
    }

    public LocalSocketManager addLocalSocket(LocalSocket interactClientSocket) {
        LocalSocketManager socketManager = new LocalSocketManager(interactClientSocket);
        mLocalSocketManagerList.add(socketManager);
        return socketManager;
    }

    public LocalSocketManager getLocalSocketManager(String from) {
        return mLocalSocketMangerMap.get(from);
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

        private boolean isRunning;

        private List<PacketData> mPacketDataList;

        private String mFrom;

        private DataUpdateListener mListener;

        public LocalSocketManager(LocalSocket localSocket) {
            Log.d(TAG, "====LocalSocketManager===");
            this.mLocalSocket = localSocket;
            isRunning = true;
            mPacketDataList = new ArrayList<>(Common.DEFAULT_ARRAY_LIST_SIZE);
            try {
                mOutputStream = localSocket.getOutputStream();
                mInputStream = localSocket.getInputStream();
                createReceiveThread();
                createSendThread();
            } catch (IOException exception) {
                Log.e(TAG, "LocalSocketManager error, msg=" + exception.getMessage());
                isRunning = false;
            }
        }

        public void setDataUpdateListener(DataUpdateListener listener) {
            this.mListener = listener;
        }

        public DataUpdateListener getDataUpdateListener() {
            return this.mListener;
        }

        private void createReceiveThread() {
            mReceiveThread = new Thread() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            PacketData receiveData = PacketData.readPacketData(mInputStream);
                            if (receiveData.type == LocalSocketConst.TYPE_LOGIN) {
                                mFrom = receiveData.content;
                                mListener.login(receiveData.content);
                                Log.d(TAG, "createReceiveThread, from=" + receiveData.content);
                                addLocalSocketManager(receiveData.content, LocalSocketManager.this);
                            } else if (receiveData.type == LocalSocketConst.TYPE_CLOSE) {
                                close();
                            } else {
                                Log.d(TAG, "createReceiveThread, receive data, content=" + receiveData.content);
                                mListener.receive(receiveData.content);
//                                sendJson("service of " + receiveData.content);
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
            isRunning = false;
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
                    while (isRunning) {
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

    public interface DataUpdateListener {
        void login(String name);
        void receive(String msg);
    }
}
