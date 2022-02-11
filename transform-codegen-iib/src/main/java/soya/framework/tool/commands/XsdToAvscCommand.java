package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.Command;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.xs.XsNode;

@Command(name = "bod-avsc", uri = "bod://avsc")
public class XsdToAvscCommand extends SchemaCommand {

    @Override
    protected String render() {
        return XsdToAvsc.fromXmlSchema(tree.origin()).toString(true);
    }
}
