package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;

import java.io.File;
import java.util.Locale;
import java.util.Map;

@Command(name = "xpath-mapping", uri = "bod://xpath-mapping")
public class XPathMappingsRendererCommand extends XPathMappingsCommand{
    @Override
    protected File getFile() {
        return new File(workDir, XPATH_MAPPINGS_FILE);
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
