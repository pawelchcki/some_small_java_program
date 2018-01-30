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
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

public class Main {
//    private static final Gauge magnificentUp = Gauge.build()
//            .name("magnificent_up").help("Magnificent status.").register();


    public static void main(String... args) throws IOException {
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        RxHttpClient rxClient = new DefaultRxHttpClient(client);
        Request request = client.prepareGet("http://localhost:12345").build();
        HTTPServer server = new HTTPServer(9414);

        Flowable<Long> reactor = Flowable.interval(1, TimeUnit.SECONDS);

        RollingAverage rolling = new RollingAverage();
        UpChecker upChecker = new UpChecker();
        ResponsesTally responsesTally = new ResponsesTally();


        reactor.flatMap((ignore) -> rxClient.prepare(request).toFlowable())
                .doOnEach(responsesTally::consumeEach)
                .doOnEach(upChecker::consumeEach)
                .doOnEach(rolling::consumeEach)
                .retry()
                .blockingSubscribe();
    }
}
