package soya.framework.transform.application.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandExecutor;

import javax.annotation.PostConstruct;
import java.util.*;

//@Service
public class CommandService {

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, CommandExecutor> delegateMap = new LinkedHashMap<>();
    private String json;

    @PostConstruct
    protected void init() {
        Map<String, CommandExecutor> beans = applicationContext.getBeansOfType(CommandExecutor.class);
        beans.entrySet().forEach(e -> {
            CommandExecutor delegate = e.getValue();
            String name = delegate.context().name();
            delegateMap.put(name, e.getValue());
        });

        this.json = toJson();
    }

    public String help() {
        return json;
    }

    public String properties(String delegate) {
        Properties properties = delegateMap.get(delegate).context().properties();


        List<?> list = new ArrayList<>(properties.keySet());
        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        StringBuilder builder = new StringBuilder();
        list.forEach(e -> {
            builder.append(e).append("=").append(properties.getProperty(e.toString())).append("\n");
        });

        return builder.toString();
    }

    public String execute(String delegate, String cmd) throws Exception {
        return delegateMap.get(delegate).execute(cmd);
    }

    public String execute(String delegate, String cmd, String options, String input) throws Exception {
        List<String> argList = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(options);
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();

            argList.add(token);
        }

        argList.add(input);
        return delegateMap.get(delegate).execute(cmd, argList.toArray(new String[argList.size()]));
    }

    private String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject object = new JsonObject();
        List<String> list = new ArrayList<String>(delegateMap.keySet());
        Collections.sort(list);
        list.forEach(e -> {
            JsonArray array = new JsonArray();
            CommandExecutor.Context ctx = delegateMap.get(e).context();
            String[] commands = ctx.commands();
            for (String cmd : commands) {
                Command annotation = ctx.getCommandType(cmd).getAnnotation(Command.class);
                Options options = ctx.options(cmd);

                JsonObject o = new JsonObject();
                o.addProperty("cmd", cmd);
                o.addProperty("description", annotation.desc());

                JsonArray opts = new JsonArray();
                options.getOptions().forEach(option -> {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("option", option.getOpt());
                    jo.addProperty("long-option", option.getLongOpt());
                    jo.addProperty("required", option.isRequired());
                    jo.addProperty("hasArg", option.hasArg());
                    jo.addProperty("description", option.getDescription());
                    opts.add(jo);
                });
                o.add("options", opts);

                JsonArray cases = new JsonArray();
                for (String cas : annotation.cases()) {
                    cases.add(cas);
                }
                o.add("example", cases);

                array.add(o);
            }
            object.add(e, array);
        });

        return gson.toJson(object);
    }

    public static class Builder {


        public CommandService create() {
            return new CommandService();
        }
    }


}
