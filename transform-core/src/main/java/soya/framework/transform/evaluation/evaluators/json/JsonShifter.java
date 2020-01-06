package soya.framework.transform.evaluation.evaluators.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import soya.framework.transform.evaluation.*;

public class JsonShifter extends JsonElementEvaluator {
    Shifter[] shifters;

    @Override
    public JsonElement evaluate(JsonElement jsonElement) throws EvaluateException {
        JsonObject target = new JsonObject();
        for (Shifter shifter : shifters) {
            String to = shifter.to;
            JsonElement value = shifter.evaluate(jsonElement);

            target.add(to, value);
        }

        return target;
    }

    static class Shifter extends JsonElementEvaluator {

        String to;
        EvaluateTreeNode from;

        @Override
        public JsonElement evaluate(JsonElement jsonElement) throws EvaluateException {
            if (from == null) {
                return jsonElement;
            }

            if (from instanceof EvaluateParameter) {
                String propName = ((EvaluateParameter) from).getValue();
                return jsonElement.getAsJsonObject().get(propName);

            } else if(from instanceof EvaluateFunction) {
                EvaluateFunction fun = (EvaluateFunction) from;
                Evaluator evaluator = Evaluators.create(fun, null);
                String st = evaluator.evaluate(jsonElement.toString());
                return new JsonPrimitive(st);
            }

            return JsonNull.INSTANCE;
        }
    }
}
