package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.transform.schema.converter.XsdToAvsc;

@Command(name = "bod-avsc", uri = "bod://avsc")
public class XsdToAvscCommand extends SchemaCommand {

    @Override
    protected String render() {
        System.out.println("==================== !!!");
        return XsdToAvsc.fromXmlSchema(tree.origin()).toString(true);
    }
}
