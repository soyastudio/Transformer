package soya.framework.transform;

public interface Serializer<T, S> {
    T serialize(S source);
}
