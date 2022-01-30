package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.Command;

@Command(name = "aes-encrypt")
public class AESEncryptCommand extends AESCommand {
    @Override
    public String call() throws Exception {
        return this.encrypt(message, secret);
    }
}
