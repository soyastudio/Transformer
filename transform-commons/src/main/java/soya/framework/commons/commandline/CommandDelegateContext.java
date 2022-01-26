package soya.framework.commons.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

public class CommandDelegateContext {

    private Map<String, Class<? extends CommandCallable>> classMap = new LinkedHashMap<>();
    private Map<String, Options> optionsMap = new LinkedHashMap<>();
    private Properties properties = new Properties();
    private Option commandOption;

    CommandDelegateContext(Class<? extends CommandCallable>[] classes, Properties properties, Option commandOption) {
        for(Class<? extends CommandCallable> c: classes) {
            register((Class<? extends CommandCallable>) c);
        }

        if (properties != null) {
            this.properties.putAll(properties);
        }

        this.commandOption = commandOption;
    }

    public Option commandOption() {
        return commandOption;
    }

    public String property(String key) {
        return properties.getProperty(key);
    }

    public String[] commands() {
        List<String> list = new ArrayList<>(classMap.keySet());
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    public Class<? extends CommandCallable> getCommandType(String cmd) {
        return classMap.get(cmd);
    }

    public Options options(String cmd) {
        return optionsMap.get(cmd);
    }

    public CommandCallable create(String cmd, String[] args) {
        if(!classMap.containsKey(cmd)) {
            throw new IllegalArgumentException("Command not defined: " + cmd);
        }
        Class<? extends CommandCallable> cls = classMap.get(cmd);
        Options options = optionsMap.get(cmd);

        try {
            CommandCallable command = cls.newInstance();
            CommandLine commandLine = new DefaultParser().parse(options, args);

            Class<?> superClass = cls;
            while (!Object.class.equals(superClass)) {
                Field[] fields = superClass.getDeclaredFields();
                for (Field field : fields) {
                    CommandOption commandOption = field.getAnnotation(CommandOption.class);
                    if (commandOption != null && commandLine.hasOption(commandOption.option())) {
                        String value = commandLine.getOptionValue(commandOption.option());
                        field.setAccessible(true);
                        field.set(command, value);
                    }
                }

                superClass = superClass.getSuperclass();
            }

            return command;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<String> list = new ArrayList<>(classMap.keySet());
        Collections.sort(list);

        list.forEach(e -> {
            builder.append(e).append("\n");
            Options options = optionsMap.get(e);
            options.getOptions().forEach(o -> {
                builder.append("\t-" + o.getOpt()).append("\t--" + o.getLongOpt()).append("\n");
            });
            builder.append("\n");
        });
        return builder.toString();
    }

    public String toString(String cmd) {
        if(cmd != null && classMap.containsKey(cmd)) {
            StringBuilder builder = new StringBuilder();
            builder.append("-a ").append(cmd);
            Options options = optionsMap.get(cmd);
            options.getOptions().forEach(e -> {
                if(!e.getOpt().equals("r")) {
                    builder.append(" -").append(e.getOpt());
                    if(e.hasArg()) {
                        builder.append(" [");
                        if(e.isRequired()) {
                            builder.append("required: ");
                        } else {
                            builder.append("optional: ");
                        }

                        builder.append(e.getDescription());

                        builder.append("]");
                    }
                }
            });


            return builder.toString();
        } else {
            return toString();
        }
    }

    private void register(Class<? extends CommandCallable> cls) {
        String name = cls.getAnnotation(Command.class).name();
        classMap.put(name, cls);

        Options options = new Options();

        Class superClass = cls;
        while (!Object.class.equals(superClass)) {
            Field[] fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                CommandOption commandOption = field.getAnnotation(CommandOption.class);
                if (commandOption != null && options.getOption(commandOption.option()) == null) {
                    options.addOption(Option.builder(commandOption.option())
                            .longOpt(commandOption.longOption())
                            .hasArg(commandOption.hasArg())
                            .required(commandOption.required())
                            .desc(commandOption.desc())
                            .build());
                }
            }
            superClass = superClass.getSuperclass();
        }

        optionsMap.put(name, options);

    }
}
