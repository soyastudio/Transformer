package soya.framework.transform.evaluation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluateEngine extends Evaluators {
    protected static EvaluateEngine instance;

    private EvaluationContext context;
    private ExecutorService executorService;

    static {
        instance = new EvaluateEngine();
        register(Evaluator.class.getPackage().getName());
    }

    private EvaluateEngine(EvaluationContext context, ExecutorService executorService) {
        this.context = context;
        this.executorService = executorService;
    }

    protected EvaluateEngine() {
        this.context = new DefaultEvaluateContext();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public String evaluate(String data, String expression) {
        String result = data;
        EvaluateFunction[] functions = EvaluateFunction.toFunctions(expression);
        for(EvaluateFunction function: functions) {
            result = create(function, context).evaluate(result);
        }

        return result;
    }

    public static EvaluateEngine getInstance() {
        return instance;
    }

    static class DefaultEvaluateContext implements EvaluationContext {

    }
}
