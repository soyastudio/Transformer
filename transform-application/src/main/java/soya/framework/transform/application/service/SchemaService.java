package soya.framework.transform.application.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.stereotype.Service;
import soya.framework.commons.cli.CommandLines;
import soya.framework.commons.util.PropertiesUtils;
import soya.framework.transform.schema.SchemaCommands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class SchemaService extends AbstractCommandLineService<SchemaCommands> {

    @Override
    protected Properties configure() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("commandlines-configuration.properties");
        properties.load(inputStream);
        PropertiesUtils.compile(properties);

        return properties;
    }

    @Override
    protected String[] compile(String cmd, String msg, Options options, Properties properties) throws ParseException {
        List<String> arguments = new ArrayList<>();
        CommandLine commandLine = compile(cmd, options);

        String action = commandLine.getOptionValue("a");
        Method method = configuration.getCommandMethod(action);

        String bod = commandLine.getOptionValue("b");
        File dir = new File(properties.getProperty("workspace.bo.dir") + "/" + bod);

        CommandLines.Command command = method.getAnnotation(CommandLines.Command.class);
        for (CommandLines.Opt opt : command.options()) {
            String o = opt.option();
            String v = commandLine.getOptionValue(o);
            if (opt.required() && v == null) {
                throw new IllegalArgumentException("Option is required: " + o);
            }

            if (v.toLowerCase().endsWith(".xsd")) {
                if (v.startsWith("/")) {
                    v = properties.getProperty("workspace.cmm.dir") + v;

                } else {
                    v = properties.getProperty("workspace.cmm.dir") + "/" + v;
                }
            }

            arguments.add("-" + o);
            arguments.add(v);
        }


        return arguments.toArray(new String[arguments.size()]);
    }
}
