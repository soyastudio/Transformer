package soya.framework.transform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface TransformService {
    TransformContext context();

    String transform(String data, String expression) throws TransformerException;

    Future<String> transform(String data, String expression, ExecutorService executorService) throws TransformerException;


}
