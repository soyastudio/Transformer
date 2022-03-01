package soya.framework.tool.commands;

import com.google.gson.JsonParser;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.xmlbeans.SchemaTypeSystem;
import soya.framework.commons.cli.Command;
import soya.framework.transform.schema.KnowledgeTree;
import soya.framework.transform.schema.avro.SampleAvroGenerator;
import soya.framework.transform.schema.converter.XsdToAvsc;
import soya.framework.transform.schema.xs.XsNode;

import java.util.Random;

@Command(name = "bod-sample-avro", uri = "bod://sample-avro")
public class SampleAvroCommand extends SchemaCommand {

    @Override
    protected String render() {
        Schema schema = XsdToAvsc.fromXmlSchema(tree.origin());
        Object result = new SampleAvroGenerator(schema, new Random(), 0).generate();
        GenericRecord genericRecord = (GenericRecord) result;

        return GSON.toJson(JsonParser.parseString(genericRecord.toString()));
    }
}
