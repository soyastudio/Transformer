package soya.framework.tool.commands;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

@Command(name = "bod-create", uri = "bod://create")
public class CreateCommand extends BusinessObjectCommand {

    @Override
    protected String execute() throws Exception {
        File project = new File(baseDir, "bod.json");
        BusinessObject bo = null;
        if (project.exists()) {
            bo = GSON.fromJson(new FileReader(project), BusinessObject.class);

        } else {
            project.createNewFile();
            bo = create(businessObject);
            FileUtils.write(project, GSON.toJson(bo), Charset.defaultCharset());

            createXPathSchema();

        }

        return GSON.toJson(bo);
    }

    private void createXPathSchema() throws IOException {
        File file = new File(cmmDir, "BOD/Get" + businessObject + ".xsd");
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        KnowledgeTree<SchemaTypeSystem, XsNode> tree = XsUtils.createKnowledgeTree(file);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        render(tree.root(), codeBuilder);

        File xpathSchema = new File(workDir, "xpath-schema.properties");
        xpathSchema.createNewFile();

        FileUtils.write(xpathSchema, codeBuilder.toString(), Charset.defaultCharset());
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
