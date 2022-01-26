package soya.framework.commons.cli2;

public interface CommandLineService {

    String help();

    String execute(String cmd, String msg) throws Exception;

}
