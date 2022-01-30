package soya.framework.commons.cli;

import java.lang.reflect.Field;

public abstract class CommandDispatcher {

    private final CommandExecutor _executor;

    public CommandDispatcher(CommandExecutor delegate) {
        _executor = delegate;
    }

    protected String _help(String cmd) {
        return _executor.context().toString(cmd);
    }

    protected String _dispatch(String methodName, Object[] args) throws Exception {
        CommandExecutor commandExecutor = null;
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (CommandExecutor.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                CommandExecutor delegate = (CommandExecutor) field.get(this);
                if(dispatchable(methodName, delegate)) {
                    commandExecutor = delegate;
                    break;
                }
            }
        }

        if(commandExecutor == null) {
            throw new IllegalArgumentException("Can..........");
        }

        return CommandExecutor.execute(getClass(), methodName, args, commandExecutor);
    }

    protected String _execute(String commandline) throws Exception {
        return _executor.execute(commandline);
    }

    private boolean dispatchable(String methodName, CommandExecutor delegate) {

        return true;
    }
}
