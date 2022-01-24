package soya.framework.transform.application.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.tool.commands.*;

import javax.annotation.PostConstruct;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class BusinessObjectCommandDelegate {

    private ExecutorService executorService;

    @Value("${workspace.home}")
    private String workspaceHome;

    private Map<String, Class<? extends Command>> classMap = new LinkedHashMap<>();
    private Map<String, Options> optionsMap = new LinkedHashMap<>();

    public BusinessObjectCommandDelegate() {
    }

    @PostConstruct
    void init() {
        System.setProperty("workspace.home", workspaceHome);

        executorService = Executors.newFixedThreadPool(5);

        Reflections reflections = new Reflections("soya.framework.tool.commands");
        Set<Class<?>> subTypes =
                reflections.getTypesAnnotatedWith(CommandExecutor.class);
        subTypes.forEach(c -> {
            if (BusinessObjectCommand.class.isAssignableFrom(c)) {
                register((Class<? extends Command>) c);
            }
        });
    }

    public String help() {
        List<String> list = new ArrayList<>(optionsMap.keySet());
        Collections.sort(list);
        CodeBuilder builder = CodeBuilder.newInstance();

        list.forEach(e -> {
            builder.appendLine(e);
            Options options = optionsMap.get(e);
            options.getOptions().forEach(o -> {
                builder.append("-" + o.getOpt(), 1).append("\t--" + o.getLongOpt()).appendLine();
            });
            builder.appendLine();
        });

        return builder.toString();
    }

    public String help(String cmd) {
        if(cmd != null && classMap.containsKey(cmd)) {
            CodeBuilder builder = CodeBuilder.newInstance();
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
            return help();
        }

    }

    public String execute(String commandline) throws Exception {

        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(commandline);
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }

        String action = null;
        List<String> args = new ArrayList<>();

        int i = 0;
        while(i < tokens.size()) {
            String token = tokens.get(i);
            if("-a".equals(token)) {
                i++;
                action = tokens.get(i);
            } else if("-r".equals(token)) {
                i ++;
            } else {
                args.add(token);
            }

            i ++;
        }

        args.add("-r");
        args.add(workspaceHome);

        Class<? extends Command> cmdType = classMap.get(action);
        Command command = create(cmdType, optionsMap.get(action), args.toArray(new String[args.size()]));

        Future<String> future = executorService.submit(command);
        while (!future.isDone()) {
            Thread.sleep(100l);
        }

        return future.get();

    }

    public String execute(Class<?> cls, String methodName, Object[] args) throws Exception {
        Method method = null;
        for (Method m : cls.getMethods()) {
            if (methodName.equals(m.getName())) {
                method = m;
                break;
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("Cannot find method: " + methodName);
        }

        Map<String, Object> params = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(PathParam.class) != null) {
                params.put(parameters[i].getAnnotation(PathParam.class).value(), args[i]);

            } else if (parameters[i].getAnnotation(QueryParam.class) != null) {
                params.put(parameters[i].getAnnotation(QueryParam.class).value(), args[i]);

            } else if (parameters[i].getAnnotation(HeaderParam.class) != null) {
                params.put(parameters[i].getAnnotation(HeaderParam.class).value(), args[i]);

            }
        }

        CommandLineTemplate clt = method.getAnnotation(CommandLineTemplate.class);
        if (clt == null) {
            throw new IllegalArgumentException("Cannot find CommandLineTemplate on method: " + methodName);
        }


        StringTokenizer tokenizer = new StringTokenizer(clt.template());
        String[] arguments = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.startsWith("-")) {
                arguments[i] = token;
            } else if (token.startsWith("{{") && token.endsWith("}}")) {
                String p = token.substring(2, token.length() - 2);
                if (params.containsKey(p)) {
                    arguments[i] = params.get(p).toString();

                } else if (System.getProperty(p) != null) {
                    arguments[i] = System.getProperty(p);

                }
            } else {
                arguments[i] = token;
            }
            i++;
        }

        Class<? extends Command> cmdType = classMap.get(clt.command());
        Command command = create(cmdType, optionsMap.get(clt.command()), arguments);

        Future<String> future = executorService.submit(command);
        while (!future.isDone()) {
            Thread.sleep(100l);
        }

        return future.get();
    }


    private Command create(Class<? extends Command> cls, Options options, String[] args) throws Exception {
        Command command = cls.newInstance();
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
    }


    private void register(Class<? extends Command> cls) {
        String name = cls.getAnnotation(CommandExecutor.class).name();
        classMap.put(name, cls);

        Options options = new Options();
/*

        options.addOption(Option.builder("a")
                .longOpt("action")
                .hasArg(true)
                .required(true)
                .desc("")
                .build());
*/

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
