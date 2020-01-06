package soya.framework.transform.evaluation;

public interface EvaluatorBuilder<T extends Evaluator> {
    T build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException;
}
