package com.example;

public class Request {
    private final double genTime;
    private final int srcId;
    private final int requestId;
    private int deviceId = -1;
    private int bufferId = -1;
    private double entryTime;
    private double exitTime;
    private boolean isRefused;

    public Request(double genTime, int srcId, int requestId) {
        this.genTime = genTime;
        this.srcId = srcId;
        this.requestId = requestId;
    }

    public double getGenTime() {
        return genTime;
    }

    public int getSrcId() {
        return srcId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public double getExitTime() {
        return exitTime;
    }

    public double getEntryTime() {
        return entryTime;
    }

    public int getBufferId() {
        return bufferId;
    }

    public boolean isRefused() {
        return isRefused;
    }

    public void setExitTime(double exitTime) {
        this.exitTime = exitTime;
    }

    public void setDeviceId(int id) {
        this.deviceId = id;
    }

    public void setEntryTime(double entryTime) {
        this.entryTime = entryTime;
    }

    public void setBufferId(int bufferId) {
        this.bufferId = bufferId;
    }

    public void setRefused(boolean refused) {
        this.isRefused = refused;
    }
}
