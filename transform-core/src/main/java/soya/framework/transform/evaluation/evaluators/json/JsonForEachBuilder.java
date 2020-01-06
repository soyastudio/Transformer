package soya.framework.transform.evaluation.evaluators.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import soya.framework.transform.evaluation.*;
import soya.framework.transform.evaluation.evaluators.AbstractEvaluatorBuilder;

@EvaluatorDef(name = "json_foreach")
public final class JsonForEachBuilder extends AbstractEvaluatorBuilder<JsonForEachBuilder.JsonForEach> {

    @Override
    public JsonForEach build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        JsonForEach eval = new JsonForEach();
        EvaluateFunction function = (EvaluateFunction) arguments[0];
        eval.evaluator = (JsonElementEvaluator) EvaluateEngine.getInstance().create(function, context);
        return eval;
    }

    static class JsonForEach extends JsonElementEvaluator {

        private JsonElementEvaluator evaluator;
        private JsonForEach() {
        }

        @Override
        public JsonElement evaluate(JsonElement jsonElement) throws EvaluateException {
            JsonArray result = new JsonArray();
            JsonArray array = jsonElement.getAsJsonArray();
            array.forEach(e -> {
                result.add(evaluator.evaluate(e));
            });

            return result;
        }
    }
}
