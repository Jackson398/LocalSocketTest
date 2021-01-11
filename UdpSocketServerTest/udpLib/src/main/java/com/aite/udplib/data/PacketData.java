package com.aite.udplib.data;

import com.aite.udplib.api.ParamType;

public class PacketData {
    private static final String TAG = "PacketData";

    @ParamType
    public int paramType;

    public MessageData data;

    @Override
    public String toString() {
        return "PacketData{" +
                "paramType=" + paramType +
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
