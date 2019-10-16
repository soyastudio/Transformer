package soya.framework.transform.jdbc;

import org.apache.commons.beanutils.RowSetDynaClass;

public class ResultSetToAvroTransformer extends ResultSetTransformer<AvroDataWrapper> {
    @Override
    protected AvroDataWrapper transform(RowSetDynaClass rowSetDynaClass) {
        return new AvroDataWrapper(rowSetDynaClass);
    }
}
