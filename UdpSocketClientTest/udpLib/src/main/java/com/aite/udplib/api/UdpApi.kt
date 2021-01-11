package com.aite.udplib.api

class UdpApi<T> private constructor(): BaseApi() {
    companion object {
        fun <T> sendJson(): UdpApi<T> {
            val api = UdpApi<T>()
            api.paramType = ParamType.json
            return api
        }

        fun <T> sendXml(): UdpApi<T> {
            val api = UdpApi<T>()
            api.paramType = ParamType.xml
            return api
        }
    }
}