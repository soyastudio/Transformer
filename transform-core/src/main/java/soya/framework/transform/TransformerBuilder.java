package soya.framework.transform;

public interface TransformerBuilder<T extends Transformer> {
    T create(String[] args, TransformContext context);
}
