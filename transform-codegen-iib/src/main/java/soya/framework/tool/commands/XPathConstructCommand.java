package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

@Command(name = "construct", uri = "bod://construct")
public class XPathConstructCommand extends ConstructCommand {

    @Override
    protected String render() {
        CodeBuilder builder = CodeBuilder.newInstance();
        printNode(tree.root(), builder);
        return builder.toString();
    }

    private void printNode(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
        if(node.getAnnotation(ASSIGNMENT_NAMESPACE) == null && node.getAnnotation(CONSTRUCTION_NAMESPACE) == null) {
            return;
        }

        builder.append(node.getPath()).append("=");
        if(node.getAnnotation(ASSIGNMENT_NAMESPACE) != null) {
            printAssignment((Assignment) node.getAnnotation(ASSIGNMENT_NAMESPACE), builder);

        } else if( node.getAnnotation(CONSTRUCTION_NAMESPACE) != null) {
            printConstruction(node, builder);
        }
    }

    private void printAssignment(Assignment assignment, CodeBuilder builder) {
        builder.appendLine(assignment.toString());
    }

    private void printConstruction(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
        Construction construction = (Construction) node.getAnnotation(CONSTRUCTION_NAMESPACE);
        builder.appendLine(construction.toString());
        node.getChildren().forEach(e -> {
            printNode(e, builder);
        });
    }
}
