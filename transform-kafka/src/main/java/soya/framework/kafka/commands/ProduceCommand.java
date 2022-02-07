package soya.framework.kafka.commands;

import org.apache.kafka.clients.producer.RecordMetadata;
import soya.framework.commons.cli.Command;
import soya.framework.kafka.KafkaUtils;

import java.util.UUID;

@Command(name = "kafka-produce", uri = "kafka://produce")
public class ProduceCommand extends AbstractProduceCommand {

    @Override
    public String call() throws Exception {
        RecordMetadata metadata = KafkaUtils.produce(produceTopic, 0, getMessage(), UUID.randomUUID().toString(), null, timeout, environment);
        return metadata.toString();
    }
}
