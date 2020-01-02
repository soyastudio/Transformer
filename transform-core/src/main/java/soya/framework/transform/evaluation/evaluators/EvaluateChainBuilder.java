package soya.framework.transform.evaluation.evaluators;

import soya.framework.transform.evaluation.EvaluationContext;
import soya.framework.transform.evaluation.EvaluatorBuildException;
import soya.framework.transform.evaluation.EvaluatorDef;

@EvaluatorDef(name = "chain")
public class EvaluateChainBuilder extends EvaluatorBuilderSupport<EvaluateChain> {

    @Override
    public EvaluateChain build(String arguments, EvaluationContext context) throws EvaluatorBuildException {
        return null;
    }
}
