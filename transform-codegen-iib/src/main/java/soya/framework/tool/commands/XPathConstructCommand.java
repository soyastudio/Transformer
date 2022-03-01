package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

@Command(name = "bod-construct", uri = "bod://construct")
public class XPathConstructCommand extends ConstructCommand {

    @Override
    protected String render() {
        CodeBuilder builder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String construction = e.getValue().construct();
            if(!construction.isEmpty()) {
                builder.append(e.getKey()).append("=").append(construction).appendLine();
            }
        });

        return builder.toString();
    }

}
