package soya.framework.commons.cli;

import soya.framework.commons.cli.commands.EchoCommand;
import soya.framework.commons.cli.commands.MessageCommand;

public class FlowRunner {
    public static void main(String[] args) {
        CommandExecutor executor = CommandExecutor.builder(MessageCommand.class)
                .scan(MessageCommand.class.getPackage().getName())
                .create();

        Flow.FlowBuilder builder = Flow.builder(executor);

        builder.addTask(Flow.Task.builder(EchoCommand.class)
                .name("one")
                .evaluator("m", "Hello")
                .create());


        builder.addTask(Flow.Task.builder(EchoCommand.class)
                .name("two")
                .evaluator("m", "World")
                .create());


        Flow flow = builder.create();

        flow.execute("Hello World", session -> {
            for (String t : session.executed()) {
                System.out.println("============ " + t + ": " + session.getResult(t));
            }
        });

        System.exit(0);
    }
}
