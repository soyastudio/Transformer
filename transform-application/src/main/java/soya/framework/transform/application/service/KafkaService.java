package soya.framework.transform.application.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import soya.framework.commons.cli.CommandLineService;
import soya.framework.commons.cli.CommandLines;
import soya.framework.commons.cli.CommandMethod;
import soya.framework.commons.util.PropertiesUtils;
import soya.framework.kafka.KafkaCommands;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class KafkaService implements CommandLineService {

    private static final CommandLineParser PARSER = new DefaultParser();
    private Map<String, CommandMethod> commands = new LinkedHashMap<>();
    private Properties properties;


    @PostConstruct
    void init() throws Exception {
        Method[] methods = KafkaCommands.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getAnnotation(CommandLines.Command.class) != null) {
                CommandMethod cm = new CommandMethod(method);
                commands.put(cm.getCommand(), cm);
            }
        }

        Properties kafkaConfig = new Properties();
        InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("kafka-config.properties");
        kafkaConfig.load(inStream);
        KafkaCommands.configure(kafkaConfig);

        properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("repository.properties");
        properties.load(inputStream);
        PropertiesUtils.compile(properties);
    }

    @Override
    public String help() {
        return CommandLines.GSON.toJson(commands.values());
    }

    @Override
    public String execute(String cmd, String msg) throws Exception {

        Map<String, String> values = new HashMap<>();
        String[] arr = cmd.split("-");
        for (String s : arr) {
            if (!s.isEmpty()) {
                String o = s;
                String v = null;
                if (s.contains(" ")) {
                    int index = s.indexOf(" ");
                    o = s.substring(0, index);
                    v = s.substring(index).trim();
                }

                values.put(o, v);
            }
        }

        String bod = values.get("b");
        String testCass = values.get("u");

        File cmmDir = new File(properties.getProperty("workspace.cmm.dir"));

        File boDir = new File(properties.getProperty("workspace.bo.dir") + "/" + bod);
        if (!boDir.exists()) {
            throw new IllegalArgumentException("Bod does not exist: " + values.get("b"));
        }
        File testDir = new File(boDir, "test");
        File cmdFile = new File(testDir, "cmd.json");

        JsonObject testCases = JsonParser.parseReader(new FileReader(cmdFile)).getAsJsonObject();
        String command = testCases.get(testCass).getAsString();

        String[] array = command.split("-");
        for (String s : array) {
            if (!s.isEmpty()) {
                String o = s;
                String v = null;
                if (s.contains(" ")) {
                    int index = s.indexOf(" ");
                    o = s.substring(0, index);
                    v = s.substring(index).trim();
                }

                values.put(o, v);
            }
        }

        String action = values.get("a");
        CommandMethod commandMethod = commands.get(action);
        Method method = commandMethod.getMethod();
        Options options = commandMethod.getOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("-a");
        arguments.add(action);

        options.getOptions().forEach(e -> {
            String opt = e.getOpt();
            String value = values.get(opt);

            if (value != null) {
                if ("m".equals(opt)) {
                    File file = new File(testDir, value);
                    if (!file.exists()) {
                        throw new IllegalArgumentException("File does not exist: " + file.getPath());
                    }
                    value = file.getPath();
                }

                if("s".equals(opt)) {
                    File file = new File(cmmDir, value);
                    if (!file.exists()) {
                        throw new IllegalArgumentException("File does not exist: " + file.getPath());
                    }
                    value = file.getPath();
                }

                arguments.add("-" + opt);
                arguments.add(value);

            } else if (e.isRequired()) {


            }

        });

        CommandLine commandLine = PARSER.parse(options, arguments.toArray(new String[arguments.size()]));


        return (String) method.invoke(null, new Object[]{commandLine});

    }
}