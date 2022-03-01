package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.util.CodeBuilder;

import java.io.File;
import java.util.Locale;
import java.util.Map;

@Command(name = "filter-mapping", uri = "bod://filter-mapping")
public class FilterMappingsCommand extends XPathMappingsCommand {

    @CommandOption(option = "q", longOption = "query")
    protected String expression;

    @Override
    protected String render() {
        String token = expression == null? "::" : expression.toUpperCase(Locale.ROOT);
        CodeBuilder builder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String value = e.getValue().toString().toUpperCase(Locale.ROOT);
            if(value.contains(token)) {
                builder.append(e.getKey()).append("=").appendLine(e.getValue().toString());
            }

        });
        return builder.toString();
    }

    @Override
    protected void annotate() throws Exception {

    }
}
