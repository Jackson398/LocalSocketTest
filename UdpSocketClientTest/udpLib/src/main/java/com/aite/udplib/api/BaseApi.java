package com.aite.udplib.api;

import org.jetbrains.annotations.NotNull;

public class BaseApi implements IApi {
    @ParamType
    protected int paramType;

    @ProtocolType
    protected String protocolType;

    @Override
    public int getParamType() {
        return paramType;
    }

    @Override
    public void setParamType(int paramType) {
        this.paramType = paramType;
    }

    @NotNull
    @Override
    public String getProtocolType() {
        return protocolType;
    }

    @Override
    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }
}
