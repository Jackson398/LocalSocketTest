package com.aite.udplib.api;

import androidx.annotation.StringDef;

/**
 * 协议类型
 */
@StringDef({ProtocolType.APP_FORM_URLENCODED, ProtocolType.APP_JSON, ProtocolType.APP_XML, ProtocolType.APP_OCTET_STREAM, ProtocolType.MULTIPART_FORM_DATA, ProtocolType.TEXT_HTML, ProtocolType.TEXT_PLAIN, ProtocolType.WILDCARD})
public @interface ProtocolType {
    String APP_FORM_URLENCODED = "application/x-www-form-urlencoded";
    String APP_JSON = "application/json";
    String APP_XML = "application/xml";
    String APP_OCTET_STREAM = "application/octet-stream";
    String MULTIPART_FORM_DATA = "multipart/form-data";
    String TEXT_HTML = "text/html";
    String TEXT_PLAIN = "text/plain";
    String WILDCARD = "*/*";
}
