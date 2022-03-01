package soya.framework.tool.commands;

import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;

public abstract class SchemaCommand extends BusinessObjectCommand {

    protected KnowledgeTree<SchemaTypeSystem, XsNode> tree;

    @Override
    public String execute() throws Exception {
        loadXmlSchema();
        process();
        return render();
    }

    private void loadXmlSchema() {
        File file = new File(cmmDir, "BOD/Get" + businessObject + ".xsd");
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        this.tree = XsUtils.createKnowledgeTree(file);
    }

    protected void process() throws Exception {

    }

    protected abstract String render();
}
