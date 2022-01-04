package soya.framework.transform.application.service;

import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import soya.framework.tool.MappingCommands;

import java.io.File;
import java.util.*;

@Service
public class TransformService extends BusinessObjectService<MappingCommands> {

    @Override
    protected String[] parse(Options options, Map<String, String> values, String msg, Properties properties) {
        String bod = values.get("b");
        File boDir = new File(boBaseDir, bod);
        if (!boDir.exists()) {
            throw new IllegalArgumentException("Bod does not exist: " + values.get("b"));
        }

        List<String> arguments = new ArrayList<>();
        options.getOptions().forEach(e -> {
            String o = e.getOpt();
            String v = values.get(o);

            if ("x".equals(o)) {
                File xsd = new File(cmmBaseDir, "/BOD/Get" + bod + ".xsd");
                if (!xsd.exists()) {
                    throw new IllegalArgumentException("File does not exist: " + xsd);
                }

                v = xsd.getPath();
            }

            if ("j".equals(o)) {
                if (v == null) {
                    v = new File(boDir, "work/xpath-adjustment.properties").getPath();
                } else {
                    File file = new File(boDir, v);
                    if (!file.exists()) {
                        throw new IllegalArgumentException("File does not exist: " + file);
                    }

                    v = file.getPath();
                }
            }

            if ("m".equals(o)) {
                if (v == null) {
                    v = new File(boDir, "work/xpath-mappings.xlsx").getPath();
                } else {
                    File file = new File(boDir, v);
                    if (!file.exists()) {
                        throw new IllegalArgumentException("File does not exist: " + file);
                    }

                    v = file.getPath();
                }
            }

            if (v != null) {
                arguments.add("-" + o);
                arguments.add(v);

            } else if (!e.hasArg() && values.containsKey(o)) {
                arguments.add("-" + o);

            }
        });

        return arguments.toArray(new String[arguments.size()]);
    }
}
