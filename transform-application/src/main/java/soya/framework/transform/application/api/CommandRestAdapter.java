package soya.framework.transform.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import soya.framework.commons.commandline.CommandAdapter;
import soya.framework.commons.commandline.CommandDelegate;

import java.lang.reflect.Field;

public abstract class CommandRestAdapter {

    protected String delegate(String methodName, Object[] args) throws Exception {
        CommandDelegate commandDelegate = null;
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (CommandDelegate.class.isAssignableFrom(field.getType()) && field.getAnnotation(Autowired.class) != null) {
                field.setAccessible(true);
                commandDelegate = (CommandDelegate)field.get(this);
            }
        }
        return CommandAdapter.execute(getClass(), methodName, args, commandDelegate);
    }
}
