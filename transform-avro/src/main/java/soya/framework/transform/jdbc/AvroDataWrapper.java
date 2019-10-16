package soya.framework.transform.jdbc;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.commons.beanutils.RowSetDynaClass;

public class AvroDataWrapper {
    private Schema schema;
    private GenericData.Array<GenericData.Record> array;

    private RowSetDynaClass rowSetDynaClass;

    public AvroDataWrapper(RowSetDynaClass rowSetDynaClass) {
        this.rowSetDynaClass = rowSetDynaClass;
    }

    public Schema getSchema() {
        return schema;
    }

    public GenericData.Array<GenericData.Record> getArray() {
        return array;
    }
}
