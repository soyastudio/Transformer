package soya.framework.commons.cli;

public interface CommandLineService {

    String help();

    String execute(String cmd, String msg) throws Exception;

}
