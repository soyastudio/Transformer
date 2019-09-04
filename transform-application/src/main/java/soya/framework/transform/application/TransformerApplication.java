package soya.framework.transform.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"soya.framework.transform.application"})
public class TransformerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransformerApplication.class, args);
    }
}
