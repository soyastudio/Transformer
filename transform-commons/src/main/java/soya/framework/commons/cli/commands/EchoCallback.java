package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.Flow;

public class EchoCallback implements Flow.Callback {
    private final String expression;
    private Flow.Evaluator evaluator = new Flow.DefaultEvaluator();

    public EchoCallback(String expression) {
        this.expression = expression;
    }

    public EchoCallback setEvaluator(Flow.Evaluator evaluator) {
        if (evaluator != null) {
            this.evaluator = evaluator;
        }
        return this;
    }

    @Override
    public void onSuccess(Flow.Session session) throws Exception {
        System.out.println(evaluator.evaluate(expression, session));
    }
}
