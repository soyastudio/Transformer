package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.transform.schema.xs.XsNode;

@Command(name = "bod-mappings-validate", uri = "bod://mappings-validate")
public class XPathMappingsValidator extends XPathMappingsCommand {

    @Override
    protected String render() {
        StringBuilder builder = new StringBuilder();
        mappings.entrySet().forEach(e -> {
            String path = e.getKey().trim();
            Mapping mapping = e.getValue();
            if (!path.startsWith("#")) {
                if (!tree.contains(path)) {
                    builder.append(path).append("=unknown()").append("\n");

                } else {
                    XsNode node = tree.get(path).origin();
                    String result = validate(mapping, node);
                    if (result != null) {
                        builder.append(path).append("=").append(result).append("\n");

                    }
                }

            }
        });

        return builder.toString();
    }

    private String validate(Mapping mapping, XsNode node) {

        String type = XsUtils.type(node);
        String cardinality = XsUtils.cardinality(node);

        if (mapping.type.equalsIgnoreCase(type) && mapping.cardinality.equalsIgnoreCase(cardinality)) {
            return null;

        } else {
            StringBuilder builder = new StringBuilder();
            if (!mapping.type.equalsIgnoreCase(type)) {
                builder.append("type(").append(mapping.type).append(" -> ").append(type).append(")::");
            }

            if (!mapping.cardinality.equals(cardinality)) {
                builder.append("cardinality(").append(mapping.cardinality).append(" -> ").append(cardinality).append(")");
            }

            String result = builder.toString();
            if (result.endsWith("::")) {
                result = result.substring(0, result.lastIndexOf("::"));
            }

            return result;

        }
    }

    @Override
    protected void annotate() throws Exception {

    }
}
