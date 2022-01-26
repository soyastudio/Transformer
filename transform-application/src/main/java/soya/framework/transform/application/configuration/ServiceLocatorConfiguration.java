package soya.framework.transform.application.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import soya.framework.commons.pattern.ServiceLocator;
import soya.framework.commons.pattern.ServiceLocatorSingleton;

@Configuration
public class ServiceLocatorConfiguration {

    @Bean
    ServiceLocator serviceLocator(ApplicationContext applicationContext) {
        return ServiceLocatorSingleton.initializer().create(
                applicationContext::getBean
        );
    }
}
