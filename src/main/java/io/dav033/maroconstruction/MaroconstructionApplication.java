package io.dav033.maroconstruction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"io.dav033.maroconstruction",
		"io.dav033.maroconstruction.mappers"
})
public class MaroconstructionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaroconstructionApplication.class, args);
	}

}
