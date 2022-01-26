package soya.framework.commons.pattern;

public interface ServiceLocator {
    <T> T find(Class<T> type) throws ServiceLocatorException;

    class ServiceLocatorException extends RuntimeException {

    }
}
