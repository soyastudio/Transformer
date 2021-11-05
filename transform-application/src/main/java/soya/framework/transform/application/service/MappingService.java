package soya.framework.transform.application.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.stereotype.Service;
import soya.framework.commons.cli.CommandLines;
import soya.framework.commons.util.PropertiesUtils;
import soya.framework.tool.MappingCommandLines;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

@Service
public class MappingService extends AbstractCommandLineService<MappingCommandLines> {

    @Override
    protected Properties configure() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("commandlines-configuration.properties");
        properties.load(inputStream);
        PropertiesUtils.compile(properties);

        return properties;
    }

    @Override
    protected Options extendCompileOptions(CommandLines.Configuration configuration) {
        Options options = new Options();

        options.addOption(Option.builder("b")
                .longOpt("bod")
                .hasArg(true)
                .desc("Business Object.")
                .build());

        configuration.getCompileOptions().getOptions().forEach(e -> {
            options.addOption(e);
        });

        options.getOptions().forEach(e -> {
            System.out.println("-" + e.getOpt());
        });

        return options;
    }

    @Override
    protected String[] compile(String cmd, String msg, Options options, Properties properties) throws ParseException {
        List<String> arguments = new ArrayList<>();
        CommandLine commandLine = compile(cmd, options);

        String action = commandLine.getOptionValue("a");
        arguments.add("-a");
        arguments.add(action);

        Method method = configuration.getCommandMethod(action);

        if (!commandLine.hasOption("b")) {
            StringTokenizer tokenizer = new StringTokenizer(cmd);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.length() > 0) {
                    arguments.add(token);
                }
            }
        } else {
            String bod = commandLine.getOptionValue("b");
            File dir = new File(properties.getProperty("workspace.bo.dir") + "/" + bod);

            CommandLines.Command command = method.getAnnotation(CommandLines.Command.class);
            for (CommandLines.Opt opt : command.options()) {
                String o = opt.option();
                String v = commandLine.getOptionValue(o);

                if ("x".equals(o)) {
                    arguments.add("-x");
                    arguments.add(properties.getProperty("workspace.cmm.dir") + "/BOD/Get" + bod + ".xsd");

                }

                if ("j".equals(o)) {
                    arguments.add("-j");
                    arguments.add(properties.getProperty("workspace.bo.dir") + "/" + bod + "/work/xpath-adjustment.properties");
                }

                if ("m".equals(o)) {
                    arguments.add("-j");
                    arguments.add(properties.getProperty("workspace.bo.dir") + "/" + bod + "/work/xpath-adjustment.properties");

                }

                if ("v".equals(o) && v != null) {
                    arguments.add("-v");
                    arguments.add(v);
                }
            }



            /*if ("o".endsWith(o)) {
                // ignore

            } else if (opt.required() && v == null) {
                if ("x".equals(o)) {
                    v = properties.getProperty("workspace.cmm.dir") + "/BOD/Get" + bod + ".xsd";
                }

                arguments.add("-x");
                arguments.add(v);


            } else if (v!= null && v.toUpperCase(Locale.ROOT).endsWith(".xlsx")) {
                File xlsx = new File(dir, "requirement/" + v);
                if (!xlsx.exists()) {
                    throw new IllegalArgumentException("File not found: " + xlsx);
                }

                arguments.add("-" + o);
                arguments.add(xlsx.getPath());
            }*/
        }

        StringBuilder builder = new StringBuilder();
        for (String arg : arguments) {
            builder.append(" ").append(arg);
        }

        String cl = builder.toString().trim();

        System.out.println(cl);

        return arguments.toArray(new String[arguments.size()]);
    }

    public String defaultCommandLine(String bod, String cmd) {

        Properties properties = configuration.getEnvironment();
        Method method = configuration.getCommandMethod(cmd);
        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + cmd);
        }

        File dir = new File(properties.getProperty("workspace.bo.dir") + "/" + bod);
        if(!dir.exists()) {
            throw new IllegalArgumentException("BOD does not exist: " + bod);
        }

        StringBuilder builder = new StringBuilder()
                .append("-a ").append(cmd);

        CommandLines.Command command = method.getAnnotation(CommandLines.Command.class);
        for (CommandLines.Opt opt : command.options()) {
            if ("x".equals(opt.option())) {
                builder.append(" -x ").append(properties.getProperty("workspace.cmm.dir") + "/BOD/Get" + bod + ".xsd");
            }

            if ("j".equals(opt.option())) {
                builder.append(" -j ").append(properties.getProperty("workspace.bo.dir") + "/" + bod + "/work/xpath-adjustment.properties");
            }

            if ("m".equals(opt.option())) {
                builder.append(" -m ").append(properties.getProperty("workspace.bo.dir") + "/" + bod + "/work/xpath-mappings.xlsx");
            }
        }

        return builder.toString();
    }
}
