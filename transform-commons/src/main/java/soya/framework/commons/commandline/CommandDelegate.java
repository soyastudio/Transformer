package soya.framework.commons.commandline;

import org.apache.commons.cli.Option;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CommandDelegate {
    private static ExecutorService DEFAULT_EXECUTOR;

    private CommandDelegateContext context;
    private CommandFactory commandFactory;
    private ExecutorService executorService;

    private CommandDelegate(ExecutorService executorService, CommandDelegateContext context, CommandFactory commandFactory) {
        this.executorService = executorService;
        this.context = context;
        this.commandFactory = commandFactory;
    }

    public CommandDelegateContext context() {
        return context;
    }

    public String execute(String commandline) throws Exception {
        CommandCallable command = commandFactory.create(commandline, context);
        Future<String> future = executorService.submit(command);
        while (!future.isDone()) {
            Thread.sleep(100l);
        }

        return future.get();

    }

    public String execute(String cmd, String[] args) throws Exception {
        CommandCallable command = context.create(cmd, args);
        Future<String> future = executorService.submit(command);
        while (!future.isDone()) {
            Thread.sleep(100l);
        }

        return future.get();
    }

    public static Builder builder(Class<? extends CommandCallable> commandType) {
        return new Builder(commandType);
    }

    public static class Builder {
        private Class<? extends CommandCallable> commandType;
        private Set<Class<? extends CommandCallable>> set = new HashSet<>();
        private Properties properties = new Properties();
        private CommandFactory commandFactory;
        private ExecutorService executorService;
        private Option commandOption;

        private Builder(Class<? extends CommandCallable> commandType) {
            this.commandType = commandType;
        }

        public Builder add(Class<? extends CommandCallable>... commandTypes) {
            for (Class<? extends CommandCallable> c : commandTypes) {
                if (commandType.isAssignableFrom(c)) {
                    set.add((Class<? extends CommandCallable>) c);
                }
            }
            return this;
        }

        public Builder scan(String packageName) {
            Reflections reflections = new Reflections(packageName);
            Set<Class<?>> subTypes =
                    reflections.getTypesAnnotatedWith(Command.class);
            subTypes.forEach(c -> {
                if (commandType.isAssignableFrom(c)) {
                    set.add((Class<? extends CommandCallable>) c);
                }
            });

            return this;
        }

        public Builder setProperty(String propName, String propValue) {
            properties.setProperty(propName, propValue);
            return this;
        }

        public Builder setProperties(Properties properties) {
            this.properties.putAll(properties);
            return this;
        }

        public Builder setCommandOption(String option, String longOption) {
            this.commandOption = Option.builder(option).longOpt(longOption).desc("Command").required().hasArg().build();
            return this;
        }

        public Builder setCommandFactory(CommandFactory commandFactory) {
            this.commandFactory = commandFactory;
            return this;
        }

        public Builder setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public CommandDelegate create() {
            if(commandOption == null) {
                commandOption = Option.builder("a").longOpt("action").hasArg().required().build();
            }

            CommandDelegateContext context = new CommandDelegateContext(set.toArray(new Class[set.size()]), properties, commandOption);
            if (commandFactory == null) {
                commandFactory = new DefaultCommandFactory();
            }

            if (executorService == null) {
                if (DEFAULT_EXECUTOR == null) {
                    DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();
                }

                executorService = DEFAULT_EXECUTOR;

            }

            return new CommandDelegate(executorService, context, commandFactory);
        }
    }

    static class DefaultCommandFactory implements CommandFactory {

        @Override
        public CommandCallable create(String commandline, CommandDelegateContext ctx) {
            String cmdOpt = "-" + ctx.commandOption().getOpt();

            List<String> tokens = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(commandline);
            while (tokenizer.hasMoreTokens()) {
                tokens.add(tokenizer.nextToken());
            }

            String action = null;
            List<String> args = new ArrayList<>();

            int i = 0;
            while (i < tokens.size()) {
                String token = tokens.get(i);
                if (cmdOpt.equals(token)) {
                    i++;
                    action = tokens.get(i);

                } else {
                    args.add(token);

                }

                i++;
            }

            return ctx.create(action, args.toArray(new String[args.size()]));
        }
    }

}
