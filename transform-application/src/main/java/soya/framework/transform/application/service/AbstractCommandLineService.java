package soya.framework.transform.application.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import soya.framework.commons.cli.CommandLines;
import soya.framework.tool.MappingCommandLines;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class AbstractCommandLineService<T> implements CommandLineService {

    protected Class<T> commandLineClass;
    protected CommandLines.Configuration configuration;

    protected Options compileOptions;

    @PostConstruct
    protected void init() throws Exception {
        this.commandLineClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.configuration = CommandLines.configure(commandLineClass, configure());
        this.compileOptions = extendCompileOptions(this.configuration);

    }

    @Override
    public String help(String query) {
        return configuration.help();
    }

    @Override
    public String execute(String cmd, String msg) throws Exception {
        return CommandLines.execute(compile(cmd, msg, compileOptions, configuration.getEnvironment()), commandLineClass);
    }

    protected abstract Properties configure() throws IOException;

    protected Options extendCompileOptions(CommandLines.Configuration configuration) throws ParseException {
        return configuration.getCompileOptions();
    }

    protected abstract String[] compile(String cmd, String msg, Options options, Properties properties) throws ParseException;

    protected CommandLine compile(String cmd, Options options) throws ParseException {
        List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(cmd);
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            list.add(token);
        }

        return new DefaultParser().parse(options, list.toArray(new String[list.size()]));
    }


}
