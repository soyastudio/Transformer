package soya.framework.commons.cli;

import org.apache.commons.cli.Options;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Flow {
    private final static Evaluator DEFAULT_EVALUATOR = new DefaultEvaluator();

    private final CommandExecutor executor;
    private List<Task> tasks = new ArrayList<>();

    private Flow(CommandExecutor executor, List<Task> tasks) {
        this.executor = executor;
        this.tasks = tasks;
    }

    public void execute(String input, Callback callback) {
        DefaultSession session = new DefaultSession(input);
        for (Task task : tasks) {
            session.executed.add(task.configuration.getName());
            session.cursor = task.configuration.getName();

            String cmd = task.configuration.getCommand();
            String[] args = task.compiler.compile(task.configuration, session, executor.context());

            Future<String> future = executor.submit(cmd, args);

            while (!future.isDone()) {
                try {
                    Thread.sleep(50l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                String result = future.get();
                session.onSuccess(result, task.callback);

            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (ExecutionException e) {
                session.onException(e, task.exceptionHandler);
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
        private ExceptionHandler exceptionHandler;

        public Task(Configuration configuration, Compiler compiler, Callback callback, ExceptionHandler exceptionHandler) {
            this.configuration = configuration;
            this.compiler = compiler;
            this.callback = callback;
            this.exceptionHandler = exceptionHandler;
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
        String input();

        String output();

        Exception exception();

        String cursor();

        String[] executed();

        String getResult(String taskName);

    }

    public interface Configuration {
        String getName();

        String getCommand();

        Options getOptions();

        void evaluator(String option, String exp);

        void evaluator(String option, String exp, Evaluator evaluator);

        String evaluate(String option, Session session, CommandExecutor.Context context);
    }

    public interface Compiler {
        String[] compile(Configuration configuration, Session session, CommandExecutor.Context context);
    }

    public interface Callback {
        void onSuccess(Session session);
    }

    public interface ExceptionHandler {
        void onException(Exception exception, Session session);

        void onException(Throwable cause);
    }

    public interface Evaluator {
        String evaluate(String exp, Session session, Properties properties);
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
        private ExceptionHandler exceptionHandler;

        private TaskBuilder(Class<? extends CommandCallable> commandType) {
            this.commandType = commandType;
            this.configuration = new DefaultConfiguration(commandType);
        }

        public TaskBuilder name(String name) {
            ((DefaultConfiguration) configuration).name = name;
            return this;
        }

        public TaskBuilder evaluator(String option, String value) {
            configuration.evaluator(option, value);
            return this;
        }

        public TaskBuilder evaluator(String option, String exp, Evaluator evaluator) {
            configuration.evaluator(option, exp, evaluator);
            return this;
        }

        public Task create() {
            if (compiler == null) {
                this.compiler = new DefaultCompiler();
            }

            return new Task(configuration, compiler, callback, exceptionHandler);
        }
    }

    private static class DefaultSession implements Session {
        private final String input;
        private String output;
        private Exception exception;

        private List<String> executed = new ArrayList<>();
        private String cursor;
        private Map<String, String> results = new LinkedHashMap<>();

        public DefaultSession(String input) {
            this.input = input;
        }

        @Override
        public String input() {
            return this.input;
        }

        @Override
        public String output() {
            return output;
        }

        @Override
        public Exception exception() {
            return exception;
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

        private void onSuccess(String result, Callback callback) {
            results.put(cursor, result);
            if (callback != null) {
                callback.onSuccess(this);
            }
        }

        private void onException(Exception e, ExceptionHandler exceptionHandler) {
            this.exception = e;
            if (exceptionHandler != null) {
                exceptionHandler.onException(e, this);
            }
        }
    }

    private static class DefaultConfiguration implements Configuration {
        private Class<? extends CommandCallable> commandType;
        private String name;
        private String command;
        private Options options;

        private Map<String, String> expressions = new HashMap<>();
        private Map<String, Evaluator> evaluators = new HashMap<>();

        public DefaultConfiguration(Class<? extends CommandCallable> commandType) {
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

        public void evaluator(String option, String exp) {
            this.expressions.put(option, exp);

        }

        public void evaluator(String option, String exp, Evaluator evaluator) {
            this.expressions.put(option, exp);
            if (evaluator != null) {
                evaluators.put(option, evaluator);
            }

        }

        @Override
        public String evaluate(String option, Session session, CommandExecutor.Context context) {
            String exp = expressions.get(option);
            Evaluator evaluator = evaluators.containsKey(option) ? evaluators.get(option) : DEFAULT_EVALUATOR;

            return evaluator.evaluate(exp, session, context.properties());
        }
    }

    static class DefaultCompiler implements Compiler {
        @Override
        public String[] compile(Configuration configuration, Session session, CommandExecutor.Context context) {
            List<String> list = new ArrayList<>();
            configuration.getOptions().getOptions().forEach(e -> {
                list.add("-" + e.getOpt());

                list.add(configuration.evaluate(e.getOpt(), session, context));

            });

            return list.toArray(new String[list.size()]);
        }
    }

    static class DefaultEvaluator implements Evaluator {

        @Override
        public String evaluate(String exp, Session session, Properties properties) {
            String token = exp;
            if(exp.contains("${")) {



            }

            return token;
        }
    }
}
