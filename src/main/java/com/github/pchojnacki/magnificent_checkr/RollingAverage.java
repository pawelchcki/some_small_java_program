package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Gauge;
import io.reactivex.Notification;
import org.asynchttpclient.Response;

import java.util.ArrayDeque;

public class RollingAverage {
    private final ArrayDeque<Double> rollingAverage;
    private Gauge magnificentLastMinuteStatus;

    public RollingAverage(Gauge magnificentLastMinuteStatus) {
        this.magnificentLastMinuteStatus = magnificentLastMinuteStatus;
        rollingAverage = new ArrayDeque<>();
    }

    void consumeEach(Notification<Response> responseNotification) {
        if (responseNotification.isOnNext()){
            if (responseNotification.getValue().getStatusCode() == 200) {
                rollingAverage.add(1.0);
            } else {
                rollingAverage.add(0.0);
            }
        } else if (responseNotification.isOnError()){
            rollingAverage.add(0.0);
        }

        if (rollingAverage.size() > 60) {
            rollingAverage.removeLast();
        }

        double average = rollingAverage.stream().mapToDouble(x -> x).average().orElse(0.0);
        magnificentLastMinuteStatus.set(average);
    }
}
