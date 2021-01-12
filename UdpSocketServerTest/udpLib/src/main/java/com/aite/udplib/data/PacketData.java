package com.aite.udplib.data;

import com.aite.udplib.api.ParamType;
import com.aite.udplib.api.ProtocolType;

public class PacketData {
    private static final String TAG = "PacketData";

    @ParamType
    public int paramType;  // 请求类型

    @ProtocolType
    public String protocolType; // 响应类型

    public MessageData data;

    @Override
    public String toString() {
        return "PacketData{" +
                "paramType=" + paramType +
                ", protocolType='" + protocolType + '\'' +
                ", data=" + data +
                '}';
    }

    public static class MessageData {
        public int type; // 0: 心跳包 1：普通信息

        public String content;

        @Override
        public String toString() {
            return "MessageData{" +
                    "type=" + type +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
