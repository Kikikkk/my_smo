package com.example;

import java.util.function.DoubleSupplier;

public class Device {
    private static int idCounter;
    private final int id;
    private double exitTime;
    private final DoubleSupplier distribution;

    public Device(DoubleSupplier distribution, int deviceCount) {
        this.id = idCounter % deviceCount;
        idCounter++;
        this.distribution = distribution;
    }

    public int getId() {
        return id;
    }

    public double getExitTime() {
        return exitTime;
    }

    public void putRequest(Request request, double curTime) {
        if (curTime >= exitTime && request != null) {
            double entryTime = Math.max(exitTime, request.getGenTime());
            exitTime = entryTime + distribution.getAsDouble();

            request.setEntryTime(entryTime);
            request.setExitTime(exitTime);
            request.setDeviceId(id);
        }
    }
}
