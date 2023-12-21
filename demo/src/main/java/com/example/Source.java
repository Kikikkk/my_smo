package com.example;

import java.util.function.DoubleSupplier;

public class Source {
    private final DoubleSupplier distribution;
    private final int id;
    private Request lastRequest;
    private double lastGenTime;
    private static int idCounter;
    private int requestCounter;

    public Source(DoubleSupplier distribution, int sourceCount) {
        this.id = idCounter % sourceCount;
        idCounter++;
        this.distribution = distribution;
        lastRequest = generateNextRequest();
    }

    public Request getLastRequest() {
        return lastRequest;
    }

    private Request generateNextRequest() {
        lastRequest = new Request(lastGenTime + distribution.getAsDouble(), id, ++requestCounter);
        lastGenTime = lastRequest.getGenTime();
        return lastRequest;
    }

    public Request extractLastRequest() {
        Request prevRequest = lastRequest;
        lastRequest = generateNextRequest();
        return prevRequest;
    }
}
