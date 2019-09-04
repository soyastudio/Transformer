package soya.framework.transform.transformers;

import soya.framework.commons.json.GsonUtils;
import soya.framework.transform.StringTransformer;
import soya.framework.transform.TransformerException;

public abstract class JsonDataTransformer implements StringTransformer {

    @Override
    public String transform(String src) throws TransformerException {
        if(GsonUtils.isValidJson(src)) {
            return process(src);
        }

        throw new IllegalArgumentException("Source string is not a valid json.");
    }

    protected abstract String process(String src) throws TransformerException;
}
