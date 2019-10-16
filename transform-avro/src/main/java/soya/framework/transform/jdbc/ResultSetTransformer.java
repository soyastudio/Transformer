package soya.framework.transform.jdbc;

import org.apache.commons.beanutils.RowSetDynaClass;
import soya.framework.transform.Transformer;
import soya.framework.transform.TransformerException;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultSetTransformer<T> implements Transformer<T, ResultSet> {
    private boolean lowerCase;
    private int limit;
    private boolean useColumnLabel;

    public ResultSetTransformer() {
    }

    public ResultSetTransformer(boolean lowerCase, int limit, boolean useColumnLabel) {
        this.lowerCase = lowerCase;
        this.limit = limit;
        this.useColumnLabel = useColumnLabel;
    }

    @Override
    public T transform(ResultSet src) throws TransformerException {

        try {
            RowSetDynaClass rowSetDynaClass = new RowSetDynaClass(src, lowerCase, limit, useColumnLabel);
            return transform(rowSetDynaClass);

        } catch (SQLException e) {
            throw new TransformerException(e);
        }
    }

    protected abstract T transform(RowSetDynaClass rowSetDynaClass);
}
