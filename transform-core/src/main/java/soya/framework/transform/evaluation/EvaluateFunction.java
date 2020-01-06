package soya.framework.transform.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class EvaluateFunction implements EvaluateTreeNode {
    private final EvaluateTreeNodeType type = EvaluateTreeNodeType.FUNCTION;
    private final String name;
    private final EvaluateTreeNode[] arguments;

    private EvaluateFunction(String name, EvaluateTreeNode[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public EvaluateTreeNodeType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public EvaluateTreeNode[] getArguments() {
        return arguments;
    }

    public static EvaluateFunction[] toFunctions(String expression) {
        String exp = expression.trim();
        List<EvaluateFunction> list = new ArrayList();
        Stack<Character> stack = new Stack<>();
        StringBuilder builder = new StringBuilder();
        for (char c : exp.toCharArray()) {
            if (c == '.' && stack.size() == 0) {
                String param = builder.toString().trim();
                if (param.length() == 0) {
                    throw new IllegalFunctionArgumentException("Argument is empty.");
                }

                list.add(toFunction(builder.toString().trim()));
                builder = new StringBuilder();

            } else {
                builder.append(c);
                if (c == '(') {
                    stack.push(c);

                } else if (c == ')') {
                    Character pop = stack.pop();
                    if (pop != '(') {
                        throw new IllegalFunctionArgumentException("Expecting '(' instead of '" + pop + "'.");
                    }
                }

            }
        }

        if (builder != null) {
            list.add(toFunction(builder.toString().trim()));
        }

        return list.toArray(new EvaluateFunction[list.size()]);
    }

    public static EvaluateFunction toFunction(String expression) {
        String exp = expression.trim();
        int start = exp.indexOf('(');
        int end = exp.lastIndexOf(')');
        String func = exp.substring(0, start);
        String params = exp.substring(start + 1, end);
        return new EvaluateFunction(func, toArray(params));
    }

    private static EvaluateTreeNode[] toArray(String params) {
        if (params == null || params.trim().length() == 0) {
            return new EvaluateTreeNode[0];
        }

        String exp = params.trim();
        List<EvaluateTreeNode> list = new ArrayList();
        Stack<Character> stack = new Stack<>();
        StringBuilder builder = new StringBuilder();
        for (char c : exp.toCharArray()) {
            if (c == ',' && stack.size() == 0) {
                String param = builder.toString().trim();
                if (param.length() == 0) {
                    throw new IllegalFunctionArgumentException("Argument is empty.");
                }

                list.add(toNode(builder.toString().trim()));
                builder = new StringBuilder();

            } else {
                builder.append(c);
                if (c == '(' || c == '[' || c == '{') {
                    stack.push(c);

                } else if (c == ')') {
                    Character pop = stack.pop();
                    if (pop != '(') {
                        throw new IllegalFunctionArgumentException("Expecting '(' instead of '" + pop + "'.");
                    }
                } else if (c == ']') {
                    Character pop = stack.pop();
                    if (pop != '[') {
                        throw new IllegalFunctionArgumentException("Expecting '[' instead of '" + pop + "'.");
                    }
                } else if (c == '}') {
                    Character pop = stack.pop();
                    if (pop != '{') {
                        throw new IllegalFunctionArgumentException("Expecting '{' instead of '" + pop + "'.");
                    }
                }
            }
        }

        if (builder != null) {
            list.add(toNode(builder.toString().trim()));
        }

        return list.toArray(new EvaluateTreeNode[list.size()]);
    }

    private static EvaluateTreeNode toNode(String exp) {
        EvaluateTreeNode node;
        if(exp.startsWith("(") && exp.endsWith(")")) {
            node = toFunction("INNER" + exp);

        } else if (exp.startsWith("[") && exp.endsWith("]")) {
            String s = exp.substring(1, exp.length() - 1);
            EvaluateTreeNode[] nodes = toArray(s);
            node = new EvaluateArray(nodes);

        } else if (!exp.contains("(")) {
            node = new EvaluateParameter(exp);

        } else {
            // Function:
            node = toFunction(exp);
        }

        return node;
    }
}
