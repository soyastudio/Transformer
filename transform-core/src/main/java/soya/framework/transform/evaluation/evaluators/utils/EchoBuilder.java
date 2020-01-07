package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;

@EvaluatorDef(name = "echo")
public class EchoBuilder implements EvaluatorBuilder<EchoBuilder.Echo> {

    @Override
    public Echo build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        Echo echo = new Echo();
        return echo;
    }

    static class Echo implements Evaluator {

        private Echo() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            System.out.println("========================== !!!");
            return data;
        }
    }
}
