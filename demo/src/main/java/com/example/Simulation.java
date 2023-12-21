package com.example;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation {
    private static int N = 1000;
    private Statistic statistic;

    public Simulation(int sourceCount, int deviceCount, int bufferSize, double a, double b, double lambda1, boolean auto) {
        // N = 2000;
        System.out.println("буф\tприб\t\tвер_отк\t\t\tзагр_при\t\t\tвремя\t\tстоимость");
        int devCost = 13700;
        int sourceCost1 = 120433;
        int sourceCost2 = 148924;
        int bufferCostx4 = 23150;
        int bufferCostx6 = 28900;
        int bufferCostx8 = 37145;
        for (double lambda = 0.2; lambda <= 1.6; lambda += 0.2) {
            System.out.println("lambda=" + lambda);
            for (int i = 0; i <= 12; i++) {// буфер
                for (int j = 1; j <= 3; j++) {// прибор
                    run(sourceCount, j, i, a, b, lambda);
                    double loadFactor = statistic.loadDeviceFactors().stream().mapToDouble(Double::doubleValue).sum()/j;
                    double sum = 0;
                    List<Double> timeInSystem = statistic.avgTimeInSystem();
                    List<Integer> count = statistic.countFromSource();
                    for (int k = 0; k < sourceCount; k++) {
                        sum += timeInSystem.get(k) * count.get(k);
                    }

                    int selectedBufferCost = 0;
                    if (i <= 4) {
                        selectedBufferCost = bufferCostx4;
                    } else if (i <= 6) {
                        selectedBufferCost = bufferCostx6;
                    } else if (i <= 8) {
                        selectedBufferCost = bufferCostx8;
                    }
                    else {
                        selectedBufferCost = bufferCostx8 + bufferCostx4;
                    }

                    int selectedSourceCost = 0;
                    if (lambda <= 1) {
                        selectedSourceCost = sourceCost1;
                    } else {
                        selectedSourceCost = sourceCost2;
                    } 

                    if (statistic.getCommonFailureProb() < 0.1 && loadFactor > 0.9) {
                        System.out.println(i + "\t" + j + "\t\t" + statistic.getCommonFailureProb() + "\t\t"
                                + loadFactor + "\t\t" + (sum / N) + "\t"
                                + (devCost * j + selectedBufferCost + selectedSourceCost * sourceCount));
                    }
                }

            }
        }

        if (auto) {
            run(sourceCount, deviceCount, bufferSize, a, b, lambda1);
            double prevProbability = statistic.getCommonFailureProb();
            while (prevProbability == 0) {
                N *= 2;
                run(sourceCount, deviceCount, bufferSize, a, b, lambda1);
                prevProbability = statistic.getCommonFailureProb();
            }
            N *= 2;
            run(sourceCount, deviceCount, bufferSize, a, b, lambda1);
            double curProbability = statistic.getCommonFailureProb();
            while (Math.abs(prevProbability - curProbability) / prevProbability > 0.05) {
                System.out.println("prob=" + Math.abs(prevProbability - curProbability) / prevProbability);
                N *= 2;
                run(sourceCount, deviceCount, bufferSize, a, b, lambda1);
                prevProbability = curProbability;
                curProbability = statistic.getCommonFailureProb();
            }
            System.out.println("prob=" + Math.abs(prevProbability - curProbability) / prevProbability);
            System.out.println(N);
        } else {
            N = 1000;
            run(sourceCount, deviceCount, bufferSize, a, b, lambda1);
        }
    }

    private void run(int sourceCount, int deviceCount, int bufferSize, double a, double b, double lambda) {
        this.statistic = new Statistic(sourceCount, deviceCount, bufferSize);

        List<Source> sources = IntStream.range(0, sourceCount)
                .mapToObj(i -> new Source(() -> Math.min(a, b) + Math.abs(b - a) * Math.random(), sourceCount))
                .collect(Collectors.toList());

        List<Device> devices = IntStream.range(0, deviceCount)
                .mapToObj(i -> new Device(() -> -1 / lambda * Math.log(Math.random()), deviceCount))
                .collect(Collectors.toList());

        RequestsManager handler = new RequestsManager(devices, bufferSize);

        for (int i = 0; i < N; i++) {
            Request request = Collections
                    .min(sources, Comparator.comparingDouble(src -> src.getLastRequest().getGenTime()))
                    .extractLastRequest();
            handler.addRequest(request);
            statistic.addRequest(request);
        }
        handler.clearBuffer();
    }

    public Statistic getStatistic() {
        return statistic;
    }
}
