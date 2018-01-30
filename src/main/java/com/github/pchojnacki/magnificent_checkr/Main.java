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
    private static final Gauge magnificentUp = Gauge.build()
            .name("magnificent_up").help("Magnificent status.").register();
    private static final Gauge magnificentBlah = Gauge.build()
            .name("magnificent_blah").help("Magnificent status.").register();

    private static final Gauge magnificentLastMinuteStatus = Gauge.build()
            .name("magnificent_last_minute_average_up").help("Magnificent status averaged in one minute").register();

    private static final Counter magnificentResponses = Counter.build()
            .name("magnificent_responses").help("Magnificent response codes.")
            .labelNames("response_code")
            .register();

    public static void main(String... args) throws IOException {
        AsyncHttpClient client = new DefaultAsyncHttpClient();

        RxHttpClient rxClient = new DefaultRxHttpClient(client);
        Request request = client.prepareGet("http://localhost:12345").build();
        HTTPServer server = new HTTPServer(9414);

        Flowable<Long> reactor = Flowable.interval(1, TimeUnit.SECONDS);

        ArrayDeque<Double> rollingAverage = new ArrayDeque<>();


        reactor.flatMap((ignore) -> rxClient.prepare(request).toFlowable())
                .doOnNext(response -> {
                    magnificentResponses.labels(Integer.toString(response.getStatusCode())).inc();

                    if (response.getStatusCode() == 200) {
                        rollingAverage.add(1.0);
                        magnificentUp.set(1);
                    } else {
                        rollingAverage.add(0.0);
                        magnificentUp.set(0);
                    }
                }).doOnError(err -> {
            magnificentUp.set(0);
            magnificentResponses.labels("exception").inc();
            rollingAverage.add(0.0);
        })
                .doOnEach(ignore -> {
                            if (rollingAverage.size() > 60) {
                                rollingAverage.removeLast();
                            }

                            magnificentBlah.set(rollingAverage.size());

                            double average = rollingAverage.stream().mapToDouble(x -> x).average().orElse(0.0);
                            magnificentLastMinuteStatus.set(average);
                        }
                )
                .retry()
                .blockingSubscribe();

        client.close();
        server.stop();
    }
}
