package soya.framework.commons.commandline.commands;

import soya.framework.commons.commandline.Command;

import java.util.Base64;

@Command(name = "base64decode")
public class Base64DecodeCommand extends MessageCommand {
    @Override
    public String call() throws Exception {
        return new String(Base64.getDecoder().decode(message));
    }
}
