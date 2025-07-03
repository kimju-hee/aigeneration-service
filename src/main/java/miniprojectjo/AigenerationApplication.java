package miniprojectjo;

import miniprojectjo.config.kafka.KafkaProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "miniprojectjo")
@EnableBinding(KafkaProcessor.class)
@EnableFeignClients
public class AigenerationApplication {

    public static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext =
            SpringApplication.run(AigenerationApplication.class, args);
    }
}
