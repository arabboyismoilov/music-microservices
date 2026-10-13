package com.epam.musicserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class MusicServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicServiceRegistryApplication.class, args);
	}

}
