package movieapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// annotation to mark this package as entry point of springboot app
// and discover all components in this package and subpackages
@SpringBootApplication
//@EnableSwagger2
public class MovieappApplication extends SpringBootServletInitializer {
	
	@Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
	    return builder.sources(MovieappApplication.class);
	  }

	public static void main(String[] args) {
		// Spring prend en charge l'application
		// en recevant en paramètre la classe principale
		// et scanne le package de l'application (et ses sous-packages)
		SpringApplication.run(MovieappApplication.class, args);
	}

}
