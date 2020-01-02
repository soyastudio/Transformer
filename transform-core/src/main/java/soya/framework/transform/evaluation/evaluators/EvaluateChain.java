package soya.framework.transform.evaluation.evaluators;

import soya.framework.transform.evaluation.EvaluateException;
import soya.framework.transform.evaluation.Evaluator;

public class EvaluateChain implements Evaluator {
    Evaluator[] evaluators;

    @Override
    public String evaluate(String data) throws EvaluateException {
        String result = data;
        for (int i = 0; i < evaluators.length; i ++) {
            result = evaluators[i].evaluate(result);
        }
        return result;
    }
}
