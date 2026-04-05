package com.pg.PaySim;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {


	@Value("${server.port}")
	static String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
        System.out.println("Application Started!! at http://localhost:" + serverPort == null ? "8080" : serverPort + "/");
	}

}
