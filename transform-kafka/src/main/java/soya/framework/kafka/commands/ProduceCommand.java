package soya.framework.kafka.commands;

import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import soya.framework.commons.commandline.Command;
import soya.framework.commons.commandline.CommandOption;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Command(name = "produce")
public class ProduceCommand extends KafkaCommand {

    @CommandOption(option = "p", longOption = "topic", required = true)
    private String topicName;

    @CommandOption(option = "m", longOption = "message", required = true)
    private String message;

    @Override
    public String call() throws Exception {

        byte[] msg = message.getBytes(StandardCharsets.UTF_8);
        long timeout = 5000L;

        String key = UUID.randomUUID().toString();

        String hs = null;
        Headers headers = headers(hs);

        ProducerRecord<String, byte[]> record = createProducerRecord(topicName, msg, headers, key);
        RecordMetadata metadata = send(createKafkaProducer(), record, timeout);
        /*if (metadata != null) {
            return prettyPrintJson(record, metadata);
        }*/

        JsonObject result = new JsonObject();

        return GSON.toJson(result);
    }
}
