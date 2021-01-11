package com.aite.udplib.api

interface IApi {
    @ParamType
    var paramType: Int

    @ProtocolType
    var protocolType: String
}