package com.aite.udplib.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private final static String TAG = "jsonUtil";

    /**
     * 转成json
     *
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * 转成bean
     *
     * @param json json字符串
     * @param cls
     * @return
     */
    public static <T> T toBean(String json, Class<T> cls) {
        T t = null;
        try {
            t = new Gson().fromJson(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: json=" + json);
        }
        return t;
    }

    /**
     * 转成list
     * 泛型在编译期类型被擦除导致报错
     *
     * @param json
     * @param cls
     * @return
     */
    public static <T> List<T> toJsonList(String json, Class<T> cls) {
        List<T> list = null;
        try {
            list = new Gson().fromJson(json, new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: jsonString=" + json);
        }

        return list;
    }

    /**
     * 转成list
     * 解决泛型问题
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public <T> List<T> toList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        try {
            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (final JsonElement elem : array) {
                list.add(gson.fromJson(elem, cls));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: jsonString=" + json);
        }

        return list;
    }


    /**
     * 转成list中有map的
     *
     * @param json
     * @return
     */
    public static <T> List<Map<String, T>> toListMaps(String json) {
        List<Map<String, T>> list = null;
        try {
            list = new Gson().fromJson(json,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: jsonString=" + json);
        }

        return list;
    }


    /**
     * 转成map的
     *
     * @param json
     * @return
     */
    public static <T> Map<String, T> toMaps(String json) {
        Map<String, T> map = null;
        try {
            map = new Gson().fromJson(json, new TypeToken<Map<String, T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "toBean: jsonString=" + json);
        }

        return map;
    }

    @Nullable
    public static <T> T fromJson(String json, @NonNull Type type) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        String typeString = "";
        if (type instanceof Class) {
            typeString = ((Class) type).getSimpleName();
        }
        if (isString(typeString)) {
            try {
                return (T) json;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }
        return new Gson().fromJson(json, type);
    }

    private static boolean isString(String typeString) {
        return typeString.startsWith("String");
    }

    public static String getValue(String json, String key) {
        String eventType = null;
        try {
            eventType = new JsonParser().parse(json).getAsJsonObject().get(key).getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getValue: json=" + json);
        }
        return eventType;
    }
}
