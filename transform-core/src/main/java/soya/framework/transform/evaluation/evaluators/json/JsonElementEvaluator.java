package soya.framework.transform.evaluation.evaluators.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import soya.framework.transform.evaluation.EvaluateException;
import soya.framework.transform.evaluation.Evaluator;

public abstract class JsonElementEvaluator implements Evaluator {
    protected Gson gson = new Gson();

    @Override
    public String evaluate(String data) throws EvaluateException {
        JsonElement src = JsonParser.parseString(data);
        return gson.toJson(evaluate(src));
    }

    public abstract JsonElement evaluate(JsonElement jsonElement) throws EvaluateException;
}
