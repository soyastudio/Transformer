package soya.framework.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class StringTransformerChain implements StringTransformer {

    private final StringTransformer[] chain;

    private StringTransformerChain(StringTransformer[] chain) {
        this.chain = chain;
    }

    @Override
    public String transform(String src) throws TransformerException {
        String result = src;
        for(int i = 0; i < chain.length; i ++) {
            result = chain[i].transform(result);
        }
        return result;
    }

    public Future<String> transform(String src, ExecutorService executorService) throws TransformerException {
        Future<String> future = null;
        for(int i = 0; i < chain.length; i ++) {
            if(future == null) {
                future = executorService.submit(new TransformTask(src, chain[i]));
            } else {
                future = executorService.submit(new TransformTask(future, chain[i]));
            }

            try {
                Thread.sleep(15L);
            } catch (InterruptedException e) {
                throw new TransformerException(e);
            }
        }

        return future;
    }

    public static StringTransformerChain parse(String expression) {
        Builder builder = builder();
        if(expression.contains("->")) {
            String[] arr = expression.split("->");
            for(String exp: arr) {
                builder.add(exp);
            }
        }
        return builder.create();
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private List<StringTransformer> list = new ArrayList<>();

        private Builder() {
        }

        public Builder add(StringTransformer transformer) {
            list.add(transformer);
            return this;
        }

        public Builder add(String exp) {
            list.add((StringTransformer) TransformerFactory.getInstance().create(exp));
            return this;
        }

        public StringTransformerChain create() {
            if(list.size() == 0) {
                throw new IllegalStateException("No transformer defined.");
            }
            return new StringTransformerChain(list.toArray(new StringTransformer[list.size()]));
        }
    }

    static class TransformTask implements Callable<String> {
        private Future<String> future;
        private String src;
        private StringTransformer transformer;

        private TransformTask(String src, StringTransformer transformer) {
            this.src = src;
            this.transformer = transformer;
        }

        private TransformTask(Future<String> future, StringTransformer transformer) {
            this.future = future;
            this.transformer = transformer;
        }


        @Override
        public String call() throws Exception {
            if(src == null) {
                while(!future.isDone()) {
                    Thread.sleep(15L);
                }
                src = future.get();
            }

            return transformer.transform(src);
        }
    }
}
