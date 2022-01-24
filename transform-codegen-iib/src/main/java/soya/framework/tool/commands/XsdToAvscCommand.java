package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.xs.XsNode;

@CommandExecutor(name = "avsc")
public class XsdToAvscCommand extends SchemaCommand {

    @Override
    protected String render(KnowledgeTree<SchemaTypeSystem, XsNode> tree) {
        return XsdToAvsc.fromXmlSchema(tree.origin()).toString(true);
    }
}
