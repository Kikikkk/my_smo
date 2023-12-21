package com.example;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private final int bufferCapacity;
    private final List<Boolean> condition;
    private int pointer;
    private final List<Request> requests = new ArrayList<>();

    public Buffer(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
        condition = new ArrayList<>(bufferCapacity);
        for (int i = 0; i < bufferCapacity; i++) {
            condition.add(false);
        }
    }

    public int getSize() {
        return requests.size();
    }

    public boolean isFull() {
        return getSize() == bufferCapacity;
    }

    private int findFreeBufferId() {
        for (int i = 0; i < condition.size(); i++) {
            if (!condition.get(i)) {
                return i;
            }
        }
        return -1; 
    }

    public void addRequest(Request request) {
        if (!isFull()) {
            int bufferId = findFreeBufferId();
            if (bufferId != -1) {
                condition.set(bufferId, true);
                request.setBufferId(bufferId);
                requests.add(request);
            }
        }
    }

    public Request removePriorityRequest() {
        if (!requests.isEmpty()) {
            int initialPointer = pointer;
            int currentPointer = pointer;
            Request selectedRequest = null;
            do {
                Request currentRequest = requests.get(currentPointer);
                if (!currentRequest.isRefused()) {
                    selectedRequest = currentRequest;
                    requests.remove(currentPointer);
                    break;
                }
                currentPointer = (currentPointer + 1) % requests.size();
            } while (currentPointer != initialPointer);
            if (selectedRequest != null) {
                pointer = currentPointer;
                condition.set(selectedRequest.getBufferId(), false);
            }
            return selectedRequest;
        }
        return null;
    }

    public Request getPriorityRequest() {
        if (!requests.isEmpty()) {
            int initialPointer = pointer;
            int currentPointer = pointer;
            Request selectedRequest = null;
            do {
                Request currentRequest = requests.get(currentPointer);
                if (!currentRequest.isRefused()) {
                    selectedRequest = currentRequest;
                    break;
                }
                currentPointer = (currentPointer + 1) % requests.size();
            } while (currentPointer != initialPointer);
            if (selectedRequest != null) {
                pointer = currentPointer;
            }
            return selectedRequest;
        }
        return null;
    }
}
