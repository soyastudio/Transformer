package soya.framework.tool.ant;

import org.apache.tools.ant.taskdefs.Mkdir;
import soya.framework.commons.cli.Command;
import soya.framework.commons.cli.CommandOption;

import java.io.File;

@Command(name = "ant-mkdir", uri = "ant://mkdir")
public class MkdirCommand implements AntCommand {

    @CommandOption(option = "d", longOption = "dir", required = true)
    protected String dir;

    @Override
    public String call() throws Exception {

        File directory = new File(dir);
        Mkdir mkdir = new Mkdir();
        mkdir.setDir(directory);
        try {
            mkdir.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }


}
