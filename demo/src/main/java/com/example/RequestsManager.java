package com.example;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestsManager {
    private final Buffer buffer;
    private final List<Device> devices;
    private int devicePointer;

    public RequestsManager(List<Device> devices, int bufferSize) {
        this.buffer = new Buffer(bufferSize);
        this.devices = devices;
        this.devicePointer = 0;
    }

    private Device findAvailableDevice(double currentTime) {
        Device bestDevice = null;
        double minExitTime = Double.MAX_VALUE;
        
        for (Device device : devices) {
            if (device.getExitTime() < currentTime && device.getExitTime() < minExitTime) {
                bestDevice = device;
                minExitTime = device.getExitTime();
            }
        }
        
        return bestDevice;
    }

    private void updatesSystem(double currentTime) {
        Device deviceForRequest;
    
        while ((deviceForRequest = findAvailableDevice(currentTime)) != null
                && buffer.getPriorityRequest() != null) {
            deviceForRequest.putRequest(buffer.removePriorityRequest(), currentTime);
        }
    }    

    public void addRequest(Request request) {
        double currentTime = request.getGenTime();
        updatesSystem(currentTime);
    
        int startingPosition = devicePointer;
    
        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get((startingPosition + i) % devices.size());
            if (device.getExitTime() < currentTime) {
                device.putRequest(request, currentTime);
                devicePointer = (startingPosition + i + 1) % devices.size();
                return;
            }
        }
        buffer.addRequest(request);
    }

    public void clearBuffer() {
        while (buffer.getSize() != 0) {
            Device closestDevice = Collections.min(devices, Comparator.comparingDouble(Device::getExitTime));
            closestDevice.putRequest(buffer.removePriorityRequest(), closestDevice.getExitTime());
        }
    }
}

