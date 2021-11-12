package soya.framework.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import soya.framework.commons.cli.CommandLines;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

public class ProjectCommandLines {

    private static String REQUIREMENT_DIR = "requirement";
    private static String WORK_DIR = "work";
    private static String TEST_DIR = "test";
    private static String HISTORY_DIR = "history";

    private static String XPATH_SCHEMA_FILE = "xpath-schema.properties";
    private static String XPATH_MAPPING_FILE = "xpath-mapping.properties";
    private static String XPATH_ADJUSTMENT_FILE = "xpath-adjustment.properties";

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
    public static String create(CommandLine cmd) throws Exception {
        File base = new File(cmd.getOptionValue("w"));
        String bod = cmd.getOptionValue("b");

        File boDir = new File(base, bod);
        if(boDir.exists()) {

        } else {
            FileUtils.forceMkdir(boDir);

            File reqDir = new File(boDir, REQUIREMENT_DIR);
            FileUtils.forceMkdir(reqDir);

            File workDir = new File(boDir, WORK_DIR);
            FileUtils.forceMkdir(workDir);

            File testDir = new File(boDir, TEST_DIR);
            FileUtils.forceMkdir(testDir);

            File histDir = new File(boDir, HISTORY_DIR);
            FileUtils.forceMkdir(histDir);

        }


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
    public static String readme(CommandLine cmd) throws Exception {
        File base = new File(cmd.getOptionValue("w"));
        String bod = cmd.getOptionValue("b");
        File boDir = new File(base, bod);

        File readme = new File(boDir, "readme.md");

        return IOUtils.toString(new FileInputStream(readme), Charset.defaultCharset());
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "b",
                            required = true,
                            desc = "Business Object Name"),
                    @CommandLines.Opt(option = "v",
                            required = true,
                            desc = "Version"),
                    @CommandLines.Opt(option = "w",
                            required = true,
                            desc = "Workspace directory")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String version(CommandLine cmd) throws Exception {
        System.out.println("================ versioning...");
        return null;
    }

    @CommandLines.Command(
            desc = "Convert xml to json against xml or avro schema",
            options = {
                    @CommandLines.Opt(option = "b",
                            required = true,
                            desc = "Business Object Name"),
                    @CommandLines.Opt(option = "v",
                            required = true,
                            desc = "Version"),
                    @CommandLines.Opt(option = "w",
                            required = true,
                            desc = "Workspace directory")
            },
            cases = {"-a xmlToJson -x SCHEMA_FILE_PATH -i INPUT -o OUTPUT_FILE"}
    )
    public static String cutoff(CommandLine cmd) throws Exception {

        System.out.println("================ cutoff...");

        return null;
    }


}
