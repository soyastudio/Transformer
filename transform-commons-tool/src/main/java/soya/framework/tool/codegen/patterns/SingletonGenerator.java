package soya.framework.tool.codegen.patterns;

import soya.framework.commons.cli.Command;
import soya.framework.tool.codegen.JavaCodeBuilderCommand;
import soya.framework.commons.util.CodeBuilder;

@Command(name = "java-singleton")
public class SingletonGenerator extends JavaCodeBuilderCommand {

    @Override
    protected void printBody(CodeBuilder builder) {
        builder.append("private static ", indent).append(className).append(" INSTANCE").appendLine(";");
        builder.appendLine();

        builder.appendLine("static {", indent);
        builder.append("INSTANCE = new ", indent + 1).append(className).appendLine("();");

        builder.appendLine("}", indent).appendLine();

        printDefaultConstructor("protected", builder);

        builder.append("public static ", indent).append(className).appendLine(" getInstance() {");
        builder.appendLine("return INSTANCE;", indent + 1);
        builder.appendLine("}", indent);

    }
}
