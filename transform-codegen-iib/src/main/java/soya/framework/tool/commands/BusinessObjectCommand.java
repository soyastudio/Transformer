package soya.framework.tool.commands;

import java.io.File;

public abstract class BusinessObjectCommand implements Command {
    public static final String CMM_DIR = "CMM";
    public static final String DIMENSIONS_DIR = "Dimensions";
    public static final String TEMPLATES_DIR = "Templates";

    public static final String REQUIREMENT_DIR = "requirement";
    public static final String WORK_DIR = "work";
    public static final String TEST_DIR = "test";
    public static final String DEPLOY_DIR = "deploy";
    public static final String HISTORY_DIR = "history";

    public static final String XLSX_MAPPINGS_FILE = "mappings.xslx";
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

    @Override
    public String call() throws Exception {
        init();
        return execute();
    }

    protected abstract String execute() throws Exception;

    protected void init() {
        this.baseDir = base();

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
    }

    protected File base() {
        this.homeDir = new File(home);
        if (!homeDir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + homeDir);
        }

        cmmDir = new File(home, CMM_DIR);
        if(!cmmDir.exists()) {
            cmmDir.mkdir();
        }

        templateDir = new File(home, TEMPLATES_DIR);
        if(!templateDir.exists()) {
            templateDir.exists();
        }

        File baseDir = new File(homeDir, "BusinessObjects/" + businessObject);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        return baseDir;

    }

    protected File workDir() {
        File baseDir = base();
        File workDir = new File(baseDir, "work");
        if (!workDir.exists()) {
            workDir.mkdirs();
        }

        return workDir;
    }
}
