package soya.framework.tool;

import org.apache.commons.cli.CommandLine;
import soya.framework.commons.cli2.CommandLines;

public class MustacheCommands {

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "t",
                            required = true,
                            desc = "Business Object Name"),
                    @CommandLines.Opt(option = "w",
                            required = true,
                            desc = "Workspace directory")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String template(CommandLine cmd) throws Exception {
        return null;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "t",
                            required = true,
                            desc = "Business Object Name"),
                    @CommandLines.Opt(option = "w",
                            required = true,
                            desc = "Workspace directory")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String model(CommandLine cmd) throws Exception {
        return null;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "b",
                            required = true,
                            desc = "Business Object Name"),
                    @CommandLines.Opt(option = "w",
                            required = true,
                            desc = "Workspace directory")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String generate(CommandLine cmd) throws Exception {
        return null;
    }
}
