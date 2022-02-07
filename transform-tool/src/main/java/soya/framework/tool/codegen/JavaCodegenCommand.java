package soya.framework.tool.codegen;

import soya.framework.commons.cli.CommandCallable;
import soya.framework.commons.cli.CommandOption;

public abstract class JavaCodegenCommand implements CommandCallable<String> {

    @CommandOption(option = "p", longOption = "pkg", required = true)
    protected String packageName;

    @CommandOption(option = "c", longOption = "cls", required = true)
    protected String className = "MyClass";

    @CommandOption(option = "i", longOption = "imp")
    protected String imports;

}
