package soya.framework.transform.schema.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {
    public static Properties yamlToProperties(String yaml) {
        Properties properties = new Properties();
        Yaml yml = new Yaml();
        Map<String, Object> map = yml.load(yaml);


        return properties;
    }

    public static String yamlToProperties(Properties properties) {


        return null;
    }

    public static JsonObject load(File dir, String baseName, InputStream template) {
        JsonObject jsonObject = new JsonObject();
        try {
            List<String> lines = IOUtils.readLines(template, "UTF-8");
            lines.forEach(e -> {
                if (e.contains("=")) {
                    int index = e.indexOf("=");
                    String key = e.substring(0, index);
                    String value = e.substring(index + 1).trim();
                    JsonObject prop = new JsonObject();
                    prop.addProperty("param", value);
                    jsonObject.add(key, prop);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().startsWith(baseName + ".")) {
                String key = null;
                if (file.getName().startsWith(baseName + ".dev")) {
                    key = "dev";
                } else if (file.getName().startsWith(baseName + ".qa")) {
                    key = "qa";
                } else if (file.getName().startsWith(baseName + ".prod")) {
                    key = "prod";
                }

                if (key != null) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(inputStream);

                        Enumeration enumeration = properties.propertyNames();
                        while (enumeration.hasMoreElements()) {
                            String propName = (String) enumeration.nextElement();
                            String propValue = properties.getProperty(propName);

                            JsonObject prop = null;
                            JsonElement jsonElement = jsonObject.get(propName);
                            if (jsonElement != null) {
                                prop = jsonElement.getAsJsonObject();
                            } else {
                                prop = new JsonObject();
                                jsonObject.add(propName, prop);
                            }

                            prop.addProperty(key, propValue);
                        }

                    } catch (IOException e) {

                    }
                }

            }
        }

        return jsonObject;
    }

    public static JsonObject load(File dir, String baseName) {
        JsonObject jsonObject = new JsonObject();

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().startsWith(baseName + ".")) {
                String key = null;
                if (file.getName().startsWith(baseName + ".dev")) {
                    key = "dev";
                } else if (file.getName().startsWith(baseName + ".qa")) {
                    key = "qa";
                } else if (file.getName().startsWith(baseName + ".prod")) {
                    key = "prod";
                }

                if (key != null) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        Properties properties = new Properties();
                        properties.load(inputStream);

                        Enumeration enumeration = properties.propertyNames();
                        while (enumeration.hasMoreElements()) {
                            String propName = (String) enumeration.nextElement();
                            String propValue = properties.getProperty(propName);

                            JsonObject prop = null;
                            JsonElement jsonElement = jsonObject.get(propName);
                            if (jsonElement != null) {
                                prop = jsonElement.getAsJsonObject();
                            } else {
                                prop = new JsonObject();
                                jsonObject.add(propName, prop);
                            }

                            prop.addProperty(key, propValue);
                        }

                    } catch (IOException e) {

                    }
                }

            }
        }


        return jsonObject;
    }

}
