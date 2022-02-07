package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.Command;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;
import java.util.Map;

@Command(name = "bod-mapping-validate", uri = "bod://validate")
public class XPathMappingValidateCommand extends XPathMappingsCommand {

    private KnowledgeTree<SchemaTypeSystem, XsNode> tree;
/*

    @Override
    protected String execute() throws Exception {
        super.execute();

        XmlSchemaCommand schemaCommand = new XmlSchemaCommand();
        schemaCommand.home = this.home;
        schemaCommand.businessObject = this.businessObject;
        schemaCommand.init();
        schemaCommand.execute();
        KnowledgeTree<SchemaTypeSystem, XsNode> tree = schemaCommand.tree;

        XlsxMappingsCommand mappingsCommand = new XlsxMappingsCommand();
        mappingsCommand.home = this.home;
        mappingsCommand.businessObject = this.businessObject;
        mappingsCommand.init();
        mappingsCommand.execute();

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
*/

    @Override
    protected File getFile() {
        return new File(workDir, XPATH_MAPPINGS_FILE);
    }

    @Override
    protected void load(File file) throws Exception {
        super.load(file);

        XmlSchemaCommand schemaCommand = new XmlSchemaCommand();
        schemaCommand.home = this.home;
        schemaCommand.businessObject = this.businessObject;
        schemaCommand.init();
        schemaCommand.execute();
        this.tree = schemaCommand.tree;

    }

    @Override
    protected String render(Map<String, Mapping> mappings) {
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


}
