package soya.framework.tool.commands;

import soya.framework.commons.cli.CommandOption;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

public abstract class ConstructCommand extends XPathMappingsCommand {

    public static final String ASSIGNMENT_NAMESPACE = "ASSIGN";
    public static final String CONSTRUCTION_NAMESPACE = "CONSTRUCT";


    @CommandOption(option = "c", longOption = "construct", required = true)
    protected String construction;

    @Override
    protected File getFile() {
        return new File(workDir, construction);
    }

    @Override
    protected void annotate() {
        mappings.entrySet().forEach(e -> {
            String path = e.getKey();
            if (!path.startsWith("#")) {
                Mapping mapping = e.getValue();
                KnowledgeTreeNode<XsNode> node = tree.get(path);
                if (node == null) {
                    System.out.println("Cannot find node with path: " + path);

                } else if (mapping.rule != null) {
                    Assignment assignment = new Assignment(mapping);
                    node.annotate(ASSIGNMENT_NAMESPACE, assignment);
                    if (assignment.getValue() != null && assignment.isInArray()) {
                        annotateArrayParent(assignment, node);
                    } else {
                        annotateParent(node);
                    }
                }
            }
        });
    }

    protected void annotateParent(KnowledgeTreeNode<XsNode> node) {
        if (node != null) {
            KnowledgeTreeNode<XsNode> parent = node.getParent();
            while (parent != null) {
                if (parent.getAnnotation(CONSTRUCTION_NAMESPACE) == null) {
                    Construction construction = createConstruction(parent);
                    parent.annotate(CONSTRUCTION_NAMESPACE, construction);
                    annotateParent(parent);

                } else {
                    break;
                }
            }
        }
    }

    protected void annotateArrayParent(Assignment assignment, KnowledgeTreeNode<XsNode> node) {
        if (node != null) {
            KnowledgeTreeNode<XsNode> parent = node.getParent();

            while (parent != null) {
                if (parent.getAnnotation(CONSTRUCTION_NAMESPACE) == null) {
                    Construction construction = createConstruction(parent);
                    parent.annotate(CONSTRUCTION_NAMESPACE, construction);
                    if(construction.isArray) {
                        System.out.println("============ " + assignment.getUri() + " -> " + parent.getPath());
                    }
                    annotateArrayParent(assignment, parent);

                } else {
                    break;
                }
            }
        }
    }

    private Construction createConstruction(KnowledgeTreeNode<XsNode> node) {
        Construction construction = new Construction();
        construction.var = node.origin().getName().getLocalPart() + "_";
        if(!BigInteger.ONE.equals(node.origin().getMaxOccurs())) {
            construction.isArray = true;
        }

        return construction;
    }

    static class Construction {
        private String var;
        private boolean isArray;

        public String getVariable() {
            return var;
        }

        public boolean isArray() {
            return isArray;
        }

        private Map<String, Array> arrays = new LinkedHashMap<>();

        public Array getArray(String path) {
            return arrays.get(path);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("construct(").append(var).append(")");


            arrays.entrySet().forEach(e -> {
                builder.append("::").append(e.getValue().toString());
            });
            return builder.toString();
        }
    }

    static class Array {
        private static int count;
        private static Set<String> vars = new HashSet<>();
        private static Map<ArrayKey, Array> created = new LinkedHashMap<>();

        private final String id;
        private final String targetPath;
        private final String sourcePath;

        private String variable;
        private Set<String> children = new LinkedHashSet<>();

        private transient Array parent;
        private transient Set<KnowledgeTreeNode<XsNode>> childNodes = new LinkedHashSet();

        private Array(ArrayKey arrayKey) {
            this.sourcePath = arrayKey.sourcePath;
            this.targetPath = arrayKey.targetPath;

            count++;
            this.id = "array" + count;
            this.variable = "_" + id;
        }

        public void addChild(KnowledgeTreeNode<XsNode> node) {
            String path = node.getPath();
            if (targetPath.length() < path.length() && path.startsWith(targetPath)) {
                List<KnowledgeTreeNode<XsNode>> list = new ArrayList<>();
                list.add(node);

                KnowledgeTreeNode<XsNode> parent = node.getParent();
                while (!targetPath.equals(parent.getPath())) {
                    list.add(0, parent);
                    parent = parent.getParent();
                }

                list.forEach(e -> {
                    if (!children.contains(e.getPath())) {
                        children.add(e.getPath());
                        childNodes.add(e);
                    }
                });
            }
        }

        @Override
        public String toString() {
            if (parent == null) {
                return id + "(" + sourcePath + ")";

            } else {
                String token = parent.variable + sourcePath.substring(parent.sourcePath.length());
                return id + "(" + token + ")";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Array)) return false;

            Array array = (Array) o;

            if (!sourcePath.equals(array.sourcePath)) return false;
            return targetPath.equals(array.targetPath);
        }

        @Override
        public int hashCode() {
            int result = sourcePath.hashCode();
            result = 31 * result + targetPath.hashCode();
            return result;
        }
    }

    static class ArrayKey {

        private final String targetPath;
        private final String sourcePath;

        public ArrayKey(String targetPath, String sourcePath) {
            this.targetPath = targetPath;
            this.sourcePath = sourcePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArrayKey)) return false;

            ArrayKey arrayKey = (ArrayKey) o;
            return sourcePath.equals(arrayKey.sourcePath) && targetPath.equals(arrayKey.targetPath);
        }

        @Override
        public int hashCode() {
            int result = targetPath != null ? targetPath.hashCode() : 0;
            result = 31 * result + (sourcePath != null ? sourcePath.hashCode() : 0);
            return result;
        }
    }

    static class Assignment {

        private String uri;
        private String variable;
        private String value;
        private boolean inArray;

        public Assignment(Mapping mapping) {
            String rule = mapping.rule.toUpperCase(Locale.ROOT);
            if (rule.contains("DEFAULT")) {
                String value = mapping.rule;
                if (value.contains("'")) {
                    this.value = value.substring(value.indexOf("'"), value.lastIndexOf("'") + 1);
                } else {
                    this.value = "???";
                }

            } else if (rule.contains("DIRECT")) {
                this.value = mapping.source;
                if (mapping.source.contains("[*]")) {
                    inArray = true;
                    String token = mapping.source;
                    int index = token.lastIndexOf("[*]");

                    String var = token.substring(0, index);
                    if (var.contains("/")) {
                        var = var.substring(var.lastIndexOf("/") + 1);
                    }
                    this.variable = "_" + var;
                    this.uri = token.substring(0, index + 3);
                    this.value = token.substring(index + 4).replaceAll("/", ".");
                }


            } else {
                this.value = "???";
            }


        }

        public boolean isInArray() {
            return inArray;
        }

        public String getUri() {
            return uri;
        }

        public String getVariable() {
            return variable;
        }

        public String getValue() {
            return value;
        }

        public String value() {
            return variable == null ? value : variable + "." + value;
        }

        public String toString() {
            return "assign(" + value() + ")";
        }
    }
}
