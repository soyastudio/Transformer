package soya.framework.transform.evaluation;

public final class EvaluateParameter implements EvaluateTreeNode {
    private final EvaluateTreeNodeType type = EvaluateTreeNodeType.VALUE;
    private final String value;

    protected EvaluateParameter(String value) {
        this.value = value;
    }

    @Override
    public EvaluateTreeNodeType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getStringValue(EvaluationContext context) {
        return value;
    }

    public boolean getBoolean(EvaluationContext context) {
        return Boolean.parseBoolean(getStringValue(context));
    }
}
