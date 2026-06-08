package br.com.sgpj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableAsync
@EnableScheduling
@EnableMethodSecurity
@SpringBootApplication
public class  SgpjApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgpjApplication.class, args);
	}

}
