package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.Command;

@Command(name = "echo")
public class EchoCommand extends MessageCommand{
    @Override
    public String call() throws Exception {
        System.out.println("================ " + message);
        return message;
    }
}
