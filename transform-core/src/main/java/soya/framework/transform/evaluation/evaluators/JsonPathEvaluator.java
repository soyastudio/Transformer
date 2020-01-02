package soya.framework.transform.evaluation.evaluators;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import soya.framework.transform.evaluation.EvaluateException;
import soya.framework.transform.evaluation.Evaluator;

public class JsonPathEvaluator implements Evaluator {
    String jsonPath;

    @Override
    public String evaluate(String data) throws EvaluateException {
        Configuration JACKSON_JSON_NODE_CONFIGURATION = Configuration.builder().jsonProvider(new GsonJsonProvider())
                .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();

        Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider())
                .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();

        return JsonPath.using(conf).parse(data).read(jsonPath);
    }
}
