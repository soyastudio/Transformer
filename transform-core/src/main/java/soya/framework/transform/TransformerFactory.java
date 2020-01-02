package soya.framework.transform;

public class TransformerFactory extends Transformers {
    protected static TransformerFactory instance;

    static {
        instance = new TransformerFactory();
        register(Transformer.class.getPackage().getName());
    }

    protected TransformerFactory() {
        super();
    }

    public Transformer create(String expression) {
        TransformerExpression exp = TransformerExpression.parse(expression);
        return getTransformerBuilder(exp.getFunction()).create(exp.getArguments(), null);
    }

    public static TransformerFactory getInstance() {
        return instance;
    }
}
