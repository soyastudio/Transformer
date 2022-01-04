package soya.framework.transform.application.service;

import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import soya.framework.transform.schema.SchemaCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class SchemaService extends BusinessObjectService<SchemaCommands> {

    @Override
    protected String[] parse(Options options, Map<String, String> values, String msg, Properties properties) {
        String bod = values.get("b");
        List<String> arguments = new ArrayList<>();
        options.getOptions().forEach(e -> {
            String o = e.getOpt();
            String v = values.get(o);

            if ("x".equals(o)) {
                File xsd = new File(properties.getProperty("workspace.cmm.dir") + "/BOD/Get" + bod + ".xsd");
                if (!xsd.exists()) {
                    throw new IllegalArgumentException("File does not exist: " + xsd);
                }

                v = xsd.getPath();
            }

            if ("i".equals(o)) {

            }

            arguments.add("-" + o);
            arguments.add(v);
        });

        return arguments.toArray(new String[arguments.size()]);
    }
}
