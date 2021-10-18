package soya.framework.transform.commands;

import com.google.gson.*;
import org.apache.commons.cli.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class CommandLines {

    protected static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    protected static CommandLineParser parser = new DefaultParser();
    protected static Options options = new Options();
    protected static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected static Class<? extends CommandLines> _clazz = CommandLines.class;
    protected static Map<String, Method> commands = new LinkedHashMap<>();

    public static CommandLine build(String cl) throws ParseException {
        List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(cl);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            list.add(token);
        }

        return build(list.toArray(new String[list.size()]));
    }

    public static CommandLine build(String[] args) throws ParseException {
        return parser.parse(options, args);
    }

    public static CommandLine build(JsonObject jsonObject) throws ParseException {
        List<String> list = new ArrayList<>();
        jsonObject.entrySet().forEach(e -> {
            String key = e.getKey();
            if (options.hasOption(e.getKey())) {
                list.add("-" + key);
                if (options.getOption(key).hasArg()) {
                    list.add(e.getValue().getAsString());
                }
            }
        });
        return build(list.toArray(new String[list.size()]));
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

    public static String execute(Map<String, String> cmd) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(GSON.toJson(cmd)).getAsJsonObject();
        return execute(build(jsonObject));
    }

    public static String execute(CommandLine commandLine) throws Exception {
        String action = "";
        if (commandLine.hasOption("a")) {
            action = commandLine.getOptionValue("a");
        }

        Method method = _clazz.getMethod(action, new Class[]{CommandLine.class});
        if (method.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Not command method: " + method.getName());
        }
        Command command = method.getAnnotation(Command.class);
        Opt[] opts = command.options();
        for (Opt opt : opts) {
            if (opt.required() && commandLine.getOptionValue(opt.option()) == null) {
                throw new IllegalArgumentException("Option '" + opt.option() + "' is required.");
            }
        }


        StringBuilder builder = new StringBuilder();

        builder.append("################################## ")
                .append(DATE_FORMAT.format(new Date()))
                .append(" ##################################")
                .append("\n").append("\n");

        for (Opt opt : opts) {
            String v = "";
            if (commandLine.getOptionValue(opt.option()) != null) {
                v = commandLine.getOptionValue(opt.option());
            } else {
                v = "[optional: " + opt.desc() + "]";
            }
            builder.append("\t").append("-").append(opt.option()).append(" ").append(v).append("\n");

            if (opt.required() && commandLine.getOptionValue(opt.option()) == null) {
                throw new IllegalArgumentException("Option '" + opt.option() + "' is required.");
            }
        }

        builder.append("\n")
                .append("##############################################################################################")
                .append("\n\n");

        System.out.println(builder.toString());



        return (String) method.invoke(null, new Object[]{commandLine});
    }

    @Command(
            desc = "Help",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "help",
                            desc = "Command name."),
                    @Opt(option = "q",
                            desc = "Query for help topic")
            },
            cases = {"-a help"}
    )
    public static String help(CommandLine cmd) {
        String query = cmd.getOptionValue("q");

        if (query == null) {
            JsonArray array = new JsonArray();
            commands.entrySet().forEach(e -> {
                Method method = e.getValue();
                array.add(commandDesc(method));
            });

            return GSON.toJson(array);

        } else if (query.length() == 1 && options.hasOption(query)) {
            Option option = options.getOption(query);
            JsonObject jo = new JsonObject();
            jo.addProperty("option", option.getOpt());
            jo.addProperty("longOption", option.getLongOpt());
            jo.addProperty("hasArg", option.hasArg());
            jo.addProperty("description", option.getDescription());
            return GSON.toJson(jo);

        } else if (query.length() > 1 && commands.containsKey(query)) {
            return GSON.toJson(commandDesc(commands.get(query)));

        } else {
            return "Can not find help topic.";
        }
    }

    protected static void init(Class<? extends CommandLines> clazz) {
        _clazz = clazz;

        Class parent = clazz;

        while (!parent.equals(Object.class)) {
            Method[] methods = parent.getDeclaredMethods();
            for(Method method: methods) {
                if(!commands.containsKey(method.getName()) && method.getAnnotation(Command.class) != null) {
                    commands.put(method.getName(), method);
                }
            }

            parent = parent.getSuperclass();

        }

    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Command {
        String desc() default "";

        Opt[] options() default {};

        String[] cases() default {};
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Opt {
        String option();

        boolean required() default false;

        String defaultValue() default "";

        String desc() default "";
    }
}
