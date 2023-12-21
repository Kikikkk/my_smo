package com.example;

public class SourceInfo {
    private final int id;
    private final int requestCount;
    private final double failureProb;
    private final double avgTimeInSystem;
    private final double avgTimeInBuffer;
    private final double avgTimeInDevice;
    private final double dmServiceTime;
    private final double dmBufferTime;
    
    public SourceInfo(int id, int requestCount, double failureProb, double avgTimeInSystem, double avgTimeInBuffer,
                      double avgTimeInDevice, double dmServiceTime, double dmBufferTime) {
        this.id = id;
        this.requestCount = requestCount;
        this.failureProb = failureProb;
        this.avgTimeInSystem = avgTimeInSystem;
        this.avgTimeInBuffer = avgTimeInBuffer;
        this.avgTimeInDevice = avgTimeInDevice;
        this.dmServiceTime = dmServiceTime;
        this.dmBufferTime = dmBufferTime;
    }
    
    public int getId() {
        return id;
    }
    
    public int getRequestCount() {
        return requestCount;
    }
    
    public double getFailureProb() {
        return failureProb;
    }
    
    
    public double getAvgTimeInSystem() {
        return avgTimeInSystem;
    }
    
    public double getAvgTimeInBuffer() {
        return avgTimeInBuffer;
    }
    
    public double getAvgTimeInDevice() {
        return avgTimeInDevice;
    }
    
    public double getDmServiceTime() {
        return dmServiceTime;
    }
    
    public double getDmBufferTime() {
        return dmBufferTime;
    }
}

