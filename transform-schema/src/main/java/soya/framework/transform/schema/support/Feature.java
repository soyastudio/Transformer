package soya.framework.transform.schema.support;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import soya.framework.transform.schema.Annotatable;

public abstract class Feature<T> implements Annotatable<T> {

    private static Gson gson = new Gson();

    private final T origin;
    protected JsonObject annotations = new JsonObject();

    protected Feature(T origin) {
        this.origin = origin;
    }

    public T origin() {
        return origin;
    }

    @Override
    public void annotate(String namespace, Object annotation) {
        if (annotation == null) {
            annotations.remove(namespace);
        } else {
            annotations.add(namespace, gson.toJsonTree(annotation));
        }
    }

    @Override
    public Object getAnnotation(String namespace) {
        return annotations.get(namespace);
    }

    @Override
    public <A> A getAnnotation(String namespace, Class<A> annotationType) {
        JsonElement v = annotations.get(namespace);
        if(v == null) {
            return null;
        }

        return gson.fromJson(v, annotationType);
    }
}
