package soya.framework.tool;

import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.Flow;
import soya.framework.commons.cli.commands.DefaultFileSystemProcessChain;
import soya.framework.commons.cli.commands.ResourceCommand;
import soya.framework.commons.cli.commands.SessionInfoCallback;
import soya.framework.kafka.KafkaClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

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
                .setProperty("bod", "GroceryOrder")
                .create();

        Flow.FlowBuilder builder = Flow.builder(new File("C:/github/Transformer/development-workflow.yaml"));

        Flow flow = builder.create();

        flow.execute(Flow.callbacks()
                .add(SessionInfoCallback
                        .instance()
                        .printProperties()
                        //.printTaskResult("validate")
                        //.printTaskResult("esql")
                        .printTaskResult("construct")
                        //.printTaskResult("arrays")
                )

                .add(DefaultFileSystemProcessChain
                        .instance("C:/github/Workshop/AppBuild/work")
                        .mkdir("${bod.name}")
                        .createFile("${bod.name}/xpath-schema.properties", "cmm", true)
                        .createFile("${bod.name}/xpath-mappings.properties", "xlsx_mapping", true)
                        .createFile("${bod.name}/xpath-adjustments.properties", "validate", true)
                        .createFile("${bod.name}/xpath-construct.properties", "construct", true)
                        .createFile("${bod.name}/iib-esql.esql", "esql", true)
                )

        );

        System.exit(0);
    }
}
