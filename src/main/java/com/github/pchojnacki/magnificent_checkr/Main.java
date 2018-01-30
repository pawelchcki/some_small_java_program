package com.github.pchojnacki.magnificent_checkr;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.reactivex.Flowable;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.extras.rxjava2.DefaultRxHttpClient;
import org.asynchttpclient.extras.rxjava2.RxHttpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Gauge magnificentUp = Gauge.build()
            .name("magnificent_up").help("Magnificent status.").register();

    private static final Counter magnificentResponses = Counter.build()
            .name("magnificent_responses").help("Magnificent response codes.")
            .labelNames("response_code")
            .register();

    private static final Gauge magnificentLastMinuteStatus = Gauge.build()
            .name("magnificent_last_minute_average_up").help("Magnificent status averaged in one minute").register();


    public static void main(String... args) throws IOException {
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        RxHttpClient rxClient = new DefaultRxHttpClient(client);
        Request request = client.prepareGet("http://localhost:12345").build();
        HTTPServer server = new HTTPServer(9414);

        //TODO: most of this would normally be constructed using some sort of DI framework that would provide configuration and construct the needed objects

        Flowable<Long> reactor = Flowable.interval(1, TimeUnit.SECONDS);

        RollingAverage rolling = new RollingAverage(magnificentLastMinuteStatus);
        UpChecker upChecker = new UpChecker(magnificentUp);
        ResponsesTally responsesTally = new ResponsesTally(magnificentResponses);

        reactor.flatMap((ignore) -> rxClient.prepare(request).toFlowable())
                .doOnEach(responsesTally::consumeEach)
                .doOnEach(upChecker::consumeEach)
                .doOnEach(rolling::consumeEach)
                .retry()
                .blockingSubscribe();
    }
}
