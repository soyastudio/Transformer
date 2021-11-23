package soya.framework.transform.application.service;

import org.apache.commons.cli.Options;
import org.springframework.stereotype.Service;
import soya.framework.tool.ProjectCommandLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class ProjectService extends BusinessObjectService<ProjectCommandLines> {

    @Override
    protected String[] parse(Options options, Map<String, String> values, String msg, Properties properties) {
        List<String> args = new ArrayList<>();

        args.add("-w");
        args.add(boBaseDir.getPath());

        args.add("-b");
        args.add(values.get("b"));

        options.getOptions().forEach(e -> {
            String opt = e.getOpt();
            String value = values.get(opt);
            System.out.println("============= " + opt);



            if(value != null) {
                args.add("-" + opt);
                args.add(value);
            }

        });


        return args.toArray(new String[args.size()]);
    }
}
