package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.CommandCallable;
import soya.framework.commons.cli.CommandOption;

public abstract class MessageCommand implements CommandCallable {

    @CommandOption(option = "m", longOption = "message", required = true)
    protected String message;


}
