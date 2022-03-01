package soya.framework.tool.commands;

import soya.framework.commons.cli.Command;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

import java.util.StringTokenizer;

@Command(name = "bod-esql", uri = "bod://esql")
public class EsqlGenCommand extends ConstructCommand {

    private String inputRootVariable = "_inputRootNode";
    private String inputRootReference = "InputRoot.JSON.Data";

    private String outputRootName;
    private String outputRootVariable;

    @Override
    protected String render() {
        String brokerSchema = bod.getFlows().get(0).getPackageName();
        String module = bod.getFlows().get(0).getTransformer() + "_Compute";

        outputRootName = tree.root().getName();
        outputRootVariable = outputRootName + "_";

        CodeBuilder builder = CodeBuilder.newInstance();
        if (brokerSchema != null && brokerSchema.trim().length() > 0) {
            builder.append("BROKER SCHEMA ").append(brokerSchema.trim()).append("\n\n");
        }
        builder.append("CREATE COMPUTE MODULE ").appendLine(module);
        builder.appendLine();

        // UDP:
        builder.appendLine("-- Declare UDPs", 1);
        builder.appendLine("DECLARE VERSION_ID EXTERNAL CHARACTER '1.0.0';", 1);
        builder.appendLine("DECLARE SYSTEM_ENVIRONMENT_CODE EXTERNAL CHARACTER 'PROD';", 1);

        builder.appendLine();

        // Namespace
        declareNamespace(builder);

        builder.appendLine("CREATE FUNCTION Main() RETURNS BOOLEAN", 1);
        begin(builder, 1);

        // Declare Input Root
        builder.appendLine("-- Declare Input Message Root", 2);
        builder.appendLine("DECLARE " + inputRootVariable + " REFERENCE TO " + inputRootReference + ";", 2);
        builder.appendLine();

        // Declare Output Domain
        builder.appendLine("-- Declare Output Message Root", 2);
        builder.append("CREATE LASTCHILD OF OutputRoot DOMAIN ", 2).append("'XMLNSC'").appendLine(";").appendLine();

        builder.append("DECLARE ", 2).append(outputRootVariable).append(" REFERENCE TO OutputRoot.XMLNSC.").append(outputRootName).appendLine(";");
        builder.append("CREATE LASTCHILD OF OutputRoot.", 2).append("XMLNSC AS ").append(outputRootVariable).append(" TYPE XMLNSC.Folder NAME '").append(outputRootName).append("'").appendLine(";");
        builder.append("SET OutputRoot.XMLNSC.", 2).append(outputRootName).appendLine(".(XMLNSC.NamespaceDecl)xmlns:Abs=Abs;");
        builder.appendLine();

        //print node:
        tree.root().getChildren().forEach(e -> {
            printNode(e, builder, 2);
        });

        // closing
        builder.appendLine("RETURN TRUE;", 2);
        builder.appendLine("END;", 1);
        builder.appendLine();
        builder.appendLine("END MODULE;");

        return builder.toString();
    }

    private void declareNamespace(CodeBuilder builder) {
        builder.appendLine("-- Declare Namespace", 1);
        builder.appendLine("DECLARE " + "Abs" + " NAMESPACE " + "'https://collab.safeway.com/it/architecture/info/default.aspx'" + ";", 1);
        builder.appendLine();
    }

    private void begin(CodeBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }

