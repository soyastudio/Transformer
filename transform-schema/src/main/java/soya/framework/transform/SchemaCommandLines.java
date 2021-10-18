package soya.framework.transform;

import com.google.gson.*;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.cli.*;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.avro.SampleAvroGenerator;
import soya.framework.transform.schema.converter.XmlToAvro;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.xs.XmlBeansUtils;
import soya.framework.transform.schema.xs.XsKnowledgeBase;
import soya.framework.transform.schema.xs.XsNode;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SchemaCommandLines {

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    protected static CommandLineParser parser = new DefaultParser();
    protected static Options options = new Options();
    protected static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected static Map<String, Method> commands = new LinkedHashMap<>();

    static {
        Method[] methods = SchemaCommandLines.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Command.class) != null) {
                commands.put(method.getName(), method);
            }
        }

        // Command line definition:
        options.addOption(Option.builder("a")
                .longOpt("action")
                .hasArg(true)
                .desc("Task to execute.")
                .required(false)
                .build());

        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg(true)
                .desc("Input message, file path or url")
                .required(false)
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg(true)
                .desc("Output file.")
                .required(false)
                .build());

        options.addOption(Option.builder("q")
                .longOpt("query")
                .hasArg(true)
                .desc("Query.")
                .required(false)
                .build());

        options.addOption(Option.builder("x")
                .longOpt("xsd")
                .hasArg(true)
                .desc("Location of xsd")
                .required(false)
                .build());

    }

    public static void main(String[] args) throws Exception {
        String cl = "-a avroToJson -i C:/github/Workshop/Repository/BusinessObjects/AirMilePoints/GetAirMilePoints.avro";
        System.out.println(execute(build(cl)));


    }

    public static String execute(CommandLine commandLine) throws Exception {
        String action = "";
        if (commandLine.hasOption("a")) {
            action = commandLine.getOptionValue("a");
        }

        Method method = SchemaCommandLines.class.getMethod(action, new Class[]{CommandLine.class});
        if (method.getAnnotation(Command.class) == null) {
            throw new IllegalArgumentException("Not command method: " + method.getName());
        }
        Command command = method.getAnnotation(Command.class);
        Opt[] opts = command.options();
        for (Opt opt : opts) {
            if (opt.required() && commandLine.getOptionValue(opt.option()) == null) {
                throw new IllegalArgumentException("Option '" + opt.option() + "' is required.");
            }
        }

        return (String) method.invoke(null, new Object[]{commandLine});
    }

    @Command(
            desc = "Help",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "help",
                            desc = "Command name."),
                    @Opt(option = "q",
                            desc = "Query for help topic")
            },
            cases = {"-a help"}
    )
    public static String help(CommandLine cmd) {
        String query = cmd.getOptionValue("q");

        if (query == null) {
            JsonArray array = new JsonArray();
            commands.entrySet().forEach(e -> {
                Method method = e.getValue();
                array.add(commandDesc(method));
            });

            return GSON.toJson(array);

        } else if (query.length() == 1 && options.hasOption(query)) {
            Option option = options.getOption(query);
            JsonObject jo = new JsonObject();
            jo.addProperty("option", option.getOpt());
            jo.addProperty("longOption", option.getLongOpt());
            jo.addProperty("hasArg", option.hasArg());
            jo.addProperty("description", option.getDescription());
            return GSON.toJson(jo);

        } else if (query.length() > 1 && commands.containsKey(query)) {
            return GSON.toJson(commandDesc(commands.get(query)));

        } else {
            return "Can not find help topic.";
        }
    }

    @Command(
            desc = "Parse xsd and render xpath, datatype and cardinality",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "xpathDataType",
                            desc = "Command name."),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd file path.")
            },
            cases = {"-a xpathDataType -x XSD_FILE_PATH"}
    )
    public static String xpathDataType(CommandLine commandLine) {
        KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree = knowledgeTree(commandLine);
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = knowledgeTree.paths();
        while (iterator.hasNext()) {
            String path = iterator.next();
            XsNode node = knowledgeTree.get(path).origin();
            builder.append(path).append("=type(");
            if (XsNode.XsNodeType.Folder.equals(node.getNodeType())) {
                builder.append("complex").append(")");

            } else if (XsNode.XsNodeType.Attribute.equals(node.getNodeType())) {
                builder.append(getSimpleType(node.getSchemaType())).append(")");

            } else {
                builder.append(getSimpleType(node.getSchemaType())).append(")");

            }

            builder.append("::").append("cardinality(").append(node.getMinOccurs()).append("-");

            if (node.getMaxOccurs() != null) {
                builder.append(node.getMaxOccurs());

            } else {
                builder.append("n");
            }

            builder.append(")").append("\n");
        }
        return builder.toString();
    }

    @Command(
            desc = "Generate sample xml against xsd",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd file path.")
            },
            cases = {"-a sampleXml -x XSD_FILE_PATH"}
    )
    public static String sampleXml(CommandLine commandLine) {
        SchemaTypeSystem sts = knowledgeTree(commandLine).origin();
        String result = SampleXmlUtil.createSampleForType(sts.documentTypes()[0]);
        return result.replace("xmlns:def", "xmlns:Abs").replaceAll("def:", "Abs:");
    }

    @Command(
            desc = "Convert xsd to avro schema",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd file path.")
            },
            cases = {"-a avsc -x XSD_FILE_PATH"}
    )
    public static String avsc(CommandLine commandLine) {
        return XsdToAvsc
                .fromXmlSchema(knowledgeTree(commandLine).origin())
                .toString(true);
    }

    @Command(
            desc = "Generate sample avro against xsd",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "o",
                            desc = "Output file or path"),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd file path.")
            },
            cases = {"-a sampleAvro -x XSD_FILE_PATH -o OUTPUT_FILE"}
    )
    public static String sampleAvro(CommandLine commandLine) throws Exception {
        Schema schema = null;
        String path = commandLine.getOptionValue("x");
        if (path.toLowerCase().endsWith(".xsd")) {
            schema = XsdToAvsc.fromXmlSchema(knowledgeTree(commandLine).origin());

        } else if (path.toLowerCase().endsWith(".avsc")) {
            schema = new Schema.Parser().parse(new File(path));

        }

        if (schema == null) {
            throw new IllegalArgumentException("Can not create schema from: " + path);
        }

        Object result = new SampleAvroGenerator(schema, new Random(), 0).generate();
        GenericRecord genericRecord = (GenericRecord) result;

        if (commandLine.hasOption("o")) {

            File out = new File(commandLine.getOptionValue("o"));
            if (out.exists()) {
                write(genericRecord, schema, out);
            }
        }

        return genericRecord.toString();
    }

    @Command(
            desc = "Convert xml to avro against xml or avro schema",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "i",
                            required = true,
                            desc = "Input string, file or url"),
                    @Opt(option = "o",
                            desc = "Output file or path"),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToAvro -x XSD_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String xmlToAvro(CommandLine commandLine) {
        Schema schema = XsdToAvsc.fromXmlSchema(knowledgeTree(commandLine).origin());

        File xml = new File(commandLine.getOptionValue("i"));
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(xml);
            document.getDocumentElement().normalize();

            //Here comes the root node
            Element root = document.getDocumentElement();

            GenericData.Record record = XmlToAvro.createRecord(schema, root);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            write(record, schema, outputStream);
            byte[] data = outputStream.toByteArray();

            if (commandLine.hasOption("o")) {
                File out = new File(commandLine.getOptionValue("o"));
                if (out.exists()) {
                    write(record, schema, out);
                }
            }

            return new String(data);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "i",
                            required = true,
                            desc = "Input string, file or url"),
                    @Opt(option = "o",
                            desc = "Output file or path"),
                    @Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String xmlToJson(CommandLine commandLine) {
        Schema schema = XsdToAvsc.fromXmlSchema(knowledgeTree(commandLine).origin());
        File xml = new File(commandLine.getOptionValue("i"));

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(xml);
            document.getDocumentElement().normalize();

            //Here comes the root node
            Element root = document.getDocumentElement();

            GenericData.Record record = XmlToAvro.createRecord(schema, root);

            JsonObject jsonObject = JsonParser.parseString(record.toString()).getAsJsonObject();

            return GSON.toJson(jsonObject);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Command(
            desc = "Read avro data from file and display message data as Json format;",
            options = {
                    @Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @Opt(option = "i",
                            required = true,
                            desc = "Input string, file or url")
            },
            cases = {"-a avroToJson -i INPUT -o OUTPUT_FILE"}
    )
    public static String avroToJson(CommandLine commandLine) throws IOException {

        List<JsonElement> list = new ArrayList<>();
        File avro = new File(commandLine.getOptionValue("i"));
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>();
        DataFileReader<GenericRecord> dataFileReader =
                new DataFileReader<GenericRecord>(avro, datumReader);

        dataFileReader.forEach(e -> {
            list.add(JsonParser.parseString(e.toString()));
        });

        return GSON.toJson(list);
    }


    public static CommandLine build(String cl) throws ParseException {
        List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(cl);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            list.add(token);
        }

        return build(list.toArray(new String[list.size()]));
    }

    public static CommandLine build(String[] args) throws ParseException {
        return parser.parse(options, args);
    }

    public static CommandLine build(JsonObject jsonObject) throws ParseException {
        List<String> list = new ArrayList<>();
        jsonObject.entrySet().forEach(e -> {
            String key = e.getKey();
            if (options.hasOption(e.getKey())) {
                list.add("-" + key);
                if (options.getOption(key).hasArg()) {
                    list.add(e.getValue().getAsString());
                }
            }
        });
        return build(list.toArray(new String[list.size()]));
    }

    protected static JsonObject commandDesc(Method method) {
        Command annotation = method.getAnnotation(Command.class);

        JsonObject obj = new JsonObject();
        obj.addProperty("command", method.getName());
        obj.addProperty("description", annotation.desc());

        JsonArray opts = new JsonArray();
        Opt[] options = annotation.options();
        for (Opt opt : options) {
            JsonObject o = new JsonObject();
            o.addProperty("option", opt.option());
            o.addProperty("required", opt.required());
            o.addProperty("defaultValue", opt.defaultValue());
            o.addProperty("description", opt.desc());
            opts.add(o);
        }
        obj.add("options", opts);

        obj.add("example", GSON.toJsonTree(annotation.cases()));

        return obj;
    }


    protected static KnowledgeTree<SchemaTypeSystem, XsNode> knowledgeTree(CommandLine commandLine) {
        String file = commandLine.getOptionValue("x");
        File xsd = new File(file);
        if (!xsd.exists()) {
            throw new IllegalArgumentException("File does not exist: " + xsd.getAbsolutePath());
        }

        return XsKnowledgeBase.builder()
                .file(xsd)
                .create().knowledge();
    }

    private static void write(GenericRecord record, Schema schema, OutputStream outputStream) throws IOException {
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

        writer.write(record, encoder);
        encoder.flush();
        outputStream.close();
    }

    private static void write(GenericRecord record, Schema schema, File avro) throws Exception {
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema));
        FileOutputStream outputStream = new FileOutputStream(avro);
        dataFileWriter.create(schema, outputStream);

        dataFileWriter.append(record);
        dataFileWriter.close();

    }

    private static String getSimpleType(SchemaType schemaType) {
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

            switch (type) {
                case "normalizedString":
                case "date":
                case "dateTime":
                case "time":
                    return "string";

                default:
                    return type;
            }
        }
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Command {
        String desc() default "";

        Opt[] options() default {};

        String[] cases() default {};
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Opt {
        String option();

        boolean required() default false;

        String defaultValue() default "";

        String desc() default "";
    }
}
