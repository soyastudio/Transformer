package soya.framework.commons.cli.commands;

import com.google.gson.*;
import soya.framework.commons.cli.Flow;

import java.util.Enumeration;

public class SessionInfoCallback implements Flow.Callback {
    @Override
    public void onSuccess(Flow.Session session) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject = new JsonObject();
        if (session.properties().size() > 0) {
            JsonArray arr = new JsonArray();
            Enumeration<?> enumeration = session.properties().propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                String value = session.properties().getProperty(key);

                JsonObject prop = new JsonObject();
                prop.addProperty("key", key);
                prop.addProperty("value", value);
                arr.add(prop);
            }

            jsonObject.add("properties", arr);
        }

        jsonObject.add("results", gson.toJsonTree(session.results()));

        System.out.println(gson.toJson(jsonObject));

    }
}
