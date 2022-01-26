package soya.framework.commons.commandline.commands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import soya.framework.commons.commandline.Command;

@Command(name = "json-format")
public class JsonBeautifyCommand extends MessageCommand {
    @Override
    public String call() throws Exception {
        return new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(message));
    }
}
