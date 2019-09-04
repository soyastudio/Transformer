package soya.framework.transform;

public interface Transformer<T, S> {
    T transform(S src) throws TransformerException;
}
