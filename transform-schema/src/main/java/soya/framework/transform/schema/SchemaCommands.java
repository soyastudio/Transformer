package soya.framework.transform.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.cli.CommandLine;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import soya.framework.commons.cli.CommandLines;
import soya.framework.transform.schema.avro.AvroUtils;
import soya.framework.transform.schema.avro.SampleAvroGenerator;
import soya.framework.transform.schema.converter.XmlToAvro;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.converter.XsdToXPathDataType;
import soya.framework.transform.schema.xs.XmlBeansUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SchemaCommands extends CommandLines {

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
    public static String xsdToXpathDataType(CommandLine commandLine) {
        File file = new File(commandLine.getOptionValue("x"));
        return XsdToXPathDataType.convert(file);
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
    public static String xsdToSampleXml(CommandLine commandLine) throws XmlException, IOException {
        File file = new File(commandLine.getOptionValue("x"));
        SchemaTypeSystem sts = XmlBeansUtils.getSchemaTypeSystem(file);
        return SampleXmlUtil.createSampleForType(sts.documentTypes()[0]);
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
    public static String xsdToAvsc(CommandLine commandLine) {
        File file = new File(commandLine.getOptionValue("x"));
        return XsdToAvsc.fromXmlSchema(file).toString(true);
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
                            desc = "Xsd file path."),
                    @Opt(option = "z",
                            defaultValue = "json",
                            desc = "Encoder, value can be 'json', 'binary'. Default value is 'json'.")
            },
            cases = {"-a sampleAvro -x XSD_FILE_PATH -o OUTPUT_FILE"}
    )
    public static String sampleAvro(CommandLine commandLine) throws Exception {
        Schema schema = null;
        String path = commandLine.getOptionValue("x");
        if (path.toLowerCase().endsWith(".xsd")) {
            schema = XsdToAvsc.fromXmlSchema(XmlBeansUtils.getSchemaTypeSystem(new File(path)));

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
                AvroUtils.write(genericRecord, schema, out);
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

        File xsd = new File(commandLine.getOptionValue("x"));

        Schema schema = XsdToAvsc.fromXmlSchema(xsd);

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

            if (commandLine.hasOption("o")) {
                File out = new File(commandLine.getOptionValue("o"));
                AvroUtils.write(record, schema, out);
            }

            return record.toString();

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

        File xsd = new File(commandLine.getOptionValue("x"));
        Schema schema = XsdToAvsc.fromXmlSchema(xsd);
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

}
