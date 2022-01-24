package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;

public abstract class SchemaCommand extends BusinessObjectCommand {

    @Override
    public String execute() throws Exception {
        File file = new File(cmmDir, "BOD/Get" + businessObject + ".xsd");
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        KnowledgeTree<SchemaTypeSystem, XsNode> tree = XsUtils.createKnowledgeTree(file);

        return render(tree);


    }

    protected abstract String render(KnowledgeTree<SchemaTypeSystem, XsNode> tree);
}
