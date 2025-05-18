package isluel.edb.hate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "isluel.edb")
@EnableJpaRepositories(basePackages = "isluel.edb")
@SpringBootApplication
@ComponentScan(basePackages = "isluel.edb")
public class HateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HateApplication.class, args);
    }
}
