package soya.framework.transform;

public interface Deserializer<T, S> {
    T deserialize(S src);
}
