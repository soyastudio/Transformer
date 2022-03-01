package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.cli.Resources;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

@Command(name = "bod-esql-validate", uri = "bod://esql-validate")
public class EsqlValidateCommand extends XPathMappingsCommand {

    @CommandOption(option = "c", longOption = "contents", required = true)
    protected String contents;

    private Set<String> lines = new LinkedHashSet<>();

    @Override
    protected void annotate() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(Resources.get(contents)));
        String line = reader.readLine();
        while (line != null) {
            String token = line.trim();
            if (token.startsWith("-- ")) {
                lines.add(token);
            }
            line = reader.readLine();
        }
    }

    @Override
    protected String render() {
        StringBuilder builder = new StringBuilder();
        mappings.entrySet().forEach(e -> {
            String path = "-- " + e.getKey();
            Mapping mapping = e.getValue();
            if (mapping.rule != null && !lines.contains(path)) {
                builder.append(e.getKey()).append("=").append(mapping.source).append("\n");
            }
        });

        return builder.toString();
    }

    protected String render2() {
        StringBuilder builder = new StringBuilder();
        lines.forEach(e -> {
            builder.append(e).append("\n");
        });

        return builder.toString();
    }
}
