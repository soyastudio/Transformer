package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;

import java.io.File;
import java.util.Map;

@Command(name = "deprecated-mappings", uri = "bod://deprecated-mappings")
public class DeprecatedMappingsCommand extends XPathMappingsCommand {



    @Override
    protected String render() {
        CodeBuilder builder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String key = e.getKey();
            if (key.startsWith("#")) {
                builder.appendLine(key.substring(1).trim());
            }
        });

        return builder.toString();
    }

    @Override
    protected void annotate() throws Exception {

    }
}
