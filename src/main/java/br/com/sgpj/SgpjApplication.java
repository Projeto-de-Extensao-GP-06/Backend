package br.com.sgpj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SgpjApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgpjApplication.class, args);
	}

}
