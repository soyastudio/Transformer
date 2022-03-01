package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.cli.Resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

@Command(name = "bod-mappings-override", uri = "bod://mappings-override")
public class XPathMappingsOverrideCommand extends XPathMappingsRenderer {

    @CommandOption(option = "o", longOption = "override")
    protected String override;

    @Override
    protected void annotate() throws Exception {
        if(override != null) {
            String contents = Resources.get(override);
            try {
                BufferedReader reader = new BufferedReader(new StringReader(contents));
                String line = reader.readLine();
                while (line != null) {
                    if (line.length() > 0 && !line.trim().startsWith("#") && line.contains("=")) {
                        String key = line.substring(0, line.indexOf("=")).trim();
                        String value = line.substring(line.indexOf("=") + 1).trim();

                        Mapping mapping = mappings.get(key);
                        if (mapping != null) {
                            Function[] functions = toFunctions(value);
                            for (Function function : functions) {
                                String name = function.getName();
                                String parameter = function.getParameters()[0];
                                if ("type".equals(name)) {
                                    mapping.type = parameter;
                                } else if ("cardinality".equals(name)) {
                                    mapping.cardinality = parameter;
                                }
                            }
                        }
                    }

                    line = reader.readLine();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
