package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Gauge;
import io.reactivex.Notification;
import org.asynchttpclient.Response;

public class UpChecker {

    private Gauge magnificentUp;

    public UpChecker(Gauge magnificentUp) {
        this.magnificentUp = magnificentUp;
    }

    void consumeEach(Notification<Response> responseNotification) {
        if (responseNotification.isOnNext()) {
            if (responseNotification.getValue().getStatusCode() == 200) {
                magnificentUp.set(1);
            } else {
                magnificentUp.set(0);
            }
        } else if (responseNotification.isOnError()) {
            magnificentUp.set(0);
        }
    }
}
