package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.Flow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystemProcessCallback implements Flow.Callback {
    private String baseDir;
    private List<Processor> chain;

    private FileSystemProcessCallback(String baseDir, List<Processor> chain) {
        this.baseDir = baseDir;
        this.chain = chain;
    }

    public Builder builder(String baseDir) {
        return new Builder(baseDir);
    }

    @Override
    public void onSuccess(Flow.Session session) throws Exception {

    }

    public static class Builder {
        private String baseDir;
        private List<Processor> processors = new ArrayList<>();

        private Builder(String baseDir) {
            this.baseDir = baseDir;
        }

        public Builder mkdir(String dir) {
            processors.add(new Mkdir(dir));
            return this;
        }

        public Builder createFile(String fileName, String source) {
            processors.add(new CreateFile());
            return this;
        }

        public FileSystemProcessCallback create() {
            return new FileSystemProcessCallback(baseDir, processors);
        }
    }

    interface Processor {
        void execute(File base, Flow.Session session) throws Exception;
    }

    public static class Mkdir implements Processor {
        private String dir;

        public Mkdir(String dir) {
            this.dir = dir;
        }

        @Override
        public void execute(File base, Flow.Session session) throws Exception {
            File directory = new File(base, dir);
            directory.mkdirs();

        }
    }

    public static class CreateFile implements Processor {
        private String fileName;
        private String source;
        private boolean overwrite;

        @Override
        public void execute(File base, Flow.Session session) throws Exception {

        }
    }

}
