package soya.framework.transform.transformers;

import com.bazaarvoice.jolt.*;
import soya.framework.transform.TemplateBased;
import soya.framework.transform.TransformerException;

public class JoltTransformer extends JsonDataTransformer implements TemplateBased {

    private final String url;
    private JoltTransformType transformDsl = JoltTransformType.Chainr;
    private Transform transform;

    public JoltTransformer(String url) {
        this.url = url;
        try {
            Object spec = JsonUtils.jsonToObject(getClass().getClassLoader().getResourceAsStream(url));
            if (this.transformDsl == JoltTransformType.Sortr) {
                this.transform = new Sortr();

            } else {
                switch (this.transformDsl) {
                    case Shiftr:
                        this.transform = new Shiftr(spec);
                        break;
                    case Defaultr:
                        this.transform = new Defaultr(spec);
                        break;
                    case Removr:
                        this.transform = new Removr(spec);
                        break;
                    case Chainr:
                    default:
                        this.transform = Chainr.fromSpec(spec);
                        break;
                }
            }

        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Resource not found: " + url);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);

        }
    }

    @Override
    public String getUrl() {
        return url;
    }

    protected String process(String src) throws TransformerException {

        try {
            if (transform != null) {
                Object inputJSON = JsonUtils.jsonToObject(src);
                Object transformedOutput = transform.transform(inputJSON);
                return JsonUtils.toPrettyJsonString(transformedOutput);

            } else {
                return src;
            }

        } catch (Exception e) {
            throw new TransformerException(e);

        }
    }

    public enum JoltTransformType {
        Chainr,
        Shiftr,
        Defaultr,
        Removr,
        Sortr
    }
}
