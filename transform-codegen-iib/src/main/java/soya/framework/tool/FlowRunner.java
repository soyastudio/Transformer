package soya.framework.tool;

import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.Flow;
import soya.framework.commons.cli.commands.ResourceCommand;
import soya.framework.commons.cli.commands.SessionInfoCallback;
import soya.framework.kafka.KafkaClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;

public class FlowRunner {
    public static void main(String[] args) throws Exception {

        try {
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream("C:/github/kafka-config.properties");
            properties.load(inputStream);
            KafkaClientFactory.configure(properties);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        CommandExecutor executor = CommandExecutor.builder(ResourceCommand.class)
                .scan(ResourceCommand.class.getPackage().getName())
                .setProperty("workspace.home", "C:/github/Workshop/AppBuild")
                .create();

        Flow.FlowBuilder builder = Flow.builder(new File("C:/github/Transformer/workflow.yaml"));

        Flow flow = builder.create();

        flow.execute(Flow.callbacks()
                .add(SessionInfoCallback
                        .instance()
                        .printProperties()
                        .printTaskResult("validate"))
        );

        System.exit(0);
    }
}
