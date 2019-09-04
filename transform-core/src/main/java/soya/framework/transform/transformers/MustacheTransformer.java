package soya.framework.transform.transformers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import soya.framework.commons.json.GsonUtils;
import soya.framework.transform.TemplateBased;
import soya.framework.transform.TransformerException;

import java.io.InputStream;
import java.io.InputStreamReader;

public class MustacheTransformer extends JsonDataTransformer implements TemplateBased {
    private final String url;
    private Template template;

    public MustacheTransformer(String url) {
        this.url = url;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(url);
            template = Mustache.compiler().compile(new InputStreamReader(is));

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

    protected String process(String data) throws TransformerException {
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(data);
            Object variables = GsonUtils.toStruct(jsonElement);

            return template.execute(variables);

        } catch (Exception e) {
            throw new TransformerException(e);

        }
    }
}
