package soya.framework.commons.cli;

import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.xml.sax.InputSource;
import soya.framework.commons.io.IOUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class Resources {

    public enum ResourceType {
        URL, FILE, PLAIN;
    }

    private Resources() {
    }

    public static String get(String source) throws Exception {
        ResourceType type = guessType(source);

        switch (type) {
            case FILE:
                return fromFile(source);

            case URL:
                return fromUrl(source);

            default:
                return source;
        }
    }

    protected static String fromFile(String path) throws IOException {
        return IOUtils.toString(new FileInputStream(path));
    }

    protected static String fromUrl(String url) throws IOException {
        return IOUtils.toString(new URL(url).openStream());
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

    public static boolean isJson(String source) {
        String token = source.trim();
        if(token.startsWith("[") && token.endsWith("]")
                || token.startsWith("{") && token.endsWith("}")) {
            try {
                JsonParser.parseString(source);
                return true;

            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isXml(String source) {
        try {
            DocumentBuilderFactory
                    .newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(source)));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBas64Encoded(String source) {
        // TODO
        return false;
    }

    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public static void compile(Properties properties) {

        int num = properties.size();
        while(true) {
            Properties values = new Properties();
            properties.entrySet().forEach(e -> {
                String v = (String) e.getValue();
                if (!v.contains("${")) {
                    values.setProperty((String) e.getKey(), v);
                }
            });

            if(values.size() == num) {
                break;

            } else {
                num = values.size();

            }

            Enumeration<?> enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                String value = properties.getProperty(key);

                if (value.contains("${")) {
                    if (value.contains("${" + key + "}")) {
                        throw new IllegalArgumentException("Self referenced for " + "${" + key + "}");
                    }

                    value = StrSubstitutor.replace(value, values);

                    if(value.contains("${")) {
                        value = StrSubstitutor.replace(value, System.getProperties());
                    }

                    properties.setProperty(key, value);

                }
            }
        }
    }


}
