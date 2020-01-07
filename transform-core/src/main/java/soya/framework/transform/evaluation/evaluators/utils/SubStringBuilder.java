package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;

@EvaluatorDef(name = "sub_string")
public class SubStringBuilder implements EvaluatorBuilder<SubStringBuilder.SubString> {

    @Override
    public SubString build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        SubString subString = new SubString();
        if (arguments.length == 1) {
            EvaluateParameter parameter = (EvaluateParameter) arguments[0];
            subString.end = parameter.getInteger(context);

        } else if (arguments.length == 2) {
            EvaluateParameter p0 = (EvaluateParameter) arguments[0];
            EvaluateParameter p1 = (EvaluateParameter) arguments[1];
            subString.start = p0.getInteger(context);
            subString.end = p1.getInteger(context);

        } else {
            throw new IllegalFunctionArgumentException();
        }

        return subString;
    }

    static class SubString implements Evaluator {
        private int start = -1;
        private int end;

        private SubString() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            if (data == null) {
                return null;
            }

            if (start >= 0 && end >= start) {
                if (data.length() < start) {
                    return null;
                } else if (data.length() < end) {
                    return data.substring(start, data.length());
                } else {
                    return data.substring(start, end);
                }
            } else if (start < 0 && end > 0) {
                if(data.length() > end) {
                    return null;
                } else {
                    return data.substring(end);
                }
            } else {
                return null;
            }
        }
    }
}
