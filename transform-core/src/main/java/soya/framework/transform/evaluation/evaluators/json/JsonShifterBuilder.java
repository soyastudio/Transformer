package soya.framework.transform.evaluation.evaluators.json;

import soya.framework.transform.evaluation.*;
import soya.framework.transform.evaluation.evaluators.AbstractEvaluatorBuilder;

import java.util.ArrayList;
import java.util.List;

@EvaluatorDef(name = "json_shifter")
public class JsonShifterBuilder extends AbstractEvaluatorBuilder<JsonShifter> {

    @Override
    public JsonShifter build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        JsonShifter evaluator = new JsonShifter();

        List<JsonShifter.Shifter> shifters = new ArrayList<>();
        if(arguments.length == 1 && (arguments[0] instanceof EvaluateArray)) {

        } else {
            for(EvaluateTreeNode node: arguments) {
                EvaluateFunction func = (EvaluateFunction) node;
                shifters.add(create(func, context));
            }
        }

        evaluator.shifters = shifters.toArray(new JsonShifter.Shifter[shifters.size()]);

        return evaluator;
    }

    private JsonShifter.Shifter create(EvaluateFunction func, EvaluationContext context) {
        JsonShifter.Shifter sh = new JsonShifter.Shifter();

        for(EvaluateTreeNode arg: func.getArguments()) {
            if(sh.to == null) {
                sh.to = ((EvaluateParameter) arg).getStringValue(context);
            } else if(sh.from == null) {
                sh.from = arg;
            }
        }
        return sh;
    }
}
