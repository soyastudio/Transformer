package soya.framework.tool.commands;

import soya.framework.commons.util.CodeBuilder;

@CommandExecutor(name = "json-types")
public class JsonTypeCommand extends XmlToJsonTypeCommand {

    protected void print(String path, String type, CodeBuilder codeBuilder) {
        codeBuilder.append(type).append("(").append(path).append(");");
    }
}