package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;

import java.util.UUID;

@EvaluatorDef(name="uuid", arguments = "format")
public class UuidBuilder implements EvaluatorBuilder<UuidBuilder.Uuid> {

    @Override
    public Uuid build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        Uuid uuid = new Uuid();
        return uuid;
    }

    static class Uuid implements Evaluator {
        private String format;

        private Uuid() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            return UUID.randomUUID().toString();
        }
    }
}