        builder.append("BEGIN").append("\n");
    }

    private void printNode(KnowledgeTreeNode<XsNode> node, CodeBuilder builder, int indent) {
        if (node.getAnnotation(CONSTRUCTION_NAMESPACE) != null) {
            if (node.getParent() != null) {
                printConstruction(node, builder, indent);
            }

        } else if (node.getAnnotation(ASSIGNMENT_NAMESPACE) != null) {
            printAssignment(node, builder, indent);

        } else {
            return;

        }
    }

    private void printAssignment(KnowledgeTreeNode<XsNode> node, CodeBuilder builder, int indent) {

        Assignment assignment = (Assignment) node.getAnnotation(ASSIGNMENT_NAMESPACE);
        Construction construction = (Construction) node.getParent().getAnnotation(CONSTRUCTION_NAMESPACE);

        builder.append("-- ", indent).appendLine(node.getPath());

        String type = "(XMLNSC.Field)";
        String name = node.getName();
        String assign = assignment.getAssign();

        if (assign.endsWith("[*]")) {
            builder.append("-- SET ", indent)
                    .append(construction.getVariable()).append(".").append(type).append(name)
                    .append(" = ")
                    .append(assign).appendLine(";")
                    .appendLine();

        } else {
            if (assign.startsWith("$.")) {
                assign = inputRootVariable + assign.substring(1);

            } else if ("???".equals(assign)) {
                assign = "'???'";

            }

            if (XsNode.XsNodeType.Attribute.equals(node.origin().getNodeType())) {
                type = "(XMLNSC.Attribute)";
                if (name.startsWith("@")) {
                    name = name.substring(1);
                }
            }

            if (node.origin().getName().getNamespaceURI() != null && node.origin().getName().getNamespaceURI().trim().length() > 0) {
                type = type + "Abs:";
            }

            builder.append("SET ", indent)
                    .append(construction.getVariable()).append(".").append(type).append(name)
                    .append(" = ")
                    .append(assign).appendLine(";")
                    .appendLine();
        }
    }

    private void printConstruction(KnowledgeTreeNode<XsNode> node, CodeBuilder builder, int indent) {
        Construction construction = (Construction) node.getAnnotation(CONSTRUCTION_NAMESPACE);
        if (construction.arrays().size() > 0) {
            int i = 0;
            for (Array arr : construction.arrays()) {
                if (i > 0) {
                    arr.addIndex(i);
                }
                printArray(arr, builder, indent);
                i++;
            }

        } else {
            Construction parent = (Construction) node.getParent().getAnnotation(CONSTRUCTION_NAMESPACE);
            String name = node.getName();
            if (node.origin().getName().getNamespaceURI().trim().length() > 0) {
                name = "Abs:" + name;
            }

            builder.append("-- ", indent).appendLine(node.getPath());
            builder.append("DECLARE ", indent)
                    .append(construction.getVariable())
                    .append(" REFERENCE TO ")
                    .append(parent.getVariable()).appendLine(";");

            builder.append("CREATE LASTCHILD OF ", indent)
                    .append(parent.getVariable())
                    .append(" AS ")
                    .append(construction.getVariable())
                    .append(" TYPE XMLNSC.Folder NAME '")
                    .append(name)
                    .appendLine("';")
                    .appendLine();

            node.getChildren().forEach(e -> {
                printNode(e, builder, indent + 1);
            });

        }

    }

    private void printArray(Array array, CodeBuilder builder, int indent) {
        KnowledgeTreeNode<XsNode> node = tree.get(array.getTargetPath());
        Construction construction = (Construction) node.getAnnotation(CONSTRUCTION_NAMESPACE);
        Construction parent = (Construction) node.getParent().getAnnotation(CONSTRUCTION_NAMESPACE);
        String name = node.getName();
        if (node.origin().getName().getNamespaceURI().trim().length() > 0) {
            name = "Abs:" + name;
        }

        builder.append("-- LOOP ", indent).append(array.getSourcePath()).append(" TO ").append(node.getPath()).appendLine();
        builder.append("DECLARE ", indent).append(array.getVariable()).append(" REFERENCE TO ").append(array.getEvaluation()).appendLine(";");
        builder.append(array.getName(), indent).append(" : WHILE LASTMOVE(").append(array.getVariable()).appendLine(") DO").appendLine();

        builder.append("-- ", indent + 1).appendLine(node.getPath());
        builder.append("DECLARE ", indent + 1)
                .append(construction.getVariable())
                .append(" REFERENCE TO ")
                .append(parent.getVariable()).appendLine(";");

        builder.append("CREATE LASTCHILD OF ", indent + 1)
                .append(parent.getVariable())
                .append(" AS ")
                .append(construction.getVariable())
                .append(" TYPE XMLNSC.Folder NAME '")
                .append(name)
                .appendLine("';")
                .appendLine();

        int level = level(node.getPath());

        array.getChildren().entrySet().forEach(e -> {
            String path = e.getKey();
            KnowledgeTreeNode<XsNode> childNode = tree.get(path);
            KnowledgeTreeNode<XsNode> parentNode = childNode.getParent();
            int diff = level(path) - level;

            String[] src = e.getValue();
            if (src.length == 0) {
                // Folder:
                String _name = childNode.getName();
                if (childNode.origin().getName().getNamespaceURI().trim().length() > 0) {
                    _name = "Abs:" + _name;
                }

                Construction sub = (Construction) childNode.getAnnotation(CONSTRUCTION_NAMESPACE);
                Construction sup = (Construction) parentNode.getAnnotation(CONSTRUCTION_NAMESPACE);
                builder.append("-- ", indent + diff + 1).appendLine(childNode.getPath());
                builder.append("DECLARE ", indent + diff + 1)
                        .append(sub.getVariable())
                        .append(" REFERENCE TO ")
                        .append(sup.getVariable()).appendLine(";");
                builder.append("CREATE LASTCHILD OF ", indent + diff + 1)
                        .append(sup.getVariable())
                        .append(" AS ")
                        .append(sub.getVariable())
                        .append(" TYPE XMLNSC.Folder NAME '")
                        .append(_name)
                        .appendLine("';")
                        .appendLine();

            } else {
                int index = 0;
                for (String v : src) {
                    if (!v.endsWith("[*]")) {
                        String _type = "(XMLNSC.Field)";
                        String _name = childNode.getName();

                        if (XsNode.XsNodeType.Attribute.equals(childNode.origin().getNodeType())) {
                            _type = "(XMLNSC.Attribute)";
                            if (_name.startsWith("@")) {
                                _name = _name.substring(1);
                            }
                        }

                        if (childNode.origin().getName().getNamespaceURI() != null && node.origin().getName().getNamespaceURI().trim().length() > 0) {
                            _type = _type + "Abs:";
                        }

                        String eval = v;
                        if (eval.contains("[*]")) {
                            String arrayMapping = eval.substring(0, eval.lastIndexOf("[*]") + 3);
                            Array mappedArray = arrayMap.get(arrayMapping);
                            eval = mappedArray.getVariable() + v.substring(mappedArray.getSourcePath().length());

                        } else if (eval.startsWith("$.")) {
                            eval = "_inputRootNode" + eval.substring(1);

                        } else if ("???".equals(eval)) {
                            eval = "'???'";
                        }

                        builder.append("-- ", indent + diff + 1).appendLine(childNode.getPath());
                        builder.append("SET ", indent + diff + 1)
                                .append(construction.getVariable()).append(".").append(_type).append(_name)
                                .append(" = ")
                                .append(eval).appendLine(";")
                                .appendLine();

                    } else if (arrayMap.containsKey(v)) {
                        Array subArray = arrayMap.get(v);
                        if (index > 0) {
                            subArray.addIndex(index);
                        }
                        printArray(subArray, builder, indent + diff + 1);

                    } else {
                        System.out.println("=============== " + v);

                    }

                    index++;
                }

            }
        });

        builder.append("MOVE ", indent).append(array.getVariable()).append(" NEXTSIBLING;").appendLine();
        builder.append("END WHILE ", indent).append(array.getName()).appendLine(";");
        builder.appendLine("-- END LOOP", indent).appendLine();

    }

    private static int level(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        return tokenizer.countTokens();
    }



