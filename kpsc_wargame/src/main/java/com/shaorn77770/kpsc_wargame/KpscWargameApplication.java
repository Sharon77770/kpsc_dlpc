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
		try {
            ProcessBuilder pb = new ProcessBuilder("docker", "info");
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Docker is running.");
            } else {
                logger.error("Docker is not running.");
				return;
            }
        } catch (Exception e) {
            logger.error("Docker is not installed or not running.", e);
			return;
        }

		String host = "localhost";
        int port = 3306;

        try (Socket socket = new Socket(host, port)) {
            logger.info("MySQL is running on {}:{}", host, port);
        } catch (Exception e) {
            logger.error("MySQL is not running or not reachable.", e);
			return;
        }

		SpringApplication.run(KpscWargameApplication.class, args);
	}

}
