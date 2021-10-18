package soya.framework.transform.schema.util;

public class StringBuilderUtils {

    private StringBuilderUtils() {
    }

    public static void println(StringBuilder builder) {
        builder.append("\n");
    }

    public static void println(StringBuilder builder, int count) {
        if(count > 0) {
            for(int i = 0; i < count; i ++) {
                builder.append("\n");
            }
        }
    }

    public static void println(String text, StringBuilder builder) {
        builder.append(text).append("\n");
    }

    public static void println(String text, StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append(text).append("\n");
    }

    public static void indent(StringBuilder builder) {
        builder.append("\t");
    }

    public static void indent(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append("\t");
        }
    }
}
