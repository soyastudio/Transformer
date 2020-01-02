package soya.framework.transform;

public interface Renderer<T> {
    T render(String data) throws RenderException;
}
