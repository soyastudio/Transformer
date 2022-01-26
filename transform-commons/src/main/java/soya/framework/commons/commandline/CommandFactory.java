package soya.framework.commons.commandline;

public interface CommandFactory {
    CommandCallable create(String commandline, CommandDelegateContext ctx);
}
