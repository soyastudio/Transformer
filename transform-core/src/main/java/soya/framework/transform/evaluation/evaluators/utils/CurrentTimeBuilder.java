package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@EvaluatorDef(name="current_time", arguments = "format")
public class CurrentTimeBuilder implements EvaluatorBuilder<CurrentTimeBuilder.CurrentTime> {

    @Override
    public CurrentTime build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        CurrentTime evaluator = new CurrentTime();
        if(arguments.length > 0) {
            EvaluateParameter param = (EvaluateParameter) arguments[0];
            evaluator.format = param.getStringValue(context);
        }
        return evaluator;
    }

    static class CurrentTime implements Evaluator {
        private String format;
        private CurrentTime() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            Date date = new Date();
            if(format != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.format(date);
            } else{
                return date.toString();
            }
        }
    }
}
