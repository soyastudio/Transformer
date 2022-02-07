package soya.framework.kafka.commands;

import soya.framework.commons.cli.Command;
import soya.framework.kafka.KafkaUtils;

@Command(name = "kafka-metrics", uri = "kafka://metrics")
public class MetricsCommand extends KafkaCommand {

    @Override
    public String call() throws Exception {
        return GSON.toJson(KafkaUtils.metrics(environment));
    }
}
