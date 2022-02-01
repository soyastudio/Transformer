package soya.framework.commons.cli;

import soya.framework.commons.cli.commands.Base64EncodeCommand;
import soya.framework.commons.cli.commands.ResourceCommand;
import soya.framework.commons.cli.commands.ResourceExtractCommand;

public class FlowRunner {
    public static void main(String[] args) {

        CommandExecutor executor = CommandExecutor.builder(ResourceCommand.class)
                .scan(ResourceCommand.class.getPackage().getName())
                .setProperty("workspace.home", "https://github.com/soyastudio")
                .create();

        Flow.FlowBuilder builder = Flow.builder(executor);

        builder.addTask(Flow.Task.builder(ResourceExtractCommand.class)
                .name("input")
                .setOption("s", "C:\\github\\SoyaCoder\\website\\markdown\\apache_avro_overview.md")
                .setCallback(Flow.LOGGER)
                .create());

        builder.addTask(Flow.Task.builder(Base64EncodeCommand.class)
                .name("two")
                .setOption("s", "${.input}")
                .setCallback(Flow.LOGGER)
                .create());

        Flow flow = builder.create();

        flow.execute(session -> {

                },
                (cause, session) -> {

                });

        System.exit(0);
    }
}
