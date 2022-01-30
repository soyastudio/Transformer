package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;

@Command(name = "json-types")
public class JsonTypeCommand extends XmlToJsonTypeCommand {

    protected void print(String path, String type, CodeBuilder codeBuilder) {
        codeBuilder.append(type).append("(").append(path).append(");");
    }
}