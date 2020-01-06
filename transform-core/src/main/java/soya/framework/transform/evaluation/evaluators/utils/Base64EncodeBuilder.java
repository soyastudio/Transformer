package soya.framework.transform.evaluation.evaluators.utils;

import soya.framework.transform.evaluation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

@EvaluatorDef(name = "encode_base64")
public class Base64EncodeBuilder implements EvaluatorBuilder<Base64EncodeBuilder.Base64Encode> {

    @Override
    public Base64Encode build(EvaluateTreeNode[] arguments, EvaluationContext context) throws EvaluatorBuildException {
        Base64Encode evaluator = new Base64Encode();
        if (arguments.length > 0) {
            EvaluateParameter parameter = (EvaluateParameter) arguments[0];
            evaluator.compress = parameter.getBoolean(context);
        }
        return evaluator;
    }

    static class Base64Encode implements Evaluator {
        private boolean compress;

        private Base64Encode() {
        }

        @Override
        public String evaluate(String data) throws EvaluateException {
            byte[] bin = new byte[0];
            try {
                bin = data.getBytes("UTF-8");
                if (compress) {
                    bin = compress(bin);
                }
            } catch (IOException e) {
                throw new EvaluateException(e);
            }

            return Base64.getEncoder().encodeToString(bin);
        }

        private static byte[] compress(byte[] src) throws IOException {
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(src);
            gzip.flush();
            gzip.close();
            return obj.toByteArray();
        }
    }
}
