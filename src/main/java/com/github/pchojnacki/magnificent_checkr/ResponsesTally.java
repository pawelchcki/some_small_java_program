package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Counter;
import io.reactivex.Notification;
import org.asynchttpclient.Response;

public class ResponsesTally {
    private static final Counter magnificentResponses = Counter.build()
            .name("magnificent_responses").help("Magnificent response codes.")
            .labelNames("response_code")
            .register();

    public ResponsesTally(){

    }

    void consumeEach(Notification<Response> responseNotification) {
        if (responseNotification.isOnNext()){
            magnificentResponses.labels(Integer.toString(responseNotification.getValue().getStatusCode())).inc();

        } else if (responseNotification.isOnError()){
            magnificentResponses.labels("exception").inc();
        }

    }
}
