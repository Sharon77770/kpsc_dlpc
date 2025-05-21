package com.shaorn77770.kpsc_wargame;

import java.net.Socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.shaorn77770.kpsc_wargame.database.service.UserService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class KpscWargameApplication {

	public static void main(String[] args) {
		try {
            ProcessBuilder pb = new ProcessBuilder("docker", "info");
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Docker is running.");
            } else {
                System.out.println("Docker is not running.");
				return;
            }
        } catch (Exception e) {
            System.out.println("Docker is not installed or not running.");
			return;
        }

		String host = "localhost";
        int port = 3306;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("MySQL is running on " + host + ":" + port);
        } catch (Exception e) {
            System.out.println("MySQL is not running or not reachable.");
			return;
        }

		SpringApplication.run(KpscWargameApplication.class, args);
	}

}
