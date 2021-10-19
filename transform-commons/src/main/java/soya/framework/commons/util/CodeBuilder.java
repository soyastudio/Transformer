package soya.framework.commons.util;

public class CodeBuilder {
    private static String[] indents = new String[]{
            "",
            "\t",
            "\t\t",
            "\t\t\t",
            "\t\t\t\t",
            "\t\t\t\t\t",
            "\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
    };

    private StringBuilder builder;

    private CodeBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public CodeBuilder append(String s) {
        builder.append(s);
        return this;
    }

    public CodeBuilder append(String s, int indent) {
        builder.append(indents[indent]).append(s);
        return this;
    }

    public CodeBuilder appendLine() {
        builder.append("\n");
        return this;
    }

    public CodeBuilder appendLine(String s) {
        builder.append(s).append("\n");
        return this;
    }

    public CodeBuilder appendLine(String s, int indent) {
        builder.append(indents[indent]).append(s).append("\n");
        return this;
    }

    public String toString() {
        return builder.toString();
    }

    public static CodeBuilder newInstance() {
        return new CodeBuilder(new StringBuilder());
    }
}
