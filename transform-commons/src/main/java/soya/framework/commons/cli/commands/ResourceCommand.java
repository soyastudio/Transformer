package soya.framework.commons.cli.commands;

import soya.framework.commons.cli.CommandCallable;
import soya.framework.commons.cli.CommandOption;
import soya.framework.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class ResourceCommand implements CommandCallable {

    @CommandOption(option = "s", longOption = "source", required = true)
    protected String source;

    @CommandOption(option = "t", longOption = "type")
    protected String resourceType;

    protected String contents() throws Exception {
        ResourceType type = null;
        if (resourceType == null) {
            type = guessType(source);

        } else {
            type = ResourceType.valueOf(resourceType);

        }

        switch (type) {
            case FILE:
                return fromFile(source);

            case URL:
                return fromUrl(source);

            default:
                return source;
        }
    }

    protected String fromFile(String path) throws IOException {
        return IOUtils.toString(new FileInputStream(path));
    }

    protected String fromUrl(String url) throws IOException {
        return IOUtils.toString(new URL(url).openStream());
    }

    public enum ResourceType {
        URL, FILE, PLAIN;
    }

    public static ResourceType guessType(String source) {
        if (isFile(source)) {
            return ResourceType.FILE;

        } else if (isURL(source)) {
            return ResourceType.URL;

        } else {
            return ResourceType.PLAIN;
        }
    }

    public static boolean isFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    public static boolean isURL(String source) {
        try {
            new URL(source);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
