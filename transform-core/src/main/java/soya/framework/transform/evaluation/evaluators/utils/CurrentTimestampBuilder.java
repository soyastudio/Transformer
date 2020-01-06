package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;
import soya.framework.transform.evaluation.evaluators.AbstractEvaluatorBuilder;

@EvaluatorDef(name="current_timestamp", arguments = "format")
public final class CurrentTimestampBuilder extends AbstractEvaluatorBuilder<CurrentTimestampBuilder.CurrentTimestamp> {

    @Override
    public CurrentTimestamp build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        CurrentTimestamp evaluator = new CurrentTimestamp();
        return evaluator;
    }

    static class CurrentTimestamp implements Evaluator {
        private CurrentTimestamp() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            return "" + System.currentTimeMillis();
        }
    }
}
