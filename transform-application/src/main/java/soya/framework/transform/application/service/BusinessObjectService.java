package soya.framework.transform.application.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import soya.framework.commons.cli.CommandLineService;
import soya.framework.commons.cli.CommandLines;
import soya.framework.commons.cli.CommandMethod;
import soya.framework.commons.util.PropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class BusinessObjectService<T> implements CommandLineService {

    public static final CommandLineParser PARSER = new DefaultParser();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Class<T> commandLineClass;
    private final Properties properties;

    protected Map<String, CommandMethod> commands = new LinkedHashMap<>();
    protected File homeDir;
    protected File cmmBaseDir;
    protected File boBaseDir;

    public BusinessObjectService() {
        this.commandLineClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.properties = new Properties();
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("repository.properties");
            properties.load(inputStream);
            PropertiesUtils.compile(properties);

            homeDir = new File(properties.getProperty("workspace.home"));
            if (!homeDir.exists()) {
                throw new RuntimeException("Home dir does not exist: " + homeDir.getPath());
            }

            cmmBaseDir = new File(properties.getProperty("workspace.cmm.dir"));
            if (!cmmBaseDir.exists()) {
                throw new RuntimeException("CMM dir does not exist: " + cmmBaseDir.getPath());
            }

            boBaseDir = new File(properties.getProperty("workspace.bo.dir"));
            if (!boBaseDir.exists()) {
                throw new RuntimeException("BO dir does not exist: " + boBaseDir.getPath());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

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
        return GSON.toJson(commands.values());
    }

    @Override
    public String execute(String cmd, String msg) throws Exception {
        Map<String, String> values = parse(cmd);
        CommandMethod commandMethod = getCommandMethod(values);

        Method method = commandMethod.getMethod();
        String[] args = parse(commandMethod.getOptions(), values, msg, properties);
        CommandLine commandLine = PARSER.parse(commandMethod.getOptions(), args);

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

    protected CommandMethod getCommandMethod(Map<String, String> values) {
        return commands.get(values.get("a"));
    }

    protected abstract String[] parse(Options options, Map<String, String> values, String msg, Properties properties);
}
