package soya.framework.transform.evaluation;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import soya.framework.transform.TransformerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Evaluators {
    private static ImmutableMap<String, EvaluatorBuilder> builders;

    protected static void register(String... packageName) {
        Map<String, EvaluatorBuilder> map = new HashMap<>();
        if (builders != null) {
            map.putAll(builders);
        }

        Set<Class<?>> set = findByAnnotation(packageName);
        set.forEach(e -> {
            EvaluatorDef def = e.getAnnotation(EvaluatorDef.class);
            String name = def.name();
            EvaluatorBuilder builder = newInstance(e);
            map.put(name, builder);

        });

        builders = ImmutableMap.copyOf(map);
    }

    public static Evaluator create(EvaluateFunction function, EvaluationContext context) {
        return builders.get(function.getName()).build(function.getArguments(), context);
    }

    public static String toJson(EvaluateTreeNode... nodes) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(nodes);
    }

    private static EvaluatorBuilder newInstance(Class<?> clazz) throws EvaluatorBuildException {
        try {
            return (EvaluatorBuilder) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new EvaluatorBuildException(e);
        }
    }

    private static Set<Class<?>> findByAnnotation(String... packageName) {
        Set<Class<?>> set = new HashSet<>();
        try {
            ClassPath classpath = ClassPath.from(getClassLoader());
            for (String pkg : packageName) {
                Set<ClassPath.ClassInfo> results = classpath.getTopLevelClassesRecursive(pkg);
                results.forEach(e -> {
                    Class<?> cls = e.load();
                    if (cls.getAnnotation(EvaluatorDef.class) != null) {
                        set.add(cls);
                    }
                });
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return set;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = TransformerFactory.class.getClassLoader();
        }

        return classLoader;
    }
}
