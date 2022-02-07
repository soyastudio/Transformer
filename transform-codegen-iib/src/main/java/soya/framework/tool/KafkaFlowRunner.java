package soya.framework.tool;

import soya.framework.commons.cli.CommandExecutor;
import soya.framework.commons.cli.Flow;
import soya.framework.kafka.KafkaClientFactory;
import soya.framework.kafka.commands.KafkaCommand;
import soya.framework.kafka.commands.PubAndSubCommand;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class KafkaFlowRunner {
    public static void main(String[] args) {

        try {
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream("C:/github/kafka-config.properties");
            properties.load(inputStream);
            KafkaClientFactory.configure(properties);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        CommandExecutor executor = CommandExecutor.builder(KafkaCommand.class)
                .scan(KafkaCommand.class.getPackage().getName())
                .setProperty("workspace.home", "https://github.com/soyastudio")
                .create();

        Flow.FlowBuilder builder = Flow.builder(executor);

        /*builder.addTask(Flow.Task.builder(TopicListCommand.class)
                .name("topics")
                .setOption("e", "dev")
                .setOption("q", "ESED_C01_")
                .create());*/

        /*builder.addTask(Flow.Task.builder(PubAndSubCommand.class)
                .name("test")
                .setOption("e", "dev")
                        .setOption("p", "OCRP_C02_EDIS_ALASKA_SUMMARY_OUTBOUND")
                        .setOption("c", "ESED_C01_AirMilePoints")
                        .setOption("m", "C:/github/test/summary.json")
                        .setOption("t", "3000")
                .create());*/

        builder.addTask(Flow.Task.builder(PubAndSubCommand.class)
                .name("PubAndSub")
                .setOption("e", "dev")
                .setOption("m", "C:/github/test/details.json")
                .setOption("p", "OCRP_C02_EDIS_ALASKA_DETAILS_OUTBOUND")
                .setOption("c", "ESED_C01_AirMilePoints")
                .create());

        Flow flow = builder.create();

        flow.execute(session -> {
            System.out.println(session.getResult("PubAndSub"));
        });

        System.exit(0);
    }
}
