package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.xs.XsNode;

@CommandExecutor(name = "sample-xml")
public class SampleXmlCommand extends SchemaCommand {

    @Override
    protected String render(KnowledgeTree<SchemaTypeSystem, XsNode> tree) {
        return SampleXmlUtil.createSampleForType(tree.origin().documentTypes()[0]);
    }
}
