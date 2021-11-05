package soya.framework.transform.application.service;

public interface CommandLineService {
    String help(String query);

    String execute(String cmd, String msg) throws Exception;
}
