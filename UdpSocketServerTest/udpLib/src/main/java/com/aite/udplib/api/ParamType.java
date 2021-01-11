package com.aite.udplib.api;

import androidx.annotation.IntDef;

@IntDef({ParamType.none, ParamType.json,ParamType.xml})
public @interface ParamType {
    int none = 1;
    int json = 2;
    int xml = 3;
}
