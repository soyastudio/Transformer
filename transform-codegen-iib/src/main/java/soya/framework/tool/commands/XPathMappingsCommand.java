package soya.framework.tool.commands;

import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.xs.XsNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.*;

public abstract class XPathMappingsCommand extends BusinessObjectCommand {

    protected Map<String, Mapping> mappings = new LinkedHashMap<>();

    @Override
    protected String execute() throws Exception {
        load(getFile());
        annotate();

        return render(mappings);
    }

    protected abstract File getFile();

    protected void annotate() {
    }

    protected void load(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            if (line.length() > 0 && line.contains("=")) {
                String key = line.substring(0, line.indexOf("="));
                String value = line.substring(line.indexOf("=") + 1);

                Mapping mapping = new Mapping(value);

                mappings.put(key, mapping);
            }
            line = reader.readLine();
        }
    }

    protected abstract String render(Map<String, Mapping> mappings);

    static class Mapping {

        String type;
        String cardinality;
        String rule;
        String source;
        String version;

        boolean todo;
        String assign;

        int arrayDepth;

        Construction construction;
        Assignment assignment;

        MappingError error;

        public Mapping(String exp) {
            String[] arr = exp.split("::");
            Function[] functions = new Function[arr.length];
            for (int i = 0; i < arr.length; i++) {
                functions[i] = new Function(arr[i]);
            }
            set(functions);
        }

        public Mapping(Function[] functions) {
            set(functions);
        }

        public boolean isArray() {
            return cardinality != null && !cardinality.trim().endsWith("-1");
        }

        public boolean isOptional() {
            return cardinality != null && cardinality.startsWith("0-");
        }

        public void set(Function[] functions) {
            for (Function func : functions) {
                try {
                    Field field = Mapping.class.getDeclaredField(func.name);
                    field.setAccessible(true);
                    field.set(this, func.parameters[0]);

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        public String toConstruct() {
            StringBuilder builder = new StringBuilder();
            if (construction != null) {
                builder.append(construction);
            } else if (assignment != null) {
                builder.append("assign(").append(assignment.value()).append(")");
            } else if (error != null) {
                builder.append("value(").append(error.value).append(")")
                        .append("::").append("error(").append(error.description).append(")");
            }

            return builder.toString();
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("type(").append(type).append(")")
                    .append("::").append("cardinality(").append(cardinality).append(")");
            if (assign != null) {
                builder.append("::").append("assign(").append(assign).append(")");
            }

            if (rule != null) {
                builder.append("::").append("rule(").append(rule).append(")");
            }

            if (source != null) {
                builder.append("::").append("source(").append(source).append(")");
            }

            if (version != null) {
                builder.append("::").append("version(").append(version).append(")");
            }

            if (error != null) {
                builder.append("::").append("error(").append(error.description).append(")");
            }

            return builder.toString();
        }

    }

    static class Adjustment {

        private String path;
        private String type;
        private String cardinality;
        private String rule;
        private String assign;

        public Adjustment(Function[] functions) {
            if (functions == null || functions.length == 0) {
                throw new IllegalArgumentException("functions cannot be null or empty.");
            }

            for (Function func : functions) {
                try {
                    Field field = Adjustment.class.getDeclaredField(func.name);
                    field.setAccessible(true);
                    field.set(this, func.parameters[0]);

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }


        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (path != null) {
                builder.append("::").append("path(").append(path).append(")");
            }

            if (type != null) {
                builder.append("::").append("type(").append(type).append(")");
            }

            if (cardinality != null) {
                builder.append("::").append("cardinality(").append(cardinality).append(")");
            }

            if (rule != null) {
                builder.append("::").append("rule(").append(rule).append(")");
            }

            if (assign != null) {
                builder.append("::").append("assign(").append(assign).append(")");
            }

            String result = builder.toString();
            if (result.startsWith("::")) {
                result = result.substring(2);
            }

            return result;
        }
    }

    static class Construction {
        private Map<String, Array> arrays = new LinkedHashMap<>();

        public Array getArray(String path) {
            return arrays.get(path);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("construct()");
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
        private String variable;
        private String value;

        public String value() {
            return variable == null ? value : variable + "." + value;
        }
    }

    private static class Function {
        private final String name;
        private final String[] parameters;

        public Function(String exp) {
            int first = exp.indexOf('(');
            int last = exp.indexOf(')');

            if (first > 0 && last > 0 && first < last) {
                name = exp.substring(0, first);
                parameters = exp.substring(first + 1, last).split(",");
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = parameters[i].trim();
                }
            } else {
                throw new IllegalArgumentException("Expression is not a function format: " + exp);
            }
        }

        public Function(String name, String[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public String[] getParameters() {
            return parameters;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder(name).append("(");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }

                builder.append(parameters[i]);
            }

            builder.append(")");

            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Function)) return false;

            Function function = (Function) o;

            return this.toString().equals(o.toString());
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }
    }

    private static class MappingError {
        private final String path;
        private final String value;

        private String description;

        public MappingError(String path, String value) {
            this.path = path;
            this.value = value;

        }

        public MappingError(String path, String value, String description) {
            this.path = path;
            this.value = value;
            this.description = description;
        }
    }

    static class MappingException extends Exception {
        private List<MappingError> errors = new ArrayList<>();

        public void add(String path, String value) {
            errors.add(new MappingError(path, value));
        }

        public void add(String path, String value, String desc) {
            errors.add(new MappingError(path, value, desc));
        }

        public void add(MappingError error) {
            errors.add(error);
        }

        public void add(MappingException ex) {
            errors.addAll(ex.errors);
        }

        public boolean hasError() {
            return errors.size() > 0;
        }
    }


}
