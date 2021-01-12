package com.aite.udplib.api

class UdpApi<T> private constructor(): BaseApi() {
    companion object {
        fun <T> sendJson(): UdpApi<T> {
            val api = UdpApi<T>()
            api.paramType = ParamType.json
            api.protocolType = ProtocolType.APP_JSON
            return api
        }

        fun <T> sendXml(): UdpApi<T> {
            val api = UdpApi<T>()
            api.paramType = ParamType.xml
            api.protocolType = ProtocolType.APP_XML
            return api
        }
    }
}