package com.aite.udplib.api;

public class BaseApi implements IApi {
    @ParamType
    protected int paramType;

    @Override
    public int getParamType() {
        return paramType;
    }

    @Override
    public void setParamType(int paramType) {
        this.paramType = paramType;
    }
}
