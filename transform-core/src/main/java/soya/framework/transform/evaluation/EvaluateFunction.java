package soya.framework.transform.evaluation;

public final class EvaluateFunction {
    private final String name;
    private final String[] arguments;

    private EvaluateFunction(String name, String[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public String[] getArguments() {
        return arguments;
    }

    public static EvaluateFunction parse(String expression) {
        String exp = expression.trim();
        int start = exp.indexOf('(');
        int end = exp.lastIndexOf(')');
        String func = exp.substring(0, start);
        String params = exp.substring(start + 1, end);
        return new EvaluateFunction(func, toArray(params));
    }

    private static String[] toArray(String params) {
        String exp = params.trim();
        if (params == null && exp.isEmpty()) {
            return new String[0];
        }

        // FIXME:
        String[] array = exp.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }
}
