package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.util.CodeBuilder;

import java.io.File;
import java.util.Locale;
import java.util.Map;

@Command(name = "filter")
public class FilterMappingsCommand extends XPathMappingsCommand {

    @CommandOption(option = "q", longOption = "query")
    protected String expression;

    @Override
    protected File getFile() {
        return new File(workDir, XPATH_MAPPINGS_FILE);
    }

    @Override
    protected String render(Map<String, Mapping> mappings) {
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
}
