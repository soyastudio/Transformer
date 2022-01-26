package soya.framework.commons.commandline;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

public class CommandAdapter {

    public static String execute(Class<?> cls, String methodName, Object[] args, CommandDelegate delegate) throws Exception {
        Method method = null;
        for (Method m : cls.getMethods()) {
            if (methodName.equals(m.getName())) {
                method = m;
                break;
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("Cannot find method: " + methodName);
        }

        CommandMapping clt = method.getAnnotation(CommandMapping.class);
        if (clt == null) {
            throw new IllegalArgumentException("Cannot find CommandLineTemplate on method: " + methodName);
        }

        StringTokenizer tokenizer = new StringTokenizer(clt.template());
        String[] arguments = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.startsWith("-")) {
                arguments[i] = token;

            } else if (token.startsWith("{{") && token.endsWith("}}")) {
                String p = token.substring(2, token.length() - 2);
                try {
                    int index = Integer.parseInt(p);
                    arguments[i] = args[index].toString();

                } catch (NumberFormatException ex) {
                    if (delegate.context().property(p) != null) {
                        arguments[i] = delegate.context().property(p);

                    } else if (System.getProperty(p) != null) {
                        arguments[i] = System.getProperty(p);

                    }
                }

            } else {
                arguments[i] = token;
            }
            i++;
        }

        String cmd = clt.command();
        if(cmd.startsWith("{{") && cmd.endsWith("}}")) {
            cmd = cmd.substring(2, cmd.length() - 2);
            int index = Integer.parseInt(cmd);

            cmd = args[index].toString();
        }

        return delegate.execute(cmd, arguments);
    }
}
