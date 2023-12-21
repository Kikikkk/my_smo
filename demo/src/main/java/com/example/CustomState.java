package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomState {
    private static int numSources;
    private static int numDevices;
    private static int bufferCapacity;
    
    public static int getSourceCount() {
        return numSources;
    }
    
    public static int getDeviceCount() {
        return numDevices;
    }
    
    public static int getBufferCapacity() {
        return bufferCapacity;
    }
    
    public static void configureSystem(int sourceCount, int deviceCount, int bufferCapacity) {
        numSources = sourceCount;
        numDevices = deviceCount;
        CustomState.bufferCapacity = bufferCapacity;
    }
    
    private double timestamp;
    private Integer sourceId;
    private Integer deviceId;
    private boolean deviceInUse;
    private Integer bufferId;
    private boolean bufferOccupied;
    private String requestId;
    
    private List<Boolean> deviceStates;
    private List<Boolean> sourceStates;
    private List<Boolean> bufferStates;
    private boolean isFailed;
    
    public CustomState(double timestamp, String requestIdentifier) {
        this.timestamp = timestamp;
        this.requestId = requestIdentifier;
    }
    
    public static CustomState initialize() {
        CustomState state = new CustomState(0, "");
        state.sourceStates = new ArrayList<>(Collections.nCopies(numSources, false));
        state.deviceStates = new ArrayList<>(Collections.nCopies(numDevices, false));
        state.bufferStates = new ArrayList<>(Collections.nCopies(bufferCapacity, false));
        return state;
    }
    
    public double getTime() {
        return timestamp;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public List<Boolean> getDeviceStates() {
        return deviceStates;
    }
    
    public List<Boolean> getSourceStates() {
        return sourceStates;
    }
    
    public List<Boolean> getBufferStates() {
        return bufferStates;
    }
    
    public boolean isFailed() {
        return isFailed;
    }
    
    public Integer getSourceId() {
        return sourceId;
    }
    
    public Integer getDeviceId() {
        return deviceId;
    }
    
    public Integer getBufferId() {
        return bufferId;
    }
    
    public CustomState setSourceEvent(int sourceId) {
        this.sourceId = sourceId;
        return this;
    }
    
    public CustomState setDeviceExitEvent(int deviceId) {
        this.deviceId = deviceId;
        deviceInUse = false;
        return this;
    }
    
    public CustomState setDeviceEntryEvent(int deviceId) {
        this.deviceId = deviceId;
        deviceInUse = true;
        return this;
    }
    
    public CustomState setDeviceEntryEvent(int deviceId, int bufferId) {
        this.deviceId = deviceId;
        this.bufferId = bufferId;
        deviceInUse = true;
        bufferOccupied = false;
        return this;
    }
    
    public CustomState setBufferEvent(int bufferId) {
        this.bufferId = bufferId;
        bufferOccupied = true;
        return this;
    }
    
    public CustomState updateStateFrom(CustomState previousState) {
        this.sourceStates = new ArrayList<>(Collections.nCopies(numSources, false));
        this.deviceStates = new ArrayList<>(previousState.deviceStates);
        this.bufferStates = new ArrayList<>(previousState.bufferStates);
        if (deviceId != null) {
            this.deviceStates.set(deviceId, deviceInUse);
        }
        if (bufferId != null) {
            this.bufferStates.set(bufferId, bufferOccupied);
        }
        if (sourceId != null) {
            this.sourceStates.set(sourceId, true);
        }
        
        return this;
    }
    
    public void markAsFailed() {
        this.isFailed = true;
    }
    
    public CustomState mergeStates(CustomState currentState) {
        return bufferId != null ? this : currentState;
    }
}
