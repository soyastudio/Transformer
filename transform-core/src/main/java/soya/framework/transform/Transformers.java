package soya.framework.transform;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Transformers {
    private static ImmutableMap<String, TransformerBuilder> builders;

    protected Transformers() {
    }

    protected static void register(String... packageName) {
        Map<String, TransformerBuilder> map = new HashMap<>();
        if (builders != null) {
            map.putAll(builders);
        }

        Set<Class<?>> set = findByAnnotation(packageName);
        set.forEach(e -> {
            TransformerDef def = e.getAnnotation(TransformerDef.class);
            String name = def.name();
            TransformerBuilder transformerBuilder = newInstance(e);
            map.put(name, transformerBuilder);

        });

        builders = ImmutableMap.copyOf(map);
    }

    protected static TransformerBuilder getTransformerBuilder(String name) {
        return builders.get(name);
    }

    private static Set<Class<?>> findByAnnotation(String... packageName) {

        Set<Class<?>> set = new HashSet<>();
        try {
            ClassPath classpath = ClassPath.from(getClassLoader());
            for (String pkg : packageName) {
                Set<ClassPath.ClassInfo> results = classpath.getTopLevelClassesRecursive(pkg);
                results.forEach(e -> {
                    Class<?> cls = e.load();
                    if (cls.getAnnotation(TransformerDef.class) != null) {
                        set.add(cls);
                    }
                });
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return set;
    }

    private static TransformerBuilder newInstance(Class<?> c) {
        return null;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = TransformerFactory.class.getClassLoader();
        }

        return classLoader;
    }
}
