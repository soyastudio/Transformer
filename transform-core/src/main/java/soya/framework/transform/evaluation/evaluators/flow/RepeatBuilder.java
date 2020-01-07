package soya.framework.transform.evaluation.evaluators.flow;

import soya.framework.transform.evaluation.*;

@EvaluatorDef(name = "repeat")
public class RepeatBuilder implements EvaluatorBuilder<RepeatBuilder.Repeat> {

    @Override
    public Repeat build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        Repeat repeat = new Repeat();
        if(arguments[0] instanceof EvaluateParameter) {
            repeat.count = EvaluateParameter.intValue(arguments[0], context);
        }

        if(arguments.length > 1) {
            repeat.executor = Evaluators.create((EvaluateFunction)arguments[1], context);
        }

        return repeat;
    }

    static class Repeat implements Evaluator {
        private int count;
        private Evaluator executor;

        private Repeat() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            int index = 0;
            while(index < count) {
                executor.evaluate(data);
                index ++;
            }

            return null;
        }
    }
}
