package soya.framework.tool.mustache;

import soya.framework.commons.cli.CommandCallable;
import soya.framework.commons.cli.CommandOption;

public abstract class MustacheCommand implements CommandCallable {

    @CommandOption(option = "t", longOption = "template", required = true)
    protected String template;

}
