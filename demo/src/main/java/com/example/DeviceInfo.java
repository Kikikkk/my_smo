package com.example;

public class DeviceInfo {
    private final int id;
    private final double loadFactor;
    
    public DeviceInfo(int id, double loadFactor) {
        this.id = id;
        this.loadFactor = loadFactor;
    }
    
    public int getId() {
        return id;
    }
    
    public double getLoadFactor() {
        return loadFactor;
    }
}

