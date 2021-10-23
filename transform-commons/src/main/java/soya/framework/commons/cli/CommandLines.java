package soya.framework.commons.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.cli.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandLines {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final CommandLineParser parser = new DefaultParser();

    private static Map<String, Configuration> configurations = new ConcurrentHashMap();

    public static Configuration configure(Class<?> cl, Properties properties) {
        configurations.put(cl.getName(), new Configuration(cl, properties));
        return configurations.get(cl.getName());
    }

    public static Properties getProperties(Class<?> cl) {
        Properties properties = new Properties();
        if (configurations.containsKey(cl.getName())) {
            Properties env = configurations.get(cl.getName()).env;
            Enumeration<?> enumeration = env.propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                properties.setProperty(key, env.getProperty(key));
            }

        }

        return properties;
    }

    public static String execute(String cmd, Class<?> cl, Evaluator evaluator) throws Exception {

        Configuration configuration = getConfiguration(cl);
        Method method = configuration.getCommandMethod(cmd);
        if (method == null) {
            return configuration.help();
        }

        if (method.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Not command method: " + method.getName());
        }

        CommandLine commandLine = commandLineBuilder(method)
                .setProperties(configuration.env)
                .setEvaluator(evaluator)
                .create(cmd);

        return (String) method.invoke(null, new Object[]{commandLine});
    }

    public static String execute(String[] args, Class<?> cl, Evaluator evaluator) throws Exception {
        Configuration configuration = getConfiguration(cl);
        Method method = configuration.getCommandMethod(args);
        if (method == null) {
            return configuration.help();
        }

        if (method.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Not command method: " + method.getName());
        }

        CommandLine commandLine = commandLineBuilder(method)
                .setProperties(configuration.env)
                .setEvaluator(evaluator)
                .create(args);

        return (String) method.invoke(null, new Object[]{commandLine});
    }

    public static Builder commandLineBuilder(Method method) {
        return new Builder(method);
    }

    private static Configuration getConfiguration(Class<?> cl) {
        if (!configurations.containsKey(cl.getName())) {
            configurations.put(cl.getName(), new Configuration(cl));
        }

        return configurations.get(cl.getName());
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {
        String name() default "";

        String desc() default "";

        Opt[] options() default {};

        String[] cases() default {};
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Opt {
        String option();

        String longOption() default "";

        boolean hasArg() default true;

        boolean required() default false;

        String defaultValue() default "";

        String desc() default "";
    }

    public static class Configuration {

        private Options options = new Options();
        private Map<String, Method> commands = new LinkedHashMap<>();

        private Properties env = new Properties();

        protected Configuration(Class<?> clazz) {
            this(clazz, null);
        }

        protected Configuration(Class<?> clazz, Properties properties) {
            if (properties != null) {
                Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    env.setProperty(key, properties.getProperty(key));
                }
            }

            options.addOption(Option.builder("a")
                    .longOpt("action")
                    .hasArg(true)
                    .desc("Task to execute.")
                    .required(false)
                    .build());

            Class parent = clazz;
            while (!parent.equals(Object.class)) {
                Method[] methods = parent.getDeclaredMethods();
                for (Method method : methods) {
                    if (!commands.containsKey(method.getName()) && method.getAnnotation(Command.class) != null) {
                        Command command = method.getAnnotation(Command.class);
                        if (command.name().isEmpty()) {
                            commands.put(method.getName(), method);
                        } else {
                            commands.put(command.name(), method);
                        }

                        for (Opt opt : command.options()) {
                            if (!options.hasOption(opt.option())) {
                                options.addOption(Option
                                        .builder(opt.option())
                                        .longOpt(opt.longOption())
                                        .hasArg(opt.hasArg())
                                        .required(false)
                                        .desc(opt.desc())
                                        .build());
                            }
                        }
                    }
                }

                parent = parent.getSuperclass();

            }
        }

        public Method getCommandMethod(String cl) {
            List<String> list = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(cl);
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }

            return getCommandMethod(list.toArray(new String[list.size()]));
        }

        public Method getCommandMethod(String[] args) {
            try {
                CommandLine commandLine = parser.parse(options, args);
                return commands.get(commandLine.getOptionValue("a"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String help() {
            JsonArray array = new JsonArray();
            commands.entrySet().forEach(e -> {
                Method method = e.getValue();
                array.add(commandDesc(method));
            });

            return GSON.toJson(array);
        }

        protected static JsonObject commandDesc(Method method) {
            Command annotation = method.getAnnotation(Command.class);

            JsonObject obj = new JsonObject();
            obj.addProperty("command", method.getName());
            obj.addProperty("description", annotation.desc());

            JsonArray opts = new JsonArray();
            Opt[] options = annotation.options();
            for (Opt opt : options) {
                JsonObject o = new JsonObject();
                o.addProperty("option", opt.option());
                o.addProperty("required", opt.required());
                o.addProperty("defaultValue", opt.defaultValue());
                o.addProperty("description", opt.desc());
                opts.add(o);
            }
            obj.add("options", opts);

            obj.add("example", GSON.toJsonTree(annotation.cases()));

            return obj;
        }
    }

    public interface Evaluator {
        String evaluate(String v, Options options, Properties properties);
    }

    public static class Builder {
        private Options options;
        private Evaluator evaluator;
        private Properties properties = new Properties();

        private Method method;
        private Map<String, String> values = new HashMap<>();

        private Builder(Method method) {
            this.method = method;
            if (method.getAnnotation(Command.class) == null) {
                throw new IllegalArgumentException("Method is not annotated as 'Command': " + method.getName());
            }

            options = new Options();
            Command command = method.getAnnotation(Command.class);
            options.addOption(Option.builder("a")
                    .longOpt("action")
                    .hasArg(true)
                    .desc("Task to execute.")
                    .required(false)
                    .build());
            if (command.name().isEmpty()) {
                values.put("a", method.getName());

            } else {
                values.put("a", command.name());

            }

            for (Opt opt : command.options()) {
                if (!options.hasOption(opt.option())) {
                    options.addOption(Option
                            .builder(opt.option())
                            .longOpt(opt.longOption())
                            .hasArg(opt.hasArg())
                            .required(opt.required())
                            .desc(opt.desc())
                            .build());
                    values.put(opt.option(), null);
                }
            }
        }

        public Builder addOption(Option option) {
            options.addOption(option);
            return this;
        }

        public Builder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder setEvaluator(Evaluator evaluator) {
            if (evaluator != null) {
                this.evaluator = evaluator;
            }
            return this;
        }

        public Builder setValue(String opt, String value) {
            String v = evaluator == null ? value : evaluator.evaluate(value, options, properties);
            return this;
        }

        public CommandLine create() {
            List<String> list = new ArrayList<>();
            options.getOptions().forEach(e -> {
                if (e.hasArg() && values.get(e.getOpt()) == null) {
                    throw new IllegalStateException("-" + e.getOpt() + " need argument");
                }

                list.add("-" + e.getOpt());
                list.add(values.get(e.getOpt()));
            });

            try {
                return parser.parse(options, list.toArray(new String[list.size()]));

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public CommandLine create(String cl) {
            List<String> list = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(cl);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (evaluator != null) {
                    token = evaluator.evaluate(token, options, properties);
                }

                list.add(token);
            }

            try {
                return parser.parse(options, list.toArray(new String[list.size()]));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public CommandLine create(String[] args) {
            try {
                if (evaluator == null) {
                    return parser.parse(options, args);

                } else {
                    String[] arr = new String[args.length];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = evaluator.evaluate(args[i], options, properties);
                    }
                    return parser.parse(options, arr);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
