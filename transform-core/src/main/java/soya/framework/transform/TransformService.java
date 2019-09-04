package soya.framework.transform;

public abstract class TransformService {

    protected static TransformService INSTANCE;

    protected TransformService() {
    }

    public static TransformService getInstance() {
        return INSTANCE;
    }

    public abstract <T> T transform(String jsonData, String sourceType, Class<T> targetType, Object caller) throws Exception;
}
