package soya.framework.tool.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import soya.framework.commons.cli.Command;

import java.io.File;
import java.io.FileReader;

@Command(name = "bod-json-schema", uri = "bod://json-schema")
public class JsonSchemaCommand extends BusinessObjectCommand {

    public static final String JSON_SCHEMA_FILE = "source-schema.json";

    @Override
    protected String execute() throws Exception {
        File jsonSchemaFile = new File(workDir, JSON_SCHEMA_FILE);
        JsonObject root = JsonParser.parseReader(new FileReader(jsonSchemaFile)).getAsJsonObject();

        StringBuilder builder = new StringBuilder();
        builder.append("$=object").append("\n");

        String parent = "$";
        root.get("properties").getAsJsonObject().entrySet().forEach(p -> {
            String propName = p.getKey();
            JsonObject propValue = p.getValue().getAsJsonObject();
            print(parent, propName, propValue, builder);

        });

        return builder.toString();
    }

    private void print(String parent, String name, JsonObject value, StringBuilder builder) {
        String type = value.get("type").getAsString();
        String path = parent + "." + name;
        if ("array".equals(type)) {
            path = path + "[*]";
            JsonObject items = value.get("items").getAsJsonObject();
            String elementType = items.get("type").getAsString();
            builder.append(path).append("=").append(elementType).append("\n");


            if ("object".equals(elementType)) {
                JsonObject elp = items.get("properties").getAsJsonObject();
                final String sub = path;
                elp.entrySet().forEach(e -> {
                    print(sub, e.getKey(), e.getValue().getAsJsonObject(), builder);
                });
            }

        } else if ("object".equals(type)) {
            if (value.get("properties") == null) {
                builder.append(path).append("=???").append("\n");

            } else {

                builder.append(path).append("=").append(type).append("\n");
                JsonObject props = value.get("properties").getAsJsonObject();

                String subParent = path;
                props.entrySet().forEach(e -> {
                    print(subParent, e.getKey(), e.getValue().getAsJsonObject(), builder);

                });

            }

        } else {
            builder.append(parent).append(".").append(name).append("=").append(type).append("\n");

        }

    }


}
