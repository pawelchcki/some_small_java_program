### Running

```bash
./gradlew run
```


### Accessing Magnificent status

Magnificent app metrics are exported using Prometheus text format on url: [http://localhost:9414]


Example output:

```
# HELP magnificent_last_minute_average_up Magnificent status averaged in one minute
# TYPE magnificent_last_minute_average_up gauge
magnificent_last_minute_average_up 0.8166666666666667
# HELP magnificent_responses Magnificent response codes.
# TYPE magnificent_responses counter
magnificent_responses{response_code="200",} 75.0
magnificent_responses{response_code="500",} 19.0
# HELP magnificent_up Magnificent status.
# TYPE magnificent_up gauge
magnificent_up 1.0
```

Note: `magnificent_last_minute_average_up` metric is added as a requirement of this task. Normally this would be handled by prometheus by scraping magnificent_up periodically.