package soya.framework.transform.application.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import soya.framework.commons.commandline.CommandDelegate;
import soya.framework.commons.commandline.commands.MessageCommand;
import soya.framework.kafka.KafkaClientFactory;
import soya.framework.kafka.commands.KafkaCommand;
import soya.framework.tool.commands.BusinessObjectCommand;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class CommandDelegateConfiguration {

    @Value("${workspace.home}")
    private String workspaceHome;

    @Bean
    ExecutorService commandExecutorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean("CommonCommandDelegate")
    CommandDelegate commonCommandDelegate(ExecutorService executorService) {
        return CommandDelegate.builder(MessageCommand.class)
                .scan(MessageCommand.class.getPackage().getName())
                .setExecutorService(executorService)
                .create();
    }

    @Bean("BusinessObjectCommandDelegate")
    CommandDelegate businessObjectCommandDelegate(ExecutorService executorService) {
        return CommandDelegate.builder(BusinessObjectCommand.class)
                .scan(BusinessObjectCommand.class.getPackage().getName())
                .setProperty("workspace.home", workspaceHome)
                .setExecutorService(executorService)
                .setCommandFactory((commandline, ctx) -> {

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

                        } else if ("-r".equals(token)) {
                            i++;

                        } else {
                            args.add(token);

                        }

                        i++;
                    }

                    args.add("-r");
                    args.add(workspaceHome);

                    return ctx.create(action, args.toArray(new String[args.size()]));
                })
                .create();

    }

    @Bean("KafkaCommandDelegate")
    CommandDelegate kafkaCommandDelegate(ExecutorService executorService) {
        try {
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("kafka-config.properties");
            properties.load(inputStream);
            KafkaClientFactory.configure(properties);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return CommandDelegate.builder(KafkaCommand.class)
                .scan(KafkaCommand.class.getPackage().getName())
                .setExecutorService(executorService)
                .setCommandFactory((commandline, ctx) -> {

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
                })
                .create();

    }

}
