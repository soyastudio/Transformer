package soya.framework.transform.evaluation.evaluators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import soya.framework.transform.evaluation.*;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractEvaluatorBuilder<T extends Evaluator> implements EvaluatorBuilder<T> {
    protected Gson gson;
    protected Class<T> clazz;
    protected String[] properties;

    public AbstractEvaluatorBuilder() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        EvaluatorDef def = getClass().getAnnotation(EvaluatorDef.class);
        this.properties = def.arguments();
    }

    public Class<T> getEvaluatorType() {
        return clazz;
    }

    public Class<?> getInnerEvaluatorType() {
        Class<?>[] inners = getEvaluatorType().getDeclaredClasses();
        if (inners != null) {
            for (Class<?> c : inners) {
                if (Evaluator.class.isAssignableFrom(c)) {
                    return c;
                }
            }
        }

        return null;
    }

    public T create(String[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        try {
            return gson.fromJson(toJsonObject(clazz, properties, arguments, context), clazz);
        } catch (NoSuchFieldException e) {
            throw new EvaluatorBuildException(e);
        }
    }

    protected JsonObject toJsonObject(Class<?> clazz, String[] properties, String[] arguments, EvaluationContext context) throws NoSuchFieldException {
        JsonObject json = new JsonObject();
        int len = arguments.length;
        for (int i = 0; i < properties.length; i++) {
            if (i < len) {
                String property = properties[i];
                Class<?> type = getPropertyType(clazz, property);


            } else {
                break;
            }
        }

        return json;
    }

    protected Class<?> getPropertyType(Class<?> clazz, String property) throws NoSuchFieldException {
        return clazz.getDeclaredField(property).getType();
    }

}
