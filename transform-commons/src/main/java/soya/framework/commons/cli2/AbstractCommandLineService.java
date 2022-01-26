package soya.framework.commons.cli2;

import org.apache.commons.cli.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractCommandLineService<T> implements CommandLineService {

    public static final CommandLineParser parser = new DefaultParser();
    protected Class<T> commandLineClass;

    protected Properties properties;
    protected Map<String, CommandMethod> commands = new LinkedHashMap<>();

    @PostConstruct
    protected void init() throws Exception {
        this.commandLineClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.properties = configure();

        Method[] methods = commandLineClass.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getAnnotation(CommandLines.Command.class) != null) {
                CommandMethod commandMethod = new CommandMethod(m);
                commands.put(commandMethod.getCommand(), commandMethod);
            }
        }
    }

    @Override
    public String help() {
        return CommandLines.GSON.toJson(commands.values());
    }

    @Override
    public String execute(String cmd, String msg) throws Exception {

        Map<String, String> values = parse(cmd);

        String action = values.get("a");
        if (!commands.containsKey(action)) {
            throw new IllegalArgumentException("Command Method does not exist: " + action + " for " + commandLineClass.getName());
        }
        Method method = commands.get(action).getMethod();
        Options options = getOptions(action);
        String[] args = compile(options, values, msg, properties);

        CommandLine commandLine = parser.parse(options, args);
        return (String) method.invoke(null, new Object[]{commandLine});
    }

    protected Map<String, String> parse(String cmd) {
        String line = cmd.trim();
        if (!line.startsWith("-")) {
            throw new IllegalArgumentException("Illegal command line: \"" + cmd + "\"");
        }

        line = line.substring(1);
        String[] arr = line.split("-");
        Map<String, String> values = new LinkedHashMap<>();
        for (String token : arr) {
            String key = token;
            String value = null;
            int index = token.indexOf(" ");
            if (index > 0) {
                key = token.substring(0, index);
                value = token.substring(index).trim();
            }

            values.put(key, value);
        }

        return values;
    }

    protected Options getOptions(String cmd) {
        if (commands.containsKey(cmd)) {
            return commands.get(cmd).getOptions();
        }

        throw new IllegalArgumentException("Command does not exist: " + cmd);
    }

    protected abstract Properties configure() throws IOException;

    protected abstract String[] compile(Options options, Map<String, String> values, String msg, Properties properties) throws ParseException;




}
