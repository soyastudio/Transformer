package soya.framework.tool.ant;

import soya.framework.commons.cli.Flow;

public class AntCallback implements Flow.Callback {
    private String dir;

    private String antFile;

    private String target;

    private AntCallback(String dir, String antFile, String target) {
        this.dir = dir;
        this.antFile = antFile;
        this.target = target;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onSuccess(Flow.Session session) {

    }

    public static class Builder {
        private String dir;

        private String antFile;

        private String target;

        private Builder() {
        }

        public AntCallback create() {
            return new AntCallback(dir, antFile, target);
        }

        public Builder baseDir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder antFile(String antFile) {
            this.antFile = antFile;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }
    }
}
