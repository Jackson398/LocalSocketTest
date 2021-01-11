package com.aite.udplib.utils;

import android.util.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;

public class XStreamUtil {

    private final static String TAG = "XStreamUtil";

    /**
     * java 转换成xml
     *
     * @param obj 对象实例
     * @return String xml字符串
     */
    public static String toXml(Object obj) {
        try {
            XStream xstream = new XStream(new Xpp3DomDriver());
            xstream.processAnnotations(obj.getClass()); //支持通过注解方式
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + xstream.toXML(obj);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toXml: null "+obj.getClass().getSimpleName());
            return "";
        }
    }

    /**
     * 将传入xml文本转换成Java对象
     *
     * @param xmlStr xml格式字符串
     * @param cls 实体类
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T toBean(String xmlStr, Class<T> cls) {
        try {
            XStream xstream = new XStream(new Xpp3DomDriver());
            xstream.processAnnotations(cls); //支持通过注解方式
            return (T) xstream.fromXML(xmlStr);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: xmlStr="+xmlStr );
            return null;
        }
    }
}
