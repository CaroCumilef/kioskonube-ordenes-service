package cl.kioskonube.ordenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KioskonubeOrdenesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KioskonubeOrdenesServiceApplication.class, args);
    }
}