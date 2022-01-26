package soya.framework.transform.commands;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.text.StrSubstitutor;
import soya.framework.commons.cli2.CommandLines;
import soya.framework.transform.schema.SchemaCommands;

import java.util.Properties;

public class TRANSFORMER {

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.setProperty("edis.cmm.home", "C:/Users/qwen002/IBM/IIBT10/workspace/APPDEV_ESED1_SRC_TRUNK/esed1_src/CMM_dev");

            CommandLines.configure(SchemaCommands.class, properties);

            String result = CommandLines.execute(args, SchemaCommands.class, new DefaultEvaluator());
            System.out.println(result);

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class DefaultEvaluator implements CommandLines.Evaluator {

        @Override
        public String evaluate(String v, Options options, Properties properties) {
            String token = v;
            while (token.contains("$[")) {
                token = token.replace("$[", "${").replace("]", "}");
            }

            token = StrSubstitutor.replace(token, properties);

            return token;
        }
    }

}
