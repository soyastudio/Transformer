package soya.framework.commons.pattern;

public class ServiceLocatorSingleton implements ServiceLocator {
    private static ServiceLocatorSingleton me;
    private final ServiceLocator serviceLocator;

    private ServiceLocatorSingleton(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public static Initializer initializer() {
        if(me != null) {
            throw new IllegalStateException("ServiceLocator has already created.");
        }

        return new Initializer();
    }

    public static ServiceLocator getInstance() {
        return me;
    }

    @Override
    public <T> T find(Class<T> type) throws ServiceLocatorException {
        return serviceLocator.find(type);
    }

    public static class Initializer {
        public ServiceLocator create(ServiceLocator serviceLocator) {
            if(serviceLocator == null) {
                throw new IllegalArgumentException("ServiceLocator cannot be null!");
            }
            return new ServiceLocatorSingleton(serviceLocator);
        }

    }

}
