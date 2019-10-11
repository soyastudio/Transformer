package soya.framework.transform.avro;

import org.apache.avro.Schema;
import soya.framework.transform.Transformer;
import soya.framework.transform.TransformerException;

import java.sql.ResultSet;

public class ResultSetToSchemaTransformer implements Transformer<Schema, ResultSet> {

    @Override
    public Schema transform(ResultSet src) throws TransformerException {
        return null;
    }
}
