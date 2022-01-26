package soya.framework.tool.commands;

import soya.framework.commons.commandline.Command;
import soya.framework.commons.commandline.CommandOption;

@Command(name = "version")
public class VersionCommand extends BusinessObjectCommand {

    @CommandOption(option = "v", longOption = "version", required = true)
    private String version;

    @Override
    protected String execute() throws Exception {
        System.out.println("=============== versioning...");
        return null;
    }
}