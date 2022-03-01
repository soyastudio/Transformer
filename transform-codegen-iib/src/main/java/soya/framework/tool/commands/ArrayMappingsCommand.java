package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;

@Command(name = "bod-arrays", uri = "bod://arrays")
public class ArrayMappingsCommand extends ConstructCommand {

    @Override
    protected String render() {
        return GSON.toJson(arrayMap.values());
    }
}
