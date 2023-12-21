package com.example;

import java.util.*;
import java.util.stream.Collectors;

public class Statistic {
    private final int sourceCount;
    private final int deviceCount;
    private final List<Request> allRequests = new ArrayList<>();
    private final Map<Double, CustomState> states = new TreeMap<>();

    public Statistic(int sourceCount, int deviceCount, int bufferCapacity) {
        this.sourceCount = sourceCount;
        this.deviceCount = deviceCount;
        CustomState.configureSystem(sourceCount, deviceCount, bufferCapacity);
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void addRequest(Request request) {
        allRequests.add(request);
    }

    public Collection<CustomState> fillConditions() {
        createStatesForRequests();
        sortStatesByTime();
        updateStatesFromPrevious();
        return new ArrayList<>(states.values());
    }

    private void sortStatesByTime() {
        states.values().stream()
                .sorted(Comparator.comparingDouble(CustomState::getTime))
                .forEach(state -> states.put(state.getTime(), state));
    }

    private void createStatesForRequests() {
        for (Request request : allRequests) {
            String requestId = (request.getSrcId() + 1) + "." + request.getRequestId();
            CustomState stateWhenGen = createStateWhenGenerated(request, requestId);
            states.put(request.getGenTime(), stateWhenGen);

            if (request.getExitTime() != 0) {
                CustomState conditionWhenExit = createStateWhenExited(request, requestId);
                addState(conditionWhenExit);
            }
        }
    }

    private CustomState createStateWhenGenerated(Request request, String requestId) {
        CustomState stateWhenGen = new CustomState(request.getGenTime(), requestId)
                .setSourceEvent(request.getSrcId());

        if (request.getEntryTime() == request.getGenTime()) {
            stateWhenGen.setDeviceEntryEvent(request.getDeviceId());
        } else if (request.getEntryTime() != 0) {
            stateWhenGen.setBufferEvent(request.getBufferId());
            CustomState conditionWhenEntry = createStateWhenEnteredBuffer(request, requestId);
            addState(conditionWhenEntry);
        } else {
            stateWhenGen.markAsFailed();
        }
        return stateWhenGen;
    }

    private CustomState createStateWhenEnteredBuffer(Request request, String reqUniqueNumber) {
        return new CustomState(request.getEntryTime(), reqUniqueNumber)
                .setDeviceEntryEvent(request.getDeviceId(), request.getBufferId());
    }

    private CustomState createStateWhenExited(Request request, String reqUniqueNumber) {
        CustomState state = new CustomState(request.getExitTime(), reqUniqueNumber)
                .setDeviceExitEvent(request.getDeviceId());
        return state;
    }

    private void addState(CustomState state) {
        states.merge(state.getTime(), state, CustomState::mergeStates);
    }

    private void updateStatesFromPrevious() {
        CustomState prevState = CustomState.initialize();
        for (CustomState state : states.values()) {
            prevState = state.updateStateFrom(prevState);
        }
    }

    public List<SourceInfo> getSourcesStat() {
        List<SourceInfo> sourceInfoList = new ArrayList<>(sourceCount);
        List<Integer> countFromSource = countFromSource();
        List<Double> failureProb = getAllFailureProb();
        List<Double> avgTimeInSystem = avgTimeInSystem();
        List<Double> avgTimeInDevice = avgTimeInDevice();
        List<Double> avgTimeInBuffer = avgTimeInBuffer();
        List<Double> dmServiceTime = dmServiceTime();
        List<Double> dmBufferTime = dmBufferTime();
        for (int i = 0; i < sourceCount; i++) {
            sourceInfoList.add(new SourceInfo(i + 1, countFromSource.get(i), failureProb.get(i), avgTimeInSystem.get(i),
                    avgTimeInBuffer.get(i), avgTimeInDevice.get(i), dmServiceTime.get(i), dmBufferTime.get(i)));
        }
        return sourceInfoList;
    }

    public List<DeviceInfo> getDeviceStat() {
        List<DeviceInfo> deviceInfoList = new ArrayList<>(deviceCount);
        List<Double> loadFactors = loadDeviceFactors();
        for (int i = 0; i < deviceCount; i++) {
            deviceInfoList.add(new DeviceInfo(i + 1, loadFactors.get(i)));
        }
        return deviceInfoList;
    }

    public double getCommonFailureProb() {
        long totalRequests = allRequests.size();
        long unhandledCount = allRequests.stream()
                .filter(request -> request.getExitTime() == 0)
                .count();
    
        return (double) unhandledCount / totalRequests;
    }    

    public List<Integer> countFromSource() {
        return allRequests.stream()
                .collect(Collectors.groupingBy(Request::getSrcId, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().intValue())
                .collect(Collectors.toList());
    }

    public List<Double> getAllFailureProb() {
        List<Integer> countFrom = new ArrayList<>(Collections.nCopies(sourceCount, 0));
        List<Integer> unhandledCountFrom = new ArrayList<>(Collections.nCopies(sourceCount, 0));
        List<Double> probabilitiesOfFail = new ArrayList<>(Collections.nCopies(sourceCount, 0.0));

        for (Request request : allRequests) {
            int srcId = request.getSrcId();
            countFrom.set(srcId, countFrom.get(srcId) + 1);
            if (request.getExitTime() == 0) {
                unhandledCountFrom.set(srcId, unhandledCountFrom.get(srcId) + 1);
            }
        }

        for (int i = 0; i < sourceCount; i++) {
            if (countFrom.get(i) > 0) {
                probabilitiesOfFail.set(i, (double) unhandledCountFrom.get(i) / countFrom.get(i));
            }
        }

        return probabilitiesOfFail;
    }

    public List<Double> avgTimeInSystem() {
        List<Double> timeInSystem = new ArrayList<>(Collections.nCopies(sourceCount, 0.0));
        List<Integer> countFrom = new ArrayList<>(Collections.nCopies(sourceCount, 0));

        for (Request request : allRequests) {
            int srcId = request.getSrcId();
            double time = Math.max(0, request.getExitTime() - request.getGenTime());
            timeInSystem.set(srcId, timeInSystem.get(srcId) + time);
            countFrom.set(srcId, countFrom.get(srcId) + 1);
        }

        for (int i = 0; i < sourceCount; i++) {
            if (countFrom.get(i) > 0) {
                timeInSystem.set(i, timeInSystem.get(i) / countFrom.get(i));
            }
        }

        return timeInSystem;
    }

    public List<Double> avgTimeInBuffer() {
        List<Double> timeInBuffer = new ArrayList<>(Collections.nCopies(sourceCount, 0.0));
        List<Integer> countFrom = new ArrayList<>(Collections.nCopies(sourceCount, 0));

        for (Request request : allRequests) {
            int srcId = request.getSrcId();
            double bufferTime = Math.max(0, request.getEntryTime() - request.getGenTime());
            timeInBuffer.set(srcId, timeInBuffer.get(srcId) + bufferTime);
            countFrom.set(srcId, countFrom.get(srcId) + 1);
        }

        for (int i = 0; i < sourceCount; i++) {
            if (countFrom.get(i) > 0) {
                timeInBuffer.set(i, timeInBuffer.get(i) / countFrom.get(i));
            }
        }

        return timeInBuffer;
    }

    public List<Double> avgTimeInDevice() {
        List<Double> timeInDevice = new ArrayList<>(sourceCount);
        List<Integer> countFrom = new ArrayList<>(sourceCount);

        for (int i = 0; i < sourceCount; i++) {
            timeInDevice.add(0.0);
            countFrom.add(0);
        }

        for (Request request : allRequests) {
            int srcId = request.getSrcId();
            timeInDevice.set(srcId, timeInDevice.get(srcId) + (request.getExitTime() - request.getEntryTime()));
            countFrom.set(srcId, countFrom.get(srcId) + 1);
        }

        for (int i = 0; i < sourceCount; i++) {
            if (countFrom.get(i) > 0) {
                timeInDevice.set(i, timeInDevice.get(i) / countFrom.get(i));
            }
        }

        return timeInDevice;
    }

    public List<Double> loadDeviceFactors() {
        List<Double> loadDeviceFactors = new ArrayList<>(Collections.nCopies(deviceCount, 0.0));
        double systemTime = 0;

        for (Request request : allRequests) {
            int deviceId = request.getDeviceId();
            if (isValidDeviceId(deviceId)) {
                double exitTime = request.getExitTime();
                double entryTime = request.getEntryTime();
                double serviceTime = exitTime - entryTime;
                loadDeviceFactors.set(deviceId, loadDeviceFactors.get(deviceId) + serviceTime);

                systemTime = Math.max(systemTime, exitTime);
            }
        }

        if (systemTime > 0) {
            for (int i = 0; i < deviceCount; i++) {
                loadDeviceFactors.set(i, loadDeviceFactors.get(i) / systemTime);
            }
        }

        return loadDeviceFactors;
    }

    private boolean isValidDeviceId(int deviceId) {
        return deviceId >= 0 && deviceId < deviceCount;
    }

    public List<Double> dmServiceTime() {
        List<Double> dmServiceTime = new ArrayList<>(sourceCount);

        for (int srcId = 0; srcId < sourceCount; srcId++) {
            double sumServiceTime = 0.0;
            int countFrom = 0;
            List<Double> serviceTimes = new ArrayList<>();

            for (Request request : allRequests) {
                if (request.getSrcId() == srcId) {
                    double requestServiceTime = request.getExitTime() - request.getEntryTime();
                    sumServiceTime += requestServiceTime;
                    serviceTimes.add(requestServiceTime);
                    countFrom++;
                }
            }

            if (countFrom == 0) {
                dmServiceTime.add(0.0);
            } else {
                double averageTime = sumServiceTime / countFrom;

                double sum = serviceTimes.stream()
                        .map(d -> Math.pow(d - averageTime, 2))
                        .mapToDouble(d -> d)
                        .sum();

                dmServiceTime.add(sum / (countFrom - 1));
            }
        }

        return dmServiceTime;
    }

    public List<Double> dmBufferTime() {
        List<Double> dmBufferTime = new ArrayList<>(sourceCount);

        for (int srcId = 0; srcId < sourceCount; srcId++) {
            double sumBufferTime = 0.0;
            int countFrom = 0;
            List<Double> bufferTimes = new ArrayList<>();

            for (Request request : allRequests) {
                if (request.getSrcId() == srcId) {
                    double requestBufferTime = Math.max(0, request.getEntryTime() - request.getGenTime());
                    sumBufferTime += requestBufferTime;
                    bufferTimes.add(requestBufferTime);
                    countFrom++;
                }
            }

            if (countFrom == 0) {
                dmBufferTime.add(0.0);
            } else {
                double averageTime = sumBufferTime / countFrom;

                double sum = bufferTimes.stream()
                        .map(d -> Math.pow(d - averageTime, 2))
                        .mapToDouble(d -> d)
                        .sum();

                dmBufferTime.add(sum / (countFrom - 1));
            }
        }
        return dmBufferTime;
    }
}
