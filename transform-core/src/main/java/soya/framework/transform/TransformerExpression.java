package soya.framework.transform;

public final class TransformerExpression {

    private final String function;
    private final String[] arguments;

    private TransformerExpression(String function, String[] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    public String getFunction() {
        return function;
    }

    public String[] getArguments() {
        return arguments;
    }

    public static TransformerExpression parse(String expression) {
        String exp = expression.trim();
        int start = exp.indexOf('(');
        int end = exp.lastIndexOf(')');
        String func = exp.substring(0, start);
        String params = exp.substring(start + 1, end);

        return new TransformerExpression(func, toArray(params));
    }

    private static String[] toArray(String params) {
        String exp = params.trim();
        if(params == null && exp.isEmpty()) {
            return new String[0];
        }

        // FIXME:
        String[] array = exp.split(",");
        for(int i = 0; i < array.length; i ++) {
            array[i] = array[i].trim();
        }
        return array;
    }
}
