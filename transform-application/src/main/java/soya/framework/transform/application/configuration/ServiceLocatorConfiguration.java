package soya.framework.transform.application.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import soya.framework.commons.pattern.ServiceLocator;

@Configuration
public class ServiceLocatorConfiguration {

    @Bean
    ServiceLocator serviceLocator(ApplicationContext applicationContext) {
        return ServiceLocator.Singleton
                .initialize(applicationContext::getBean);
    }
}
