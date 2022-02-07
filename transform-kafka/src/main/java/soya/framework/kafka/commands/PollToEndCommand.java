package soya.framework.kafka.commands;

import soya.framework.commons.cli.Command;

@Command(name = "kafka-poll-to-end", uri = "kafka://poll-to-end")
public class PollToEndCommand extends KafkaCommand {

    @Override
    public String call() throws Exception {
        return null;
    }
}
