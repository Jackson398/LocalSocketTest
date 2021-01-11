package com.aite.udplib.api;

import android.util.Log;

import com.aite.udplib.data.PacketData;
import com.aite.udplib.rx.RxBus;
import com.aite.udplib.utils.JsonUtil;
import com.aite.udplib.utils.XStreamUtil;

public class UdpScheduler {
    private final static String TAG = "UdpScheduler ";

    public static void notifyLiveDataChanged(IApi api, String response) {
        PacketData result = new PacketData();
        switch (api.getProtocolType()) {
            case ProtocolType.APP_XML:
                 result = XStreamUtil.toBean(response, PacketData.class);
                break;
            case ProtocolType.APP_JSON:
                result = JsonUtil.toBean(response, PacketData.class);
                break;
            default:
        }
        Log.d(TAG, "result="+ result +", protocolType="+ api.getProtocolType());
        if (result == null) {
            // todo
        } else {
            RxBus.getInstance().post(result);
        }
    }

    public static String getPostMessage(IApi api, String params, int type) {
        String reqBody = "";
        PacketData packetData = new PacketData();
        packetData.paramType = api.getParamType();
        PacketData.MessageData messageData =new PacketData.MessageData();
        messageData.type = type;
        messageData.content = params;
        packetData.data = messageData;
        switch (api.getParamType()) {
            case ParamType.none: {
                reqBody = "";
                Log.d(TAG, "1 reqBody=$reqBody");
            }
            case ParamType.xml: {
                reqBody = XStreamUtil.toXml(packetData);
                Log.d(TAG, "2 reqBody=$reqBody");
            }
            case ParamType.json: {
                reqBody = JsonUtil.toJsonString(packetData);
                Log.d(TAG, "3 reqBody=$reqBody");
            }
        }
        Log.d(TAG, "reqBody=$reqBody");
        return reqBody;
    }
}
