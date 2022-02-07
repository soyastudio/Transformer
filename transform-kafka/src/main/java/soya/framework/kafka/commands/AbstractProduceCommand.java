package soya.framework.kafka.commands;

import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.cli.Resources;

public abstract class AbstractProduceCommand extends KafkaCommand {

    @CommandOption(option = "p", longOption = "produceTopic", required = true)
    protected String produceTopic;

    @CommandOption(option = "m", longOption = "message", required = true)
    protected String message;


    protected String getMessage() throws Exception{
        return Resources.get(message);
    }
}
