package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Gauge;
import io.reactivex.Notification;
import org.asynchttpclient.Response;

public class UpChecker {
    private static final Gauge magnificentUp = Gauge.build()
            .name("magnificent_up").help("Magnificent status.").register();

    void consumeEach(Notification<Response> responseNotification) {
        if (responseNotification.isOnNext()){
            if (responseNotification.getValue().getStatusCode() == 200) {
                magnificentUp.set(1);
            } else {
                magnificentUp.set(0);
            }
        } else if (responseNotification.isOnError()){
            magnificentUp.set(0);
        }
    }
}
