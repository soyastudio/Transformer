package soya.framework.tool;

import org.apache.commons.cli.CommandLine;
import soya.framework.commons.cli.CommandLines;

public class MappingCommandLines {

    public static void main(String[] args) {
        try {
            String cmd = new StringBuilder("-a ")
                    .append("construct")
                    .append(" -x ").append("C:")
                    .append(" -f ").append("C:")
                    .toString();

            String result = CommandLines.execute(cmd, MappingCommandLines.class, null);
            System.out.println(result);

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "a",
                            required = true,
                            defaultValue = "sampleXml",
                            desc = "Command name."),
                    @CommandLines.Opt(option = "f",
                            required = true,
                            desc = "Input string, file or url"),
                    @CommandLines.Opt(option = "o",
                            desc = "Output file or path"),
                    @CommandLines.Opt(option = "x",
                            required = true,
                            desc = "Xsd or avsc file path.")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String construct(CommandLine cmd) {
        return "Hello";
    }
}
