package com.shaorn77770.kpsc_wargame;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KpscWargameApplication {

	private static final Logger logger = LogManager.getLogger(KpscWargameApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KpscWargameApplication.class, args);
	}

}
