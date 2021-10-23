package soya.framework.transform.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import soya.framework.commons.cli.CommandLines;
import soya.framework.transform.schema.SchemaCommands;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class CommandLinesConfiguration {

    public CommandLinesConfiguration() throws IOException {

    }

    public Properties getCommandLineProperties(Class<?> cl) {
        return CommandLines.getProperties(cl);
    }

    @Bean
    public CommandLines.Configuration schemaConfig() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("commandlines-configuration.properties");
        properties.load(inputStream);

        return CommandLines.configure(SchemaCommands.class, properties);
    }
}
