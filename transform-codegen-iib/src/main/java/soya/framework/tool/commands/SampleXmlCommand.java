package soya.framework.tool.commands;

import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import soya.framework.commons.cli.Command;

@Command(name = "sample-xml", uri = "bod://sample-xml")
public class SampleXmlCommand extends SchemaCommand {

    @Override
    protected String render() {
        return SampleXmlUtil.createSampleForType(tree.origin().documentTypes()[0]);
    }
}
