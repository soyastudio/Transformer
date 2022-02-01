package soya.framework.commons.cli;

import org.apache.commons.cli.Options;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Flow {

    public final static Callback LOGGER = new LoggerCallback();

    private final static Evaluator DEFAULT_EVALUATOR = new DefaultEvaluator();

    private final CommandExecutor executor;

    private List<Task> tasks;

    private Flow(CommandExecutor executor, List<Task> tasks) {
        this.executor = executor;
        this.tasks = tasks;
    }

    public void execute(Callback callback, ExceptionHandler exceptionHandler) {
        DefaultSession session = new DefaultSession(executor.context().properties());
        for (Task task : tasks) {
            session.executed.add(task.configuration.getName());
            session.cursor = task.configuration.getName();

            String cmd = task.configuration.getCommand();
            String[] args = task.compiler.compile(task.configuration, session);

            Future<String> future = executor.submit(cmd, args);

            while (!future.isDone()) {
                try {
                    Thread.sleep(50l);
                } catch (InterruptedException e) {
                    session.onException(e, exceptionHandler);
                }
            }

            try {
                String result = future.get();
                session.onSuccess(task.getName(), result, task.callback);

            } catch (InterruptedException e) {
                session.onException(e, exceptionHandler);

            } catch (ExecutionException e) {
                session.onException(e, exceptionHandler);
            }

        }

        if (callback != null) {
            callback.onSuccess(session);
        }
    }

    public static FlowBuilder builder(CommandExecutor executor) {
        return new FlowBuilder(executor);
    }

    public static class Task {
        private final Configuration configuration;

        private Compiler compiler;
        private Callback callback;

        public Task(Configuration configuration, Compiler compiler, Callback callback) {
            this.configuration = configuration;
            this.compiler = compiler;
            this.callback = callback;
        }

        public String getName() {
            return configuration.getName();
        }

        public void execute(Session session) {

        }

        public static TaskBuilder builder(Class<? extends CommandCallable> commandType) {
            return new TaskBuilder(commandType);
        }

    }

    public interface Session {
        String getId();

        long startTime();

        Properties properties();

        String cursor();

        String[] executed();

        String getResult(String taskName);

        Object getAttribute(String name);

        void setAttribute(String name, Object value);

    }

    public interface Configuration {
        String getName();

        String getCommand();

        Options getOptions();

        void evaluator(String option, String exp);

        void evaluator(String option, String exp, Evaluator evaluator);

        String evaluate(String option, Session session);
    }

    public interface Compiler {
        String[] compile(Configuration configuration, Session session);
    }

    public interface Callback {
        void onSuccess(Session session);
    }

    public interface ExceptionHandler {
        void onException(Throwable cause, Session session);
    }

    public interface Evaluator {
        String evaluate(String exp, Session session);
    }

    public static class FlowBuilder {

        private CommandExecutor executor;
        private List<Task> tasks = new ArrayList<>();

        private FlowBuilder(CommandExecutor executor) {
            this.executor = executor;
        }

        public FlowBuilder addTask(Task task) {
            tasks.add(task);
            return this;
        }

        public Flow create() {
            return new Flow(executor, tasks);
        }
    }

    public static class TaskBuilder {
        private Class<? extends CommandCallable> commandType;
        private Configuration configuration;
        private Compiler compiler;
        private Callback callback;

        private TaskBuilder(Class<? extends CommandCallable> commandType) {
            this.commandType = commandType;
            this.configuration = new DefaultConfiguration(commandType);
        }

        public TaskBuilder name(String name) {
            ((DefaultConfiguration) configuration).name = name;
            return this;
        }

        public TaskBuilder setOption(String option, String value) {
            configuration.evaluator(option, value);
            return this;
        }

        public TaskBuilder setOption(String option, String exp, Evaluator evaluator) {
            configuration.evaluator(option, exp, evaluator);
            return this;
        }

        public TaskBuilder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Task create() {
            if (compiler == null) {
                this.compiler = new DefaultCompiler();
            }

            return new Task(configuration, compiler, callback);
        }
    }

    static class DefaultSession implements Session {
        private final String id;
        private final long startTime;
        private final Properties properties = new Properties();

        private List<String> executed = new ArrayList<>();
        private String cursor;
        private Map<String, String> results = new LinkedHashMap<>();

        private Map<String, Object> attributes = new HashMap<>();

        private DefaultSession(Properties properties) {
            this.id = UUID.randomUUID().toString();
            this.startTime = System.currentTimeMillis();

            this.properties.putAll(properties);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public long startTime() {
            return startTime;
        }

        @Override
        public Properties properties() {
            return properties;
        }

        @Override
        public String cursor() {
            return cursor;
        }

        @Override
        public String[] executed() {
            return executed.toArray(new String[executed.size()]);
        }

        @Override
        public String getResult(String taskName) {
            return results.get(taskName);
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            if (value != null) {
                attributes.put(name, value);

            } else {
                attributes.remove(name);
            }

        }

        private void onSuccess(String task, String result, Callback callback) {
            results.put(task, result);
            if (callback != null) {
                callback.onSuccess(this);
            }
        }

        private void onException(Exception e, ExceptionHandler exceptionHandler) {
            if (exceptionHandler != null) {
                exceptionHandler.onException(e, this);
            }
        }
    }

    static class DefaultConfiguration implements Configuration {
        private Class<? extends CommandCallable> commandType;
        private String name;
        private String command;
        private Options options;

        private Map<String, String> expressions = new HashMap<>();
        private Map<String, Evaluator> evaluators = new HashMap<>();

        DefaultConfiguration(Class<? extends CommandCallable> commandType) {
            this.commandType = commandType;
            Command command = commandType.getAnnotation(Command.class);
            this.command = command.name();
            this.name = command.name();
            this.options = CommandRunner.parse(commandType);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getCommand() {
            return command;
        }

        @Override
        public Options getOptions() {
            return options;
        }

        @Override
        public void evaluator(String option, String exp) {
            this.expressions.put(option, exp);

        }

        @Override
        public void evaluator(String option, String exp, Evaluator evaluator) {
            this.expressions.put(option, exp);
            if (evaluator != null) {
                evaluators.put(option, evaluator);
            }

        }

        @Override
        public String evaluate(String option, Session session) {
            String exp = expressions.get(option);
            Evaluator evaluator = evaluators.containsKey(option) ? evaluators.get(option) : DEFAULT_EVALUATOR;

            return evaluator.evaluate(exp, session);
        }
    }

    static class DefaultCompiler implements Compiler {

        @Override
        public String[] compile(Configuration configuration, Session session) {
            List<String> list = new ArrayList<>();
            configuration.getOptions().getOptions().forEach(e -> {
                String opt = e.getOpt();
                String value = configuration.evaluate(opt, session);
                if (value != null) {
                    list.add("-" + opt);
                    list.add(value);

                }
            });

            return list.toArray(new String[list.size()]);
        }
    }

    static class DefaultEvaluator implements Evaluator {
        private final String regex = "\\$\\{([A-Za-z_.][A-Za-z0-9_.]*)}";
        private final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        @Override
        public String evaluate(String exp, Session session) {
            String expression = exp;

            if (expression != null && expression.contains("${")) {
                StringBuffer buffer = new StringBuffer();
                Matcher matcher = pattern.matcher(expression);
                while (matcher.find()) {
                    String token = matcher.group(1);
                    String value = getValue(token, session);
                    matcher.appendReplacement(buffer, value);
                }
                matcher.appendTail(buffer);

                expression = buffer.toString();
            }

            return expression;
        }

        private String getValue(String attribute, Session session) {
            Properties properties = session.properties();

            if (attribute.startsWith(".")) {
                return session.getResult(attribute.substring(1));

            } else if (properties.getProperty(attribute) != null) {
                return properties.getProperty(attribute);

            } else if (System.getProperty(attribute) != null) {
                return System.getProperty(attribute);

            }


            throw new IllegalArgumentException("Cannot find attribute on current context: " + attribute);
        }
    }

    static class LoggerCallback implements Callback {

        @Override
        public void onSuccess(Session session) {
            System.out.println(new Date(session.startTime()) + "[" + session.getId() + "] " + session.cursor());
        }
    }
}
