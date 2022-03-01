package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;

import java.util.Locale;

@Command(name = "bod-xpath-mappings", uri = "bod://xpath-mappings")
public class XPathMappingsRenderer extends XPathMappingsCommand {

    @Override
    protected void annotate() throws Exception {
    }

    @Override
    protected String render() {

        CodeBuilder builder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String value = e.getValue().toString().toUpperCase(Locale.ROOT);
            builder.append(e.getKey()).append("=").appendLine(e.getValue().toString());

        });
        return builder.toString();
    }
}
