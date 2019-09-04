package soya.framework.transform.transformers;

import soya.framework.transform.TransformerException;

public class JsonPathTransformer extends JsonDataTransformer {

    private String uri;

    public JsonPathTransformer(String uri) {
        this.uri = uri;
    }

    @Override
    protected String process(String src) throws TransformerException {
        return null;
    }
}
