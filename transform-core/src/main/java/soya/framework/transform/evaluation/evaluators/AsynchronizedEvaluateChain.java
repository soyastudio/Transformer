package soya.framework.transform.evaluation.evaluators;

import soya.framework.transform.evaluation.EvaluateException;
import soya.framework.transform.evaluation.Evaluator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsynchronizedEvaluateChain implements Evaluator {
    private ExecutorService executorService;
    private Evaluator[] evaluators;

    @Override
    public String evaluate(String data) throws EvaluateException {
        Future<String> future = null;
        for (int i = 0; i < evaluators.length; i++) {
            if (future == null) {
                future = executorService.submit(new EvaluateTask(data, evaluators[i]));
            } else {
                future = executorService.submit(new EvaluateTask(future, evaluators[i]));
            }

            try {
                Thread.sleep(15L);
            } catch (InterruptedException e) {
                throw new EvaluateException(e);
            }
        }
        return null;
    }


    static class EvaluateTask implements Callable<String> {
        private Future<String> future;
        private String src;
        private Evaluator evaluator;

        private EvaluateTask(String src, Evaluator evaluator) {
            this.src = src;
            this.evaluator = evaluator;
        }

        private EvaluateTask(Future<String> future, Evaluator evaluator) {
            this.future = future;
            this.evaluator = evaluator;
        }


        @Override
        public String call() throws Exception {
            if (src == null) {
                while (!future.isDone()) {
                    Thread.sleep(15L);
                }
                src = future.get();
            }

            return evaluator.evaluate(src);
        }
    }
}
