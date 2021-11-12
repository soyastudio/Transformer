package soya.framework.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.CommandLines;
import soya.framework.commons.poi.XlsxUtils;
import soya.framework.commons.util.CodeBuilder;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.KnowledgeTreeNode;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.xs.XmlBeansUtils;
import soya.framework.transform.schema.xs.XsKnowledgeBase;
import soya.framework.transform.schema.xs.XsNode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class MappingCommandLines {

    public static void main(String[] args) {

        String x = "C:/github/Workshop/AppBuild/CMM/BOD/GetGroceryOrder.xsd";
        String xlsx = "C:/github/Workshop/AppBuild/BusinessObjects/GroceryOrder/work/xpath-mappings.xlsx";

        String j = "C:/github/Workshop/AppBuild/BusinessObjects/GroceryOrder/work/xpath-adjustment.properties";
        String m = "C:/github/Workshop/AppBuild/BusinessObjects/GroceryOrder/work/xpath-mappings.properties";
        String n = "com.abs.ocrp.AirMilePoints.AirMilePoints_Details_Transformer_Compute";

        //String esql = "C:/github/Workshop/AppBuild/BusinessObjects/GroceryOrder/work/ESED_GroceryOrder_CMM_Transformer_Compute.esql";
        String esql = "C:/github/Workshop/AppBuild/BusinessObjects/GroceryOrder/work/ESED_GEN.esql";

        String SCHEMA = new StringBuilder("-a schema").append(" -x ").append(x).toString();

        String MAPPING = new StringBuilder("-a mapping")
                .append(" -m ").append(xlsx)
                //.append(" -j ").append(j)
                //.append(" -v V1.14.1")
                .toString();

        String VALIDATE = new StringBuilder("-a validate").append(" -x ").append(x)
                .append(" -m ").append(xlsx)
                .toString();

        String CONSTRUCT = new StringBuilder("-a construct").append(" -x ").append(x)
                .append(" -j ").append(j)
                .append(" -m ").append(xlsx)
                .toString();

        String ESQL = new StringBuilder("-a esql")
                .append(" -x ").append(x)
                .append(" -j ").append(j)
                .append(" -m ").append(xlsx)
                .append(" -n ").append(n)
                .toString();

        String VALIDATE_ESQL = new StringBuilder("-a validateEsql")
                .append(" -f ").append(esql)
                .append(" -x ").append(x)
                .append(" -j ").append(j)
                .append(" -m ").append(xlsx)
                .toString();


        String cmd = VALIDATE_ESQL;

        try {
            String result = CommandLines.execute(cmd, MappingCommandLines.class, null);
            System.out.println(result);

            System.exit(0);

        } catch (InvocationTargetException ite) {
            Throwable ex = ite.getTargetException();
            if (ex instanceof MappingException) {
                System.out.println(CommandLines.GSON.toJson(ex));

            } else {
                ex.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String schema(CommandLine cmd) throws MappingException {
        MappingException exception = new MappingException();
        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        new Renderer() {
            @Override
            public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {

                codeBuilder.append(node.getPath()).append("=").appendLine(getMapping(node).toString());
                node.getChildren().forEach(e -> {
                    render(e, codeBuilder);
                });
            }
        }.render(knowledgeTree.root(), codeBuilder);

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String avsc(CommandLine cmd) throws MappingException {
        File file = new File(cmd.getOptionValue("x"));
        return XsdToAvsc.fromXmlSchema(file).toString(true);
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "s",
                            desc = "Sheet name of xlsx file."),
                    @CommandLines.Opt(option = "v",
                            desc = "Version to render.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String mapping(CommandLine cmd) throws Exception {

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String key = e.getKey();
            Mapping mapping = e.getValue();

            if (!cmd.hasOption("v") || cmd.getOptionValue("v").equals(mapping.version)) {
                codeBuilder.append(key).append("=").appendLine(mapping.toString());
            }


        });


        String output = codeBuilder.toString();
        if (cmd.hasOption("o")) {
            write(output, cmd.getOptionValue("o"));
        }

        return output;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String unknownPaths(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String key = e.getKey();
            if (knowledgeTree.get(key) == null) {

                String guess = guessPath(key, knowledgeTree);
                if (guess == null) {
                    guess = "???";
                }

                codeBuilder.append(key).append("=").appendLine(guess);
            }
        });

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            required = true,
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "s",
                            desc = "Sheet name of xlsx file."),
                    @CommandLines.Opt(option = "v",
                            desc = "Version to render.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String adjust(CommandLine cmd) throws Exception {

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        File adjustmentFile = new File(cmd.getOptionValue("j"));
        mappings = adjust(mappings, adjustmentFile);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        mappings.entrySet().forEach(e -> {
            String key = e.getKey();
            Mapping mapping = e.getValue();

            if (!cmd.hasOption("v") || cmd.getOptionValue("v").equals(mapping.version)) {
                codeBuilder.append(key).append("=").appendLine(mapping.toString());
            }


        });

        String output = codeBuilder.toString();
        if (cmd.hasOption("o")) {
            write(output, cmd.getOptionValue("o"));
        }

        return output;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String validate(CommandLine cmd) throws Exception {

        Map<String, Adjustment> adjustments = new LinkedHashMap<>();
        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        mappings.entrySet().forEach(e -> {
            String key = e.getKey();
            Mapping mapping = e.getValue();

            List<Function> functions = new ArrayList<>();

            if (knowledgeTree.get(key) == null) {

                String guess = guessPath(key, knowledgeTree);
                if (guess == null) {
                    guess = "???";
                }

                functions.add(new Function("path", new String[]{guess}));

            } else {
                Mapping cmm = getMapping(knowledgeTree.get(key));
                if (!cmm.type.equals(mapping.type)) {
                    functions.add(new Function("type", new String[]{cmm.type}));
                }

                if (!cmm.cardinality.equals(mapping.cardinality)) {
                    functions.add(new Function("cardinality", new String[]{cmm.cardinality}));
                }

            }

            if (!functions.isEmpty()) {
                adjustments.put(key, new Adjustment(functions.toArray(new Function[functions.size()])));
            }
        });

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        adjustments.entrySet().forEach(e -> {
            codeBuilder.append(e.getKey()).append("=").appendLine(e.getValue().toString());
        });

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String arrays(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        return CommandLines.GSON.toJson(Array.created.values());
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String construct(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        new ConstructRenderer().render(knowledgeTree.root(), codeBuilder);

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "n",
                            required = true,
                            desc = "Full name of the esql module."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String esql(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        new EsqlRenderer(cmd.getOptionValue("n")).render(knowledgeTree.root(), codeBuilder);

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "n",
                            required = true,
                            desc = "Full name of the esql module."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String generateEsql(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        new EsqlRenderer(cmd.getOptionValue("n")).render(knowledgeTree.root(), codeBuilder);

        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "f",
                            required = true,
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "j",
                            desc = "Adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String validateEsql(CommandLine cmd) throws Exception {

        Set<String> set = new LinkedHashSet<>();
        try (Stream<String> lines = Files.lines(Paths.get(cmd.getOptionValue("f")), Charset.defaultCharset())) {
            lines.forEachOrdered(line -> {
                String token = line.trim();
                if (token.startsWith("-- ")) {
                    token = token.substring(3);
                    if (token.contains("/") && !token.contains(" ")) {
                        set.add(token);
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);
        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        Iterator<String> iterator = knowledgeTree.paths();
        while (iterator.hasNext()) {
            String path = iterator.next();
            KnowledgeTreeNode<XsNode> node = knowledgeTree.get(path);
            Mapping mapping = getMapping(node);
            if (mapping.construction != null || mapping.assignment != null) {
                if (path.contains("/") && !set.contains(path)) {
                    codeBuilder.append(path).append("=").appendLine(mapping.toString());

                }
            }
        }
        return codeBuilder.toString();
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            required = true,
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "s",
                            desc = "Sheet name of xlsx file."),
                    @CommandLines.Opt(option = "x",
                            desc = "Version to render.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String xpathJsonType(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        Iterator<String> iterator = knowledgeTree.paths();
        while (iterator.hasNext()) {
            String path = iterator.next();
            KnowledgeTreeNode<XsNode> node = knowledgeTree.get(path);
            Mapping mapping = getMapping(node);

            if (mapping.construction != null || mapping.assignment != null) {
                String dataType = mapping.type;
                String cardinality = mapping.cardinality;

                String type = null;
                if (!cardinality.endsWith("-1")) {
                    type = "array";
                    if (!"complex".equals(dataType)) {
                        type = simpleTypeConvert(dataType) + "_array";
                    }

                } else {
                    type = simpleTypeConvert(dataType);
                }

                if (!"string".equals(type)) {
                    codeBuilder.append(path).append("=").appendLine(type);
                }
            }
        }

        String output = codeBuilder.toString();
        if (cmd.hasOption("o")) {
            write(output, cmd.getOptionValue("o"));
        }

        return output;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "j",
                            required = true,
                            desc = "Path of mapping adjustment file."),
                    @CommandLines.Opt(option = "m",
                            required = true,
                            desc = "Mapping file path."),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "s",
                            desc = "Sheet name of xlsx file."),
                    @CommandLines.Opt(option = "x",
                            desc = "Version to render.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String xpathJsonTypeFunctions(CommandLine cmd) throws Exception {

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = createKnowledgeTree(cmd);

        File file = new File(cmd.getOptionValue("m"));
        String sheet = cmd.hasOption("s") ? cmd.getOptionValue("s") : null;
        Map<String, Mapping> mappings = load(file, sheet);

        if (cmd.hasOption("j")) {
            mappings = adjust(mappings, new File(cmd.getOptionValue("j")));
        }

        annotateMappings(knowledgeTree, mappings);

        CodeBuilder codeBuilder = CodeBuilder.newInstance();
        Iterator<String> iterator = knowledgeTree.paths();
        while (iterator.hasNext()) {
            String path = iterator.next();
            KnowledgeTreeNode<XsNode> node = knowledgeTree.get(path);
            Mapping mapping = getMapping(node);

            if (mapping.construction != null || mapping.assignment != null) {
                String dataType = mapping.type;
                String cardinality = mapping.cardinality;

                String type = null;
                if (!cardinality.endsWith("-1")) {
                    type = "array";
                    if (!"complex".equals(dataType)) {
                        type = simpleTypeConvert(dataType) + "_array";
                    }

                } else {
                    type = simpleTypeConvert(dataType);
                }

                if (!"string".equals(type)) {
                    codeBuilder.append(type).append("(").append(path).append(");");
                }
            }
        }

        String output = codeBuilder.toString();
        if (cmd.hasOption("o")) {
            write(output, cmd.getOptionValue("o"));
        }

        return output;
    }

    private static String simpleTypeConvert(String type) {
        switch (type) {
            case "boolean":
                return "boolean";

            case "float":
            case "double":
            case "decimal":
            case "int":
            case "integer":
            case "long":
            case "short":
            case "byte":
            case "nonPositiveInteger":
            case "NegativeInteger":
            case "nonNegativeInteger":
            case "positiveInteger":
            case "unsignedLong":
            case "unsignedInt":
            case "unsignedShort":
            case "unsignedByte":
                return "number";

            default:
                return "string";
        }
    }

    private static void annotateMappings(KnowledgeTree<SchemaTypeSystem, XsNode> tree, Map<String, Mapping> mappings) throws MappingException {
        mappings.entrySet().forEach(e -> {
            String xpath = e.getKey();
            Mapping annotation = e.getValue();

            KnowledgeTreeNode<XsNode> node = tree.get(xpath);
            if (node != null) {
                Mapping mapping = getMapping(node);
                mapping.set(parseExpression(annotation.toString()));

                if (mapping.assign != null || mapping.rule != null || mapping.source != null) {
                    bubble(node);
                    annotateAssignment(node);
                }
            }
        });

        Array.created.entrySet().forEach(e -> {

            String xpath = e.getKey().targetPath;
            Array array = e.getValue();

            KnowledgeTreeNode<XsNode> node = tree.get(xpath);
            getMapping(node).construction.arrays.put(e.getKey().sourcePath, array);
            if (array.parent != null) {
                array.parent.addChild(node);
            }
        });
    }

    private static void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
        codeBuilder.append(node.getPath()).append("=").appendLine(getMapping(node).toString());
        //codeBuilder.append(node.getPath()).append("=").appendLine(getMapping(node).toConstruct());
        node.getChildren().forEach(e -> {
            render(e, codeBuilder);
        });

    }

    private static void render2(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
        int level = node.getPath().split("/").length;

        if (node.getAnnotation("construct") != null) {
            Construction construction = node.getAnnotation("construct", Construction.class);
            codeBuilder.append(node.getPath() + "=")
                    .appendLine(construction.toString());

            node.getChildren().forEach(e -> {
                render(e, codeBuilder);
            });

        } else if (node.getAnnotation("assign") != null) {
            Assignment assignment = node.getAnnotation("assign", Assignment.class);
            codeBuilder.append(node.getPath() + "=")
                    .appendLine("assign(" + assignment.value() + ")");
        }
    }

    private static Map<String, Mapping> load(File xlsx, String sheetName) throws IOException {
        String startToken = "#";
        String[] columnNames = {"Target", "DataType", "Cardinality", "Mapping", "Source", "Version"};
        List<Map<String, String>> result = XlsxUtils.extract(xlsx, sheetName, startToken, columnNames);

        Map<String, Mapping> mappings = new LinkedHashMap<>();
        result.forEach(e -> {
            String key = e.get("Target");
            String exp = toExpression(e);

            Mapping mapping = new Mapping(parseExpression(exp));
            mappings.put(key, mapping);
        });

        return mappings;
    }

    private static Map<String, Mapping> adjust(Map<String, Mapping> mappings, File adjFile) {
        if (!adjFile.exists()) {
            return mappings;

        } else {
            Map<String, Mapping> adjusted = new LinkedHashMap<>();
            Map<String, Adjustment> adjustments = new LinkedHashMap<>();

            try (Stream<String> lines = Files.lines(Paths.get(adjFile.getPath()), Charset.defaultCharset())) {
                lines.forEachOrdered(line -> {
                    if (!line.startsWith("#")) {
                        int index = line.indexOf('=');
                        if (index > 0) {
                            String key = line.substring(0, index).trim();
                            String exp = line.substring(index + 1).trim();
                            adjustments.put(key, new Adjustment(parseExpression(exp)));

                        }
                    } else {
                        String token = line.substring(1).trim();
                        int index = token.indexOf('=');
                        token = token.substring(0, index).trim();
                        Function func = new Function("path", new String[]{"!!!"});
                        adjustments.put(token, new Adjustment(new Function[]{func}));
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mappings.entrySet().forEach(e -> {
                String key = e.getKey();
                Mapping mapping = e.getValue();
                if (!adjustments.containsKey(key)) {
                    adjusted.put(key, mapping);

                } else {
                    Adjustment adjustment = adjustments.get(key);
                    if ("!!!".equals(adjustment.path)) {
                        // removed

                    } else {
                        if (adjustment.type != null) {
                            mapping.type = adjustment.type;
                        }

                        if (adjustment.cardinality != null) {
                            mapping.cardinality = adjustment.cardinality;
                        }

                        if (adjustment.rule != null) {
                            mapping.rule = adjustment.rule;
                        }

                        if (adjustment.assign != null) {
                            mapping.assign = adjustment.assign;
                        }

                        if (adjustment.path != null && !"???".equals(adjustment.path)) {
                            adjusted.put(adjustment.path, mapping);

                        } else {
                            adjusted.put(key, mapping);
                        }
                    }
                }
            });

            return adjusted;
        }
    }

    private static String guessPath(String path, KnowledgeTree tree) {
        int index = path.lastIndexOf("/");
        String token = path.substring(0, index + 1) + "@" + path.substring(index + 1);
        if (tree.contains(token)) {
            return token;
        }

        Iterator<String> iterator = tree.paths();
        while (iterator.hasNext()) {
            String p = iterator.next();
            if (p.equalsIgnoreCase(path)) {
                return p;
            }
        }

        return null;
    }

    private static String toExpression(Map<String, String> value) {
        StringBuilder builder = new StringBuilder();
        builder.append("type(").append(getType(value)).append(")")
                .append("::").append("cardinality(").append(value.containsKey("Cardinality") ? value.get("Cardinality") : "???").append(")");

        if (value.containsKey("Mapping") && value.get("Mapping").trim().length() > 0) {
            builder.append("::").append("rule(").append(value.get("Mapping")).append(")");
        }

        if (value.containsKey("Source") && value.get("Source").trim().length() > 0) {
            builder.append("::").append("source(").append(getSource(value)).append(")");
        }

        if (value.containsKey("Version") && value.get("Version").trim().length() > 0) {
            builder.append("::").append("version(").append(value.get("Version").toUpperCase()).append(")");
        }

        return builder.toString();
    }

    private static String getType(Map<String, String> values) {
        String v = values.containsKey("DataType") ? values.get("DataType") : "???";
        v = v.trim();
        if (v.contains("(")) {
            v = v.substring(0, v.indexOf("(")).trim();
        }

        if (v.contains(" ")) {
            v = v.substring(0, v.indexOf(" "));
        }

        return v;
    }

    private static String getSource(Map<String, String> values) {
        StringBuilder builder = new StringBuilder();
        boolean boo = false;
        StringTokenizer tokenizer = new StringTokenizer(values.get("Source"));
        while (tokenizer.hasMoreTokens()) {
            if (boo) {
                builder.append(" ");
            }
            builder.append(tokenizer.nextToken());

            boo = true;
        }

        return builder.toString();
    }

    private static void write(String contents, String o) throws IOException {
        File output = new File(o);
        FileUtils.write(output, contents, Charset.defaultCharset());
    }

    private static KnowledgeTree<SchemaTypeSystem, XsNode> createKnowledgeTree(CommandLine cmd) {
        File xsd = new File(cmd.getOptionValue("x"));
        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = XsKnowledgeBase.builder()
                .file(xsd)
                .create().knowledge();
        initMappings(knowledgeTree.root());

        if (cmd.hasOption("j")) {
            adjustMappings(cmd.getOptionValue("j"), knowledgeTree);
        }

        return knowledgeTree;
    }

    private static void initMappings(KnowledgeTreeNode<XsNode> node) {
        node.annotate("mapping", new Mapping(node.origin()));
        node.getChildren().forEach(e -> {
            initMappings(e);
        });
    }

    private static void adjustMappings(String adjFilePath, KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree) {
        try (Stream<String> lines = Files.lines(Paths.get(adjFilePath), Charset.defaultCharset())) {
            lines.forEachOrdered(line -> {
                if (line != null) {
                    int index = line.indexOf('=');
                    if (index > 0) {
                        String key = line.substring(0, index).trim();
                        String exp = line.substring(index + 1).trim();

                        KnowledgeTreeNode<XsNode> node = knowledgeTree.get(key);
                        if (node != null) {
                            Mapping mapping = getMapping(node);
                            mapping.set(parseExpression(exp));
                        }
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

/*

    private static KnowledgeTree<SchemaTypeSystem, XsNode> createKnowledgeTree2(CommandLine cmd) throws MappingException {
        MappingException exception = new MappingException();

        File xsd = new File(cmd.getOptionValue("x"));
        File mapping = new File(cmd.getOptionValue("f"));

        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = XsKnowledgeBase.builder()
                .file(xsd)
                .create().knowledge();

        Properties mappings = new Properties();
        try {
            mappings.load(new FileInputStream(mapping));
            Enumeration<?> enumeration = mappings.propertyNames();
            while (enumeration.hasMoreElements()) {
                String propName = (String) enumeration.nextElement();
                String exp = mappings.getProperty(propName);
                KnowledgeTreeNode<XsNode> node = knowledgeTree.get(propName);
                if (node == null) {
                    exception.add(propName, "path()");

                } else {
                    XsNode xsNode = node.origin();
                    if (exp != null) {
                        Function[] functions = parseExpression(exp);
                        validate(node, functions, exception);
                        if (function("rule", functions) != null || function("assign", functions) != null) {
                            bubble(node);
                        }

                        Map<String, Function> map = new LinkedHashMap<>();
                        for (Function func : functions) {
                            map.put(func.name, func);
                        }

                        if (map.containsKey("assign")) {
                            Assignment assignment = new Assignment();
                            assignment.rule = Assignment.FORCE_ASSIGN_RULE;
                            assignment.expression = assignment.value = map.get("assign").parameters[0];
                            node.annotate("assign", assignment);

                        } else if (map.containsKey("rule")) {
                            Assignment assignment = new Assignment();
                            String rule = map.get("rule").getParameters()[0];

                            if (rule.toUpperCase().contains("DEFAULT")) {
                                assignment.rule = Assignment.HARDCODE_RULE;
                                assignment.expression = rule;
                                if (rule.indexOf("'") > 0) {
                                    assignment.value = rule.substring(rule.indexOf("'"));
                                }

                            } else if (rule.toUpperCase().contains("DIRECT")) {
                                if (map.containsKey("source")) {

                                    String source = map.get("source").getParameters()[0];
                                    assignment.expression = source;
                                    if (source.contains(" ")) {

                                    } else if (source.indexOf('*') > 0) {
                                        assignment.rule = Assignment.ARRAY_MAPPING_RULE;
                                        annotateArrays(assignment, node);

                                    } else {
                                        assignment.rule = Assignment.DIRECT_MAPPING_RULE;
                                        assignment.variable = "$";
                                        assignment.value = source.replaceAll("/", ".");
                                    }
                                }
                            }

                            node.annotate("assign", assignment);
                        }

                    }
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (exception.hasError()) {
            throw exception;

        } else {
            return knowledgeTree;
        }
    }
*/

    private static void validate(KnowledgeTreeNode<XsNode> node, Function[] functions, MappingException exception) {
        Mapping mapping = getMapping(node);
        for (Function function : functions) {
            if ("type".equals(function.getName()) && !function.getParameters()[0].equals(mapping.type)) {
                exception.add(node.getPath(), function.toString(), "Type is not match that of XSD");

            } else if ("cardinality".equals(function.getName()) && !function.getParameters()[0].equals(mapping.cardinality)) {
                exception.add(node.getPath(), function.toString(), "Cardinality is not match that of XSD");

            }
        }
    }

    private static void bubble(KnowledgeTreeNode<XsNode> node) {
        List<KnowledgeTreeNode<XsNode>> list = new ArrayList<>();
        list.add(node);

        KnowledgeTreeNode<XsNode> parent = node.getParent();
        while (parent != null) {
            list.add(0, parent);
            Mapping mapping = getMapping(parent);
            if (mapping.construction == null) {
                mapping.construction = new Construction();
            }

            parent = parent.getParent();
        }

        int level = 1;
        for (KnowledgeTreeNode<XsNode> n : list) {
            Mapping mapping = getMapping(n);
            if (mapping.isArray()) {
                if (mapping.arrayDepth > 0) {
                    level = mapping.arrayDepth;

                } else {
                    mapping.arrayDepth = level;
                }

                level++;
            }
        }
    }

    private static void annotateAssignment(KnowledgeTreeNode<XsNode> node) {
        Mapping mapping = getMapping(node);
        Assignment assignment = mapping.assignment = new Assignment();

        if (mapping.assign != null) {
            assignment.value = mapping.assign;

        } else if (mapping.rule != null) {
            String rule = mapping.rule;
            if (rule.toUpperCase().contains("DEFAULT")) {
                if (rule.indexOf("'") > 0) {
                    assignment.value = rule.substring(rule.indexOf("'"));

                } else {
                    assignment.value = "???";
                }

            } else if (rule.toUpperCase().contains("DIRECT")) {
                if (mapping.source != null) {
                    String source = mapping.source;
                    if (source.contains(" ")) {
                        mapping.todo = true;

                    } else if (source.indexOf('*') > 0) {
                        annotateArrays(assignment, node);

                    } else {
                        assignment.variable = "$";
                        assignment.value = source.replaceAll("/", ".");


                    }
                }
            } else {
                mapping.todo = true;
            }

        } else if (mapping.source != null) {
            mapping.todo = true;
        }
    }

    private static void annotateArrays(Assignment assignment, KnowledgeTreeNode<XsNode> node) {

        Mapping mapping = getMapping(node);
        String exp = mapping.assign != null ? mapping.assign : mapping.source;

        if (exp.endsWith("[*]")) {


        } else {
            int depth = getArrayDepth(exp);
            KnowledgeTreeNode<XsNode> parent = findParentArrayNode(node);

            if (parent == null) {
                mapping.error = new MappingError(node.getPath(), exp, "Cannot find array parent node.");

            } else {
                Mapping parentMapping = getMapping(parent);
                int index = exp.lastIndexOf("[*]/");
                String arrayPath = exp.substring(0, index + 3);

                if (parentMapping.arrayDepth != depth) {
                    System.out.println(exp + " -> " + node.getPath());

                    mapping.error = new MappingError(node.getPath(), exp,
                            "Array depth not match: " + exp + " to " + parent.getPath());

                } else {
                    ArrayKey arrayKey = new ArrayKey(parent.getPath(), arrayPath);

                    Array array = Array.created.get(arrayKey);
                    if (!Array.created.containsKey(arrayKey)) {
                        array = createArray(arrayPath, parent);
                        parentMapping.construction.arrays.put(array.sourcePath, array);

                    }

                    array.addChild(node);

                    assignment.variable = array.variable;
                    assignment.value = exp.substring(index + 4).replaceAll("/", ".");
                }
            }
        }
    }

    private static KnowledgeTreeNode<XsNode> findParentArrayNode(KnowledgeTreeNode<XsNode> node) {

        KnowledgeTreeNode<XsNode> parent = node.getParent();
        while (parent != null && !getMapping(parent).isArray()) {
            parent = parent.getParent();
        }

        if (parent != null && getMapping(parent).isArray()) {
            return parent;

        } else {
            return null;
        }
    }

    private static Array createArray(String sourcePath, KnowledgeTreeNode<XsNode> arrayNode) {

        ArrayKey key = new ArrayKey(arrayNode.getPath(), sourcePath);
        if (!Array.created.containsKey(key)) {
            List<ArrayKey> list = new ArrayList<>();

            KnowledgeTreeNode<XsNode> parent = arrayNode;
            String path = sourcePath;

            while (path.contains("[*]/")) {
                path = path.substring(0, path.lastIndexOf("[*]/") + 3);
                parent = findParentArrayNode(parent);
                list.add(0, new ArrayKey(parent.getPath(), path));
            }

            list.add(key);

            Array parr = null;
            for (ArrayKey ak : list) {
                if (!Array.created.containsKey(ak)) {
                    Array arr = new Array(ak);
                    if (parr != null) {
                        arr.parent = parr;
                    }


                    Array.created.put(ak, arr);
                }

                parr = Array.created.get(ak);
            }
        }

        return Array.created.get(key);
    }

    private static int getArrayDepth(String path) {
        int depth = new StringTokenizer(path, "*").countTokens() - 1;
        return depth;
    }

    private static Mapping getMapping(KnowledgeTreeNode<XsNode> node) {
        return node.getAnnotation("mapping", Mapping.class);
    }

    private static String type(XsNode node) {
        if (XsNode.XsNodeType.Folder.equals(node.getNodeType())) {
            return "complex";

        } else if (XsNode.XsNodeType.Attribute.equals(node.getNodeType())) {
            return getXsType(node.getSchemaType());

        } else {
            return getXsType(node.getSchemaType());

        }
    }

    private static String getXsType(SchemaType schemaType) {
        SchemaType base = schemaType;
        while (base != null && !base.isSimpleType()) {
            base = base.getBaseType();
        }

        if (base == null || XmlBeansUtils.getXMLBuildInType(base) == null) {
            return "string";

        } else {
            XmlBeansUtils.XMLBuildInType buildInType = XmlBeansUtils.getXMLBuildInType(base);
            String type = buildInType.getName();
            if (type.startsWith("xs:")) {
                type = type.substring(3);
            }

            return type;
        }
    }

    private static String cardinality(XsNode xsNode) {
        return xsNode.getMaxOccurs() == null ? xsNode.getMinOccurs() + "-n" : xsNode.getMinOccurs() + "-" + xsNode.getMaxOccurs();
    }

    private static Function function(String name, Function[] functions) {
        for (Function function : functions) {
            if (name.equals(function.getName())) {
                return function;
            }
        }

        return null;
    }

    private static Function[] parseExpression(String exp) {
        List<Function> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(exp, "::");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            list.add(new Function(token));
        }

        return list.toArray(new Function[list.size()]);
    }

    static class Mapping {

        private String type;
        private String cardinality;
        private String rule;
        private String source;
        private String version;

        private boolean todo;
        private String assign;

        private int arrayDepth;

        private Construction construction;
        private Assignment assignment;

        private MappingError error;

        public Mapping(Function[] functions) {
            set(functions);
        }

        public Mapping(XsNode xsNode) {
            type = type(xsNode);
            cardinality = cardinality(xsNode);
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

    public interface Renderer {
        void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder);
    }

    static class MappingRenderer implements Renderer {
        @Override
        public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
            codeBuilder.append(node.getPath()).append("=").appendLine(getMapping(node).toString());
            //codeBuilder.append(node.getPath()).append("=").appendLine(getMapping(node).toConstruct());
            node.getChildren().forEach(e -> {
                render(e, codeBuilder);
            });
        }
    }

    static class ConstructRenderer implements Renderer {

        @Override
        public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
            Mapping mapping = getMapping(node);
            if (mapping.construction != null) {
                codeBuilder.append(node.getPath()).append("=").appendLine(mapping.construction.toString());
                node.getChildren().forEach(e -> {
                    render(e, codeBuilder);
                });

            } else if (mapping.assign != null) {
                Assignment assignment = mapping.assignment;
                codeBuilder.append(node.getPath()).append("=").appendLine(mapping.assign);

            } else if (mapping.assignment != null) {
                Assignment assignment = mapping.assignment;
                codeBuilder.append(node.getPath()).append("=").appendLine(assignment.value());

            }
        }
    }

    static class ConstructTreeRenderer implements Renderer {

        @Override
        public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
            int level = node.getPath().split("/").length;
            Mapping mapping = getMapping(node);
            /*if(mapping.assignment != null) {
                codeBuilder.appendLine();
            }*/

            if (mapping.todo) {
                codeBuilder.append("-- ", level).appendLine(node.getPath());
                codeBuilder.appendLine("-- TODO", level);
                codeBuilder.appendLine();

            } else if (mapping.error != null) {
                codeBuilder.append("-- ", level).appendLine(node.getPath());
                codeBuilder.append("-- ERROR: ", level).appendLine(mapping.error.description);
                codeBuilder.appendLine();

            } else if (mapping.construction != null) {
                Construction construction = mapping.construction;
                codeBuilder.append("-- ", level).appendLine(node.getPath());

                if (construction.arrays.isEmpty()) {
                    node.getChildren().forEach(e -> {
                        render(e, codeBuilder);
                    });

                } else {
                    construction.arrays.entrySet().forEach(e -> {
                        Array array = e.getValue();
                        codeBuilder.append("-- ARRAY:", level).append(array.sourcePath).append(" to ").append(array.targetPath).appendLine();
                        array.childNodes.forEach(n -> {
                            render(n, codeBuilder);
                        });

                    });
                }

            } else if (mapping.assignment != null) {
                Assignment assignment = mapping.assignment;
                codeBuilder.append("-- ", level).appendLine(node.getPath());
                codeBuilder.append("-- ", level).append("ASSIGN(").append(assignment.value()).appendLine(")");
                codeBuilder.appendLine();

            }
        }
    }

    static class ArraysRenderer implements Renderer {

        @Override
        public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
            List<Array> list = new ArrayList<>(Array.created.values());

            codeBuilder.appendLine(CommandLines.GSON.toJson(Array.created.values()));

        }
    }

    static class EsqlRenderer implements Renderer {
        private String packageName;
        private String name;

        public EsqlRenderer(String fullName) {
            if (fullName.contains(".")) {
                int lastPoint = fullName.lastIndexOf('.');
                packageName = fullName.substring(0, lastPoint);
                name = fullName.substring(lastPoint + 1);
            } else {
                name = fullName;
            }
        }

        @Override
        public void render(KnowledgeTreeNode<XsNode> node, CodeBuilder codeBuilder) {
            if (node.getParent() == null) {
                printStart(node, codeBuilder);
            }

            node.getChildren().forEach(e -> {
                Mapping mapping = getMapping(e);
                if (mapping.construction != null || mapping.assignment != null) {
                    printNode(e, codeBuilder);
                }
            });

            if (node.getParent() == null) {
                printEnd(codeBuilder);
            }

        }

        private void printNode(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            Mapping mapping = getMapping(node);
            if (mapping.construction != null) {
                if (mapping.construction.arrays.isEmpty()) {
                    printConstruct(node, builder);

                } else {
                    printArrays(node, builder);

                }
            } else if (mapping.assignment != null) {
                printAssignment(node, builder);

            }

        }

        private void printConstruct(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            int indent = builder.currentIndentLevel() + level(node);

            String var = var(node);
            String par = var(node.getParent());
            String qname = qname(node);

            builder.appendLine("-- " + node.getPath(), indent);
            builder.append("DECLARE ", indent)
                    .append(var).append(" REFERENCE TO ").append(par).appendLine(";");
            builder.append("CREATE LASTCHILD OF ", indent)
                    .append(par).append(" AS ").append(var)
                    .append(" TYPE XMLNSC.Folder NAME '").append(qname).appendLine("';");
            builder.appendLine();

            node.getChildren().forEach(e -> {
                printNode(e, builder);
            });
        }

        private void printArrays(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            Mapping mapping = getMapping(node);
            mapping.construction.arrays.entrySet().forEach(e -> {
                printArray(e.getValue(), node, builder);
            });
            builder.popIndent();
        }

        private void printArray(Array array, KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            int indent = builder.currentIndentLevel() + level(node);
            String inputAssign = array.sourcePath;
            inputAssign = inputAssign.substring(0, inputAssign.length() - 3) + ".Item";
            if (array.parent == null) {
                inputAssign = "_inputRootNode." + inputAssign.replaceAll("/", ".");

            } else {
                inputAssign = array.parent.variable + "." + inputAssign.substring(array.parent.sourcePath.length() + 1).replaceAll("/", ".");
            }

            String name = "loop" + array.variable;

            builder.append("-- LOOP FROM ", indent)
                    .append(array.sourcePath).append(" TO ").append(array.targetPath).appendLine();
            builder.append("DECLARE ", indent)
                    .append(array.variable).append(" REFERENCE TO ").append(inputAssign).appendLine(";");

            builder.append(name, indent).append(" : WHILE LASTMOVE(").append(array.variable).appendLine(") DO");
            builder.appendLine();

            builder.pushIndent();
            indent++;

            String var = var(node);
            String par = var(node.getParent());
            String qname = qname(node);
            builder.appendLine("-- " + node.getPath(), indent);
            builder.append("DECLARE ", indent)
                    .append(var).append(" REFERENCE TO ").append(par).appendLine(";");
            builder.append("CREATE LASTCHILD OF ", indent)
                    .append(par).append(" AS ").append(var)
                    .append(" TYPE XMLNSC.Folder NAME '").append(qname).appendLine("';");
            builder.appendLine();

            array.childNodes.forEach(e -> {
                printNode(e, builder);
            });

            indent--;
            builder.popIndent();
            builder.append("MOVE ", indent).append(array.variable).appendLine(" NEXTSIBLING;");
            builder.append("END WHILE ", indent).append(name).appendLine(";");
            builder.appendLine();
        }

        private void printAssignment(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            int indent = builder.currentIndentLevel() + level(node);
            Mapping mapping = getMapping(node);
            String par = var(node.getParent());
            String name = node.getName();
            if (name.startsWith("@")) {
                name = name.substring(1);
                XsNode xsNode = node.origin();
                if (!xsNode.getName().getLocalPart().equals(xsNode.getName().toString())) {
                    name = "Abs:" + name;
                }
            } else {
                name = "Abs:" + name;
            }

            String value = mapping.assignment.value();
            if (value != null) {
                value = value.replace("$.", "_inputRootNode.");
            } else {
                value = "'???'";
            }

            builder.appendLine("-- " + node.getPath(), indent);
            builder.append("SET ", indent)
                    .append(par).append(".(").append(type(node)).append(")").append(name)
                    .append(" = ").append(value).appendLine(";");
            builder.appendLine();
        }

        private void printStart(KnowledgeTreeNode<XsNode> node, CodeBuilder builder) {
            String var = var(node);

            builder.append("BROKER SCHEMA ").appendLine(packageName);
            builder.appendLine();
            builder.append("CREATE COMPUTE MODULE ").appendLine(name);
            builder.appendLine();
            builder.pushIndent();

            builder.appendLine("-- Declare UDPs", builder.currentIndentLevel());
            builder.appendLine("DECLARE VERSION_ID EXTERNAL CHARACTER '1.0.0';", builder.currentIndentLevel());
            builder.appendLine("DECLARE SYSTEM_ENVIRONMENT_CODE EXTERNAL CHARACTER 'PROD';", builder.currentIndentLevel());
            builder.appendLine();

            builder.appendLine("-- Declare Namespace", builder.currentIndentLevel());
            builder.appendLine("DECLARE Abs NAMESPACE 'https://collab.safeway.com/it/architecture/info/default.aspx';", builder.currentIndentLevel());
            builder.appendLine();

            builder.appendLine("CREATE FUNCTION Main() RETURNS BOOLEAN", builder.currentIndentLevel());
            builder.appendLine("BEGIN", builder.currentIndentLevel());
            builder.pushIndent();

            builder.appendLine("-- Declare Input Message Root", builder.currentIndentLevel());
            builder.appendLine("DECLARE _inputRootNode REFERENCE TO InputRoot.JSON.Data;", builder.currentIndentLevel());
            builder.appendLine();

            builder.appendLine("-- Declare Output Message Root", builder.currentIndentLevel());
            builder.appendLine("CREATE LASTCHILD OF OutputRoot DOMAIN 'XMLNSC';", builder.currentIndentLevel());
            builder.appendLine();

            builder.append("DECLARE ", builder.currentIndentLevel()).append(var).appendLine(" REFERENCE TO OutputRoot.XMLNSC;");
            builder.append("CREATE LASTCHILD OF OutputRoot.XMLNSC AS ", builder.currentIndentLevel())
                    .append(var).append(" TYPE XMLNSC.Folder NAME '").append(node.getName()).appendLine("';");
            builder.append("SET OutputRoot.XMLNSC.", builder.currentIndentLevel())
                    .append(node.getName()).appendLine(".(XMLNSC.NamespaceDecl)xmlns:Abs=Abs;");
            builder.appendLine();

            builder.popIndent();
            builder.popIndent();
        }

        private void printEnd(CodeBuilder builder) {
            builder.appendLine("END;", 1);
            builder.appendLine("END MODULE;");

        }

        private int level(KnowledgeTreeNode<XsNode> node) {
            return new StringTokenizer(node.getPath(), "/").countTokens();
        }

        private String var(KnowledgeTreeNode<XsNode> node) {
            String token = node.getName();
            return token + "_";
        }

        private String qname(KnowledgeTreeNode<XsNode> node) {
            int level = node.getPath().split("/").length;
            return level > 2 ? "Abs:" + node.getName() : node.getName();
        }

        private String type(KnowledgeTreeNode<XsNode> node) {
            XsNode.XsNodeType nodeType = node.origin().getNodeType();
            if (XsNode.XsNodeType.Folder.equals(nodeType)) {
                return "XMLNSC.Folder";

            } else if (XsNode.XsNodeType.Attribute.equals(nodeType)) {
                return "XMLNSC.Attribute";

            } else {
                return "XMLNSC.Field";

            }
        }

    }
}
