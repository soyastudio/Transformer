package soya.framework.transform.evaluation.evaluators;

import soya.framework.transform.evaluation.EvaluationContext;
import soya.framework.transform.evaluation.EvaluatorBuildException;
import soya.framework.transform.evaluation.EvaluatorDef;

@EvaluatorDef(name="jsonpath", arguments = "jsonPath")
public class JsonPathEvaluatorBuilder extends EvaluatorBuilderSupport<JsonPathEvaluator> {
    @Override
    public JsonPathEvaluator build(String arguments, EvaluationContext context) throws EvaluatorBuildException {
        return null;
    }
}
