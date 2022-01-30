package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

@Command(name = "schema")
public class XmlSchemaCommand extends SchemaCommand {

    @Override
    protected String render(KnowledgeTree<SchemaTypeSystem, XsNode> tree) {
        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        render(tree.root(), codeBuilder);

        return codeBuilder.toString();
    }

    private void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
        codeBuilder.append(node.getPath())
                .append("=").append("type(").append(XsUtils.type(node.origin())).append(")")
                .append("::").append("cardinality(").append(XsUtils.cardinality(node.origin())).appendLine(")");
        node.getChildren().forEach(e -> {
            render(e, codeBuilder);
        });
    }
}