/*
    private void printFunction(Function function, KnowledgeTreeNode<XsNode> node, StringBuilder builder, int indent) {
        if(FUNCTION_LOOP.equals(function.name)) {
            printLoopFunction(function, node, builder, indent);
        }
    }

    private void printLoopFunction(Function function, KnowledgeTreeNode<XsNode> node, StringBuilder builder, int indent) {

        Construction parentConstruction = ((KnowledgeTreeNode<XsNode>)node.getParent()).getAnnotation(NAMESPACE_CONSTRUCTION, Construction.class);
        Construction construction = node.getAnnotation(NAMESPACE_CONSTRUCTION, Construction.class);
        String loopName = function.parameters[0];
        String srcPath = function.parameters[1];
        String variable = function.parameters[2];

        String source = srcPath;
        if(source.endsWith("[*]")) {
            source = source.substring(0, source.length() - 3) + ".Item";
        }

        if(source.startsWith("$.")) {
            source = inputRootVariable + source.substring(1);
        }

        StringBuilderUtils.println("-- LOOP FROM " + srcPath + " TO " + node.getPath() + ":", builder, construction.getLevel() + indent);
        StringBuilderUtils.println("DECLARE " + variable + " REFERENCE TO " + source + ";", builder, construction.getLevel() + indent);
        StringBuilderUtils.println(loopName + " : WHILE LASTMOVE(" + variable + ") DO", builder, construction.getLevel() + indent);
        StringBuilderUtils.println(builder);

        int offset = 1;

        StringBuilderUtils.println("-- " + node.getPath(), builder, construction.getLevel() + indent + offset);
        StringBuilderUtils.println("DECLARE " + construction.getAlias() + " REFERENCE TO " + parentConstruction.getAlias() + ";", builder, construction.getLevel() + indent + offset);
        StringBuilderUtils.println("CREATE LASTCHILD OF " + parentConstruction.getAlias() + " AS " + construction.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, construction.getLevel() + indent + offset);
        StringBuilderUtils.println(builder);

        for (TreeNode child : node.getChildren()) {
            System.out.println("============== " + node.getPath());
            printNode((KnowledgeTreeNode<XsNode>) child, builder, indent + offset);
        }
 */
