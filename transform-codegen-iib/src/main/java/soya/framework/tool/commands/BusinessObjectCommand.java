package soya.framework.tool.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import soya.framework.commons.cli.CommandCallable;
import soya.framework.commons.cli.CommandOption;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract class BusinessObjectCommand implements CommandCallable<String> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String CMM_DIR = "CMM";
    public static final String DIMENSIONS_DIR = "Dimensions";
    public static final String TEMPLATES_DIR = "Templates";

    public static final String REQUIREMENT_DIR = "requirement";
    public static final String WORK_DIR = "work";
    public static final String TEST_DIR = "test";
    public static final String DEPLOY_DIR = "deploy";
    public static final String HISTORY_DIR = "history";

    public static final String XLSX_MAPPINGS_FILE = "xpath-mappings.xlsx";
    public static final String XPATH_MAPPINGS_FILE = "xpath-mappings.properties";

    @CommandOption(option = "r", longOption = "home", required = true)
    protected String home;

    @CommandOption(option = "b", longOption = "bo", required = true)
    protected String businessObject;

    protected File homeDir;
    protected File cmmDir;
    protected File templateDir;

    protected File baseDir;
    protected File requirementDir;
    protected File workDir;
    protected File testDir;
    protected File deployDir;
    protected File historyDir;

    protected BusinessObject bod;

    @Override
    public String call() throws Exception {
        init();
        return execute();
    }

    protected void init() throws IOException {
        this.homeDir = new File(home);
        if (!homeDir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + homeDir);
        }

        cmmDir = new File(home, CMM_DIR);
        if (!cmmDir.exists()) {
            cmmDir.mkdir();
        }

        templateDir = new File(home, TEMPLATES_DIR);
        if (!templateDir.exists()) {
            templateDir.exists();
        }

        File file = new File(cmmDir, "BOD/Get" + businessObject + ".xsd");
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());

        } else {
            baseDir = new File(homeDir, "BusinessObjects/" + businessObject);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }

            this.requirementDir = new File(baseDir, REQUIREMENT_DIR);
            if (!requirementDir.exists()) {
                requirementDir.mkdir();
            }

            this.workDir = new File(baseDir, WORK_DIR);
            if (!workDir.exists()) {
                workDir.mkdir();
            }

            this.testDir = new File(baseDir, TEST_DIR);
            if (!testDir.exists()) {
                testDir.mkdir();
            }

            this.deployDir = new File(baseDir, DEPLOY_DIR);
            if (!deployDir.exists()) {
                deployDir.mkdir();
            }

            this.historyDir = new File(baseDir, HISTORY_DIR);
            if (!historyDir.exists()) {
                historyDir.mkdir();
            }

            File bodFile = new File(baseDir, "bod.json");
            if (bodFile.exists()) {
                bod = GSON.fromJson(new FileReader(bodFile), BusinessObject.class);
            }
        }
    }

    protected abstract String execute() throws Exception;

    public interface SchemaAware {

    }

    protected BusinessObject create(String name) {
        BusinessObject bo = new BusinessObject();

        Flow flow = new Flow();
        flow.name = "ESED_{{SRC}}_" + name + "_IH_Publisher";
        flow.packageName = "com.abs.{{SRC}}." + name;
        flow.source = "{{SRC}}";
        flow.sourceTopic = "{{SRC_TOPIC}}";
        flow.transformer = name + "_Transform_Compute";

        bo.flows.add(flow);

        return bo;
    }

    static class BusinessObject {
        private String name;
        private String version = "1.0";
        private String consumer = "EDM";

        private List<Flow> flows = new ArrayList<>();

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getConsumer() {
            return consumer;
        }

        public List<Flow> getFlows() {
            return flows;
        }
    }

    static Function[] toFunctions(String exp) {
        String[] arr = exp.split("::");
        Function[] functions = new Function[arr.length];
        for (int i = 0; i < arr.length; i++) {
            functions[i] = new Function(arr[i]);
        }

        return functions;
    }

    static class Flow {
        private String name;
        private String packageName;
        private String source;

        private String sourceTopic;
        private String targetTopic;

        private String transformer;
        private String mapping = XPATH_MAPPINGS_FILE;

        public String getName() {
            return name;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getSource() {
            return source;
        }

        public String getSourceTopic() {
            return sourceTopic;
        }

        public String getTransformer() {
            return transformer;
        }

        public String getTargetTopic() {
            return targetTopic;
        }

        public String getMapping() {
            return mapping;
        }
    }

    static class Mapping {

        String type;
        String cardinality;
        String rule;
        String source;
        String version;

        Set<String> assignments = new LinkedHashSet<>();

        String construction;
        Set<String> arrays = new LinkedHashSet<>();

        public Mapping(Function[] functions) {
            for (Function function : functions) {
                String name = function.getName();
                String value = function.getParameters()[0];

                if ("type".equals(name)) {
                    this.type = value;

                } else if ("cardinality".equals(name)) {
                    this.cardinality = value;

                } else if ("rule".equals(name)) {
                    this.rule = value;

                } else if ("source".equals(name)) {
                    this.source = value;

                } else if ("version".equals(name)) {
                    this.version = value;

                } else if ("assign".equals(name)) {
                    assignments.add(value);

                } else if ("CONSTRUCT".equals(name)) {
                    this.construction = value;

                } else if ("ARRAY".equals(name)) {
                    this.arrays.add(value);

                }
            }
        }

        public Mapping assign(String assignment) {
            if (assignment != null && assignment.trim().length() > 0) {
                this.assignments.add(assignment);
            }

            return this;
        }

        public Mapping construct(String var) {
            this.construction = var;
            return this;
        }

        public Mapping arrayMapping(String path) {
            this.arrays.add(path);
            return this;
        }

        public String construct() {
            StringBuilder builder = new StringBuilder();

            if (assignments.size() > 0) {
                assignments.forEach(e -> {
                    builder.append("assign(").append(e).append(")");
                });
            }

            if (construction != null) {
                builder.append("CONSTRUCT(").append(construction).append(")");
            }

            if (arrays.size() > 0) {
                arrays.forEach(e -> {
                    builder.append("::").append("ARRAY(").append(e).append(")");
                });
            }

            return builder.toString();
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("type(").append(type).append(")")
                    .append("::").append("cardinality(").append(cardinality).append(")");
            if (rule != null) {
                builder.append("::").append("rule(").append(rule).append(")");
            }

            if (source != null) {
                builder.append("::").append("source(").append(source).append(")");
            }

            if (version != null) {
                builder.append("::").append("version(").append(version).append(")");
            }

            if (assignments.size() > 0) {
                assignments.forEach(e -> {
                    builder.append("::").append("assign(").append(e).append(")");
                });
            }

            if (construction != null) {
                builder.append("::").append("CONSTRUCT(").append(construction).append(")");
            }

            if (arrays.size() > 0) {
                arrays.forEach(e -> {
                    builder.append("::").append("ARRAY(").append(e).append(")");
                });
            }


            return builder.toString();
        }
    }

    static class Function {
        private final String name;
        private final String[] parameters;

        public Function(String exp) {
            int first = exp.indexOf('(');
            int last = exp.indexOf(')');

            if (first < 1 || last < first) {
                name = "unknown";
                parameters = new String[]{exp};

            } else {
                name = exp.substring(0, first);
                parameters = exp.substring(first + 1, last).split(",");
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = parameters[i].trim();
                }

            }
        }

        public String getName() {
            return name;
        }

        public String[] getParameters() {
            return parameters;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder(name).append("(");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }

                builder.append(parameters[i]);
            }

            builder.append(")");

            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Function)) return false;

            Function function = (Function) o;

            return this.toString().equals(o.toString());
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }
    }

}
