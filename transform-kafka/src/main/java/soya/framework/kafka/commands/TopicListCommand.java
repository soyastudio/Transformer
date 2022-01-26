package soya.framework.kafka.commands;

import org.apache.kafka.clients.admin.AdminClient;
import soya.framework.commons.commandline.Command;
import soya.framework.commons.commandline.CommandOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Command(name = "topics")
public class TopicListCommand extends KafkaCommand {

    @CommandOption(option = "q", longOption = "query")
    private String query;

    @Override
    public String call() throws Exception {
        List<String> results = new ArrayList<>();

        AdminClient adminClient = createAdminClient();
        Future<Set<String>> future = adminClient.listTopics().names();
        while (!future.isDone()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            List<String> topics = new ArrayList<>(future.get());
            Collections.sort(topics);
            topics.forEach(e -> {
                if (query == null || e.startsWith(query)) {
                    results.add(e);

                }
            });

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return GSON.toJson(results);
    }
}
