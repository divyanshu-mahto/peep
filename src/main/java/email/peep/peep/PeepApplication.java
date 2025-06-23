package email.peep.peep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeepApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeepApplication.class, args);
	}

}
