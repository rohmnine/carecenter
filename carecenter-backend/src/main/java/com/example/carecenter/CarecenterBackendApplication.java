package com.example.carecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.carecenter.config.RsaKeyProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class CarecenterBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarecenterBackendApplication.class, args);
	}

}