/*

        if (node.getAnnotation(CONDITION) != null) {
            String condition = node.getAnnotation(CONDITION, String.class);
            StringBuilderUtils.println("IF " + condition + " THEN", builder, node.getLevel() + indent + 1);
            StringBuilderUtils.println(builder);
            offset++;
        }


        StringBuilderUtils.println("-- " + node.getPath(), builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println(builder);

        for (XmlSchemaBase.MappingNode child : node.getChildren()) {
            printNode(child, builder, indent + offset);
        }

        if (node.getAnnotation(CONDITION) != null) {
            StringBuilderUtils.println("END IF;", builder, node.getLevel() + indent + 1);
            StringBuilderUtils.println(builder);
        }
*//*


        StringBuilderUtils.println("MOVE " + variable + " NEXTSIBLING;", builder, construction.getLevel() + indent);
        StringBuilderUtils.println("END WHILE " + loopName + ";", builder, construction.getLevel() + indent);
        StringBuilderUtils.println(builder);

    }
*/



/*

        if (node.getAnnotation(MAPPING) != null && node.getAnnotation(MAPPING, Mapping.class).assignment != null) {
            printAssignment(node, builder, indent);

        } else if (node.getAnnotation(CONSTRUCT) != null) {
            printConstruct(node, builder, indent);

        } else if (XmlSchemaBase.NodeType.Folder.equals(node.getNodeType())) {

            if (node.getAnnotation(CONDITION) != null) {
                String condition = node.getAnnotation(CONDITION, String.class);

                StringBuilderUtils.println("IF " + condition + " THEN", builder, node.getLevel() + indent);
                printSimpleFolder(node, builder, indent + 1);
                StringBuilderUtils.println("END IF;", builder, node.getLevel() + indent);
                StringBuilderUtils.println(builder);

            } else {
                printSimpleFolder(node, builder, indent);
            }
        }
*/
/*

    private void printSimpleFolder(KnowledgeTreeNode<XsNode> node, StringBuilder builder, int indent) {
        Construction construction = node.getAnnotation(NAMESPACE_CONSTRUCTION, Construction.class);
        if (node.getParent() != null) {
            Construction parentConstruction = ((KnowledgeTreeNode<XsNode>) node.getParent()).getAnnotation(NAMESPACE_CONSTRUCTION, Construction.class);
            StringBuilderUtils.println("-- " + node.getPath(), builder, construction.getLevel() + indent);
            StringBuilderUtils.println("DECLARE " + construction.getAlias() + " REFERENCE TO " + parentConstruction.getAlias() + ";", builder, construction.getLevel() + indent);
            StringBuilderUtils.println("CREATE LASTCHILD OF " + parentConstruction.getAlias() + " AS " + construction.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                    , builder, construction.getLevel() + indent);
            StringBuilderUtils.println(builder);
        }

        node.getChildren().forEach(n -> {
            printNode((KnowledgeTreeNode<XsNode>) n, builder, indent);
        });
    }

    private void printAssignment(KnowledgeTreeNode<XsNode> node, StringBuilder builder, int indent) {
        Assignment assignment = node.getAnnotation(NAMESPACE_ASSIGNMENT, Assignment.class);
        Construction construction = ((KnowledgeTreeNode<XsNode>)node.getParent()).getAnnotation(NAMESPACE_CONSTRUCTION, Construction.class);

        //String assignment = getAssignment(mapping, inputRootVariable);
        if (assignment != null) {
            StringBuilderUtils.println("-- " + node.getPath(), builder, construction.getLevel() + indent + 1);
            if(assignment.functions.size() == 1) {
                Function function = assignment.getFirst();
                StringBuilderUtils.println("SET " + construction.getAlias()
                        + ".(XMLNSC." + node.origin().getNodeType() + ")" + getFullName(node)
                        + " = " + getAssignment(function) + ";", builder, construction.getLevel() + indent + 1);
            }
            StringBuilderUtils.println(builder);
        }
    }

    private String getAssignment(Function function) {
        if(FUNCTION_DEFAULT.equalsIgnoreCase(function.name)) {
            return function.parameters[0];

        } else if(FUNCTION_ASSIGN.equalsIgnoreCase(function.name)) {
            String assignment = function.parameters[0];
            if(assignment.startsWith("$.")) {
                assignment = inputRootVariable + assignment.substring(1);
            }
            return assignment;

        } else if(FUNCTION_LOOP_ASSIGN.equals(function.name)) {
            System.out.println("==================== function: " + function.toString());
            return function.parameters[1];
        }

        return "XXX";
    }

    private String getFullName(KnowledgeTreeNode<XsNode> node) {
        String fullName = node.getName();
        if(fullName.startsWith("@")) {
            fullName = fullName.substring(1);
        }
        String uri = node.origin().getName().getNamespaceURI();
        if (uri != null && uri.equals(URI)) {
            fullName = "Abs:" + fullName;
        }

        return fullName;
    }
*/

