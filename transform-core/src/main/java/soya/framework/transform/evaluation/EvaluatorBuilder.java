package soya.framework.transform.evaluation;

public interface EvaluatorBuilder<T extends Evaluator> {
    T build(String arguments, EvaluationContext context) throws EvaluatorBuildException;
}
