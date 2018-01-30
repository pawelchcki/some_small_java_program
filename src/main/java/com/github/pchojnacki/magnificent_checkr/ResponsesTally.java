package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Counter;
import io.reactivex.Notification;
import org.asynchttpclient.Response;

public class ResponsesTally {

    private Counter magnificentResponses;

    public ResponsesTally(Counter magnificentResponses) {
        this.magnificentResponses = magnificentResponses;
    }

    void consumeEach(Notification<Response> responseNotification) {
        if (responseNotification.isOnNext()) {
            magnificentResponses.labels(Integer.toString(responseNotification.getValue().getStatusCode())).inc();

        } else if (responseNotification.isOnError()) {
            magnificentResponses.labels("exception").inc();
        }
    }
}
