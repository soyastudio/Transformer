package soya.framework.commons.commandline.commands;

import soya.framework.commons.commandline.CommandCallable;
import soya.framework.commons.commandline.CommandOption;

public abstract class MessageCommand implements CommandCallable {

    @CommandOption(option = "m", longOption = "message", required = true)
    protected String message;


}
