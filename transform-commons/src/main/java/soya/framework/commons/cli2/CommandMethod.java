package soya.framework.commons.cli2;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.lang.reflect.Method;

public class CommandMethod {

    private final transient Method method;
    private final transient Options options;
    private final String command;

    public CommandMethod(Method method) {
        this.method = method;
        this.options = new Options();
        CommandLines.Command cmd = method.getAnnotation(CommandLines.Command.class);
        CommandLines.Opt[] opts = cmd.options();
        for (CommandLines.Opt opt : opts) {
            options.addOption(Option.builder(opt.option())
                    .required(opt.required())
                    .hasArg(opt.hasArg())
                    .desc(opt.desc())
                    .build());
        }

        this.command = cmd.name().isEmpty() ? method.getName() : cmd.name();

    }

    public String getCommand() {
        return command;
    }

    public Method getMethod() {
        return method;
    }

    public Options getOptions() {
        return options;
    }
}
