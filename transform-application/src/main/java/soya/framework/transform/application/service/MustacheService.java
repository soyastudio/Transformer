package soya.framework.transform.application.service;

import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import soya.framework.tool.MustacheCommands;

import java.util.Map;
import java.util.Properties;

@Service
public class MustacheService extends BusinessObjectService<MustacheCommands> {
    @Override
    protected String[] parse(Options options, Map<String, String> values, String msg, Properties properties) {
        return new String[0];
    }
}