/*
    private void printConstruct(XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {

        Construct construct = node.getAnnotation(CONSTRUCT, Construct.class);

        if (construct.procedure != null) {
            printProcedureCall(construct.procedure, node, builder, indent);

        } else {
            construct.loops.forEach(e -> {
                printLoop(e, node, builder, indent);
            });

            construct.constructors.forEach(e -> {
                printConstructor(e, node, builder, indent);

            });

        }
    }

    private void printLoop(WhileLoop loop, XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {

        constructions.put(loop.sourcePath, loop);

        constructNodeMap.put(loop.variable, loop);

        loop.parent = findParent(loop.sourcePath);

        StringBuilderUtils.println("-- LOOP FROM " + loop.sourcePath + " TO " + node.getPath() + ":", builder, node.getLevel() + indent);
        StringBuilderUtils.println("DECLARE " + loop.variable + " REFERENCE TO " + getAssignment(loop, inputRootVariable) + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println(loop.name + " : WHILE LASTMOVE(" + loop.variable + ") DO", builder, node.getLevel() + indent);
        StringBuilderUtils.println(builder);

        int offset = 1;
        if (node.getAnnotation(CONDITION) != null) {
            String condition = node.getAnnotation(CONDITION, String.class);
            StringBuilderUtils.println("IF " + condition + " THEN", builder, node.getLevel() + indent + 1);
            StringBuilderUtils.println(builder);
            offset++;
        }


        StringBuilderUtils.println("-- " + node.getPath(), builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, node.getLevel() + indent + offset);
        StringBuilderUtils.println(builder);

        for (XmlSchemaBase.MappingNode child : node.getChildren()) {
            printNode(child, builder, indent + offset);
        }

        if (node.getAnnotation(CONDITION) != null) {
            StringBuilderUtils.println("END IF;", builder, node.getLevel() + indent + 1);
            StringBuilderUtils.println(builder);
        }

        StringBuilderUtils.println("MOVE " + loop.variable + " NEXTSIBLING;", builder, node.getLevel() + indent);
        StringBuilderUtils.println("END WHILE " + loop.name + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println(builder);

    }

    private void printConstructor(Constructor constructor, XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {
        constructions.put(constructor.sourcePath, constructor);
        constructNodeMap.put(constructor.variable, constructor);
    }

    private void printProcedureCall(Procedure procedure, XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {

        procedures.add(procedure);

        StringBuilderUtils.println("-- " + node.getPath(), builder, node.getLevel() + indent);

        // FIXME:
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, node.getLevel() + indent);

        StringBuilderUtils.println(builder);
        StringBuilderUtils.println("CALL " + procedure.invocation() + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println(builder);

    }

    private void printProcedures(StringBuilder builder) {
        procedures.forEach(e -> {
            printProcedure(e, builder);
        });
    }

    private void printProcedure(Procedure procedure, StringBuilder builder) {

        StringBuilderUtils.println("CREATE PROCEDURE " + procedure.signature(), builder, 1);
        StringBuilderUtils.println("BEGIN", builder, 2);
        if (procedure.body != null) {
            StringBuilderUtils.println(decode(procedure.body), builder);
        }
        StringBuilderUtils.println(builder);
        StringBuilderUtils.println("END;", builder, 2);

        StringBuilderUtils.println(builder);

    }

    private void printConstructions(XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {
        Map<String, Constructor> constructionMap = new LinkedHashMap<>();
        String exp = node.getAnnotation(CONSTRUCTION, String.class);
        String[] definitions = exp.split(".end\\(\\)");
        for (int i = 0; i < definitions.length; i++) {
            Function[] functions = Function.fromString(definitions[i]);
            Constructor construction = createConstruction(functions);
            if (construction != null) {
                constructionMap.put(construction.name, construction);
            }
        }

        node.getChildren().forEach(c -> {
            sort(c, constructionMap);
        });

        List<Constructor> list = new ArrayList<>(constructionMap.values());
        for (int i = 0; i < list.size(); i++) {
            String suffix = i == 0 ? "" : "" + i;
            Constructor construction = list.get(i);
            printConstruction(construction, suffix, node, builder, indent);
        }

    }

    private void sort(XmlSchemaBase.MappingNode node, Map<String, Constructor> constructionMap) {
        if (node.getAnnotation(MAPPING) != null) {
            Mapping mapping = node.getAnnotation(MAPPING, Mapping.class);
            String assignment = mapping.assignment;
            if (assignment != null && assignment.startsWith("for(")) {
                String[] arr = assignment.split("end\\(\\)");
                for (String exp : arr) {
                    Function[] assignments = Function.fromString(exp);
                    String dest = assignments[0].getArguments()[0];
                    String assign = assignments[1].getArguments()[0];

                    if (constructionMap.containsKey(dest)) {
                        constructionMap.get(dest).assignments.put(node.getPath(), assign);
                    }
                }

            }
        }
    }

    private Constructor createConstruction(Function[] functions) {
        Constructor construction = null;


        return construction;
    }

    private void printConstruction(Constructor construction, String suffix, XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {

        StringBuilderUtils.println("-- Construct " + node.getName() + " FROM " + construction.sourcePath + ":", builder, node.getLevel() + indent);
        StringBuilderUtils.println("DECLARE " + construction.variable + " REFERENCE TO " + inputRootVariable + "." + construction.sourcePath.replaceAll("/", ".") + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println(builder);


        Map<String, XmlSchemaBase.MappingNode> map = new LinkedHashMap<>();
        Map<String, String> assignments = construction.assignments;
        assignments.entrySet().forEach(e -> {
            String path = e.getKey();
            List<String> paths = getAllPath(path, node.getPath());
            paths.forEach(p -> {
                map.put(p, base.get(p));
            });
        });

        map.entrySet().forEach(e -> {
            XmlSchemaBase.MappingNode n = e.getValue();

            StringBuilderUtils.println("-- " + n.getPath(), builder, n.getLevel() + indent);
            if (n.getNodeType().equals(XmlSchemaBase.NodeType.Folder)) {
                StringBuilderUtils.println("DECLARE " + n.getAlias() + suffix + " REFERENCE TO " + n.getParent().getAlias() + ";", builder, n.getLevel() + indent);
                StringBuilderUtils.println("CREATE LASTCHILD OF " + n.getParent().getAlias() + suffix + " AS " + n.getAlias() + suffix + " TYPE XMLNSC.Folder NAME '" + getFullName(n) + "';"
                        , builder, n.getLevel() + indent);

            } else if (n.getNodeType().equals(XmlSchemaBase.NodeType.Field)) {
                StringBuilderUtils.println("SET " + n.getParent().getAlias() + suffix + ".(XMLNSC.Field)" + getFullName(n) + " = " + assignments.get(n.getPath()) + ";", builder, n.getLevel() + indent);

            } else if (n.getNodeType().equals(XmlSchemaBase.NodeType.Attribute)) {
                StringBuilderUtils.println("SET " + n.getParent().getAlias() + suffix + ".(XMLNSC.Attribute)" + getFullName(n) + " = " + assignments.get(n.getPath()) + ";", builder, n.getLevel() + indent);

            }
            StringBuilderUtils.println(builder);
        });

        StringBuilderUtils.println(builder);
    }

    private void printBlock(XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {

        StringBuilderUtils.println("-- " + node.getPath(), builder, node.getLevel() + indent);
        StringBuilderUtils.println("DECLARE " + node.getAlias() + " REFERENCE TO " + node.getParent().getAlias() + ";", builder, node.getLevel() + indent);
        StringBuilderUtils.println("CREATE LASTCHILD OF " + node.getParent().getAlias() + " AS " + node.getAlias() + " TYPE XMLNSC.Folder NAME '" + getFullName(node) + "';"
                , builder, node.getLevel() + indent);
        StringBuilderUtils.println(builder);

        String[] lines = node.getAnnotation(BLOCK, String[].class);
        for (String line : lines) {
            String ln = line.trim();
            int offset = 0;
            while (ln.startsWith("+")) {
                ln = ln.substring(1).trim();
                offset++;
            }

            if (ln.equals("")) {
                StringBuilderUtils.println(builder);
            } else {
                StringBuilderUtils.println(ln, builder, node.getLevel() + indent + offset);
            }
        }
        StringBuilderUtils.println(builder);

    }

    private void printAssignment(XmlSchemaBase.MappingNode node, StringBuilder builder, int indent) {
        Mapping mapping = node.getAnnotation(MAPPING, Mapping.class);
        String assignment = getAssignment(mapping, inputRootVariable);

        if (assignment != null) {

            StringBuilderUtils.println("-- " + node.getPath(), builder, node.getLevel() + indent);
            if (XmlSchemaBase.NodeType.Attribute.equals(node.getNodeType())) {
                // Attribute:
                StringBuilderUtils.println("SET " + node.getParent().getAlias() + ".(XMLNSC.Attribute)" + getFullName(node) + " = " + assignment + ";", builder, node.getLevel() + indent);
            } else {
                // Field:
                StringBuilderUtils.println("SET " + node.getParent().getAlias() + ".(XMLNSC.Field)" + getFullName(node) + " = " + assignment + ";", builder, node.getLevel() + indent);
            }

            StringBuilderUtils.println(builder);
        }
    }

    private WhileLoop findParent(String path) {
        String token = path;
        int index = token.lastIndexOf('/');
        while (index > 0) {
            token = token.substring(0, index);
            if (constructions.containsKey(token)) {
                return (WhileLoop) constructions.get(token);
            }
            index = token.lastIndexOf('/');
        }

        return null;
    }

    private String getFullName(XmlSchemaBase.MappingNode node) {
        String fullName = node.getName();
        if (node.getNamespaceURI() != null && node.getNamespaceURI().equals(URI)) {
            fullName = "Abs:" + fullName;
        }

        return fullName;
    }

    private String getAssignment(Mapping mapping, String inputRootVariable) {
        if (mapping == null) {
            return null;
        }

        String assignment = "'???'";
        if (mapping.assignment != null) {
            assignment = mapping.assignment;
            if (assignment.contains(INPUT_ROOT + ".")) {
                assignment = assignment.replace(INPUT_ROOT + ".", inputRootVariable + ".");
            }

        } else if (mapping.sourcePath != null) {
            String path = mapping.sourcePath;
            assignment = inputRootVariable + "." + path.replaceAll("/", "\\.");
        }

        return assignment;
    }

    private String getAssignment(WhileLoop wl, String inputRoot) {
        if (wl.parent == null) {
            return inputRoot + "." + wl.sourcePath.replace("[*]", "/Item").replaceAll("/", "\\.");

        } else {
            WhileLoop parent = wl.parent;
            String path = wl.sourcePath.substring(parent.sourcePath.length() + 1);
            return wl.parent.variable + "." + path.replace("[*]", "/Item").replaceAll("/", "\\.");

        }

    }
*/


}
