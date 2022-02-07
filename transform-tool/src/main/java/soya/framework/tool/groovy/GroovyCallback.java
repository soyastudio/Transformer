package soya.framework.tool.groovy;

import soya.framework.commons.cli.Flow;
import soya.framework.commons.cli.Resources;

public class GroovyCallback implements Flow.Callback {

    private String source;

    private GroovyCallback(String resource) {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onSuccess(Flow.Session session) throws Exception {
        String script = Resources.get(source);
    }

    public static class Builder {
        private String script;

        private Builder() {
        }

        public Builder script(String script) {
            return this;
        }

        public GroovyCallback create() {
            return new GroovyCallback(script);
        }

    }
}
