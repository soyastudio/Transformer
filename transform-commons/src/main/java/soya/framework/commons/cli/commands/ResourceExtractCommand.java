package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.Command;

@Command(name = "extract")
public class ResourceExtractCommand extends ResourceCommand {

    @Override
    public String call() throws Exception {
        return contents();
    }
}
