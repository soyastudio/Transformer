package soya.framework.commons.commandline.commands;

import soya.framework.commons.commandline.Command;

import java.nio.charset.Charset;
import java.util.Base64;

@Command(name = "base64encode")
public class Base64EncodeCommand extends MessageCommand {
    @Override
    public String call() throws Exception {
        return Base64.getEncoder().encodeToString(message.getBytes(Charset.defaultCharset()));
    }
}
