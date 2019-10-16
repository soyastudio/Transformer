package soya.framework.transform.jdbc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;

import java.util.List;

public class ResultSetToDynaBeanTransformer extends ResultSetTransformer<List<DynaBean>> {

    @Override
    protected List<DynaBean> transform(RowSetDynaClass rowSetDynaClass) {
        return ImmutableList.copyOf(rowSetDynaClass.getRows());
    }
}
