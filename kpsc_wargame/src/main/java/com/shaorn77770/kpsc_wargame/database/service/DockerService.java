package com.shaorn77770.kpsc_wargame.database.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.shaorn77770.kpsc_wargame.data_class.ContainerData;
import com.shaorn77770.kpsc_wargame.data_class.UserData;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DockerService {
    public List<ContainerData> getAllContainers() {
        List<ContainerData> list = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("docker", "ps", "-a", "--format", "{{.ID}} {{.Names}} {{.Status}}").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                if (parts.length >= 3) {
                    String id = parts[0];
                    String name = parts[1];
                    boolean isRunning = parts[2].toLowerCase().contains("up");

                    if(!name.startsWith("jupyter_")){
                        continue;
                    }

                    // 로그 가져오기
                    String logs = getContainerLogs(id);

                    list.add(new ContainerData(id, name, isRunning, logs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean stopContainer(String containerId) {
        return runCommand("docker", "stop", containerId);
    }

    public boolean startContainer(String containerId) {
        return runCommand("docker", "start", containerId);
    }

    private boolean runCommand(String... command) {
        try {
            Process process = new ProcessBuilder(command).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getContainerLogs(String containerId) {
        StringBuilder logs = new StringBuilder();
        try {
            Process process = new ProcessBuilder("docker", "logs", "--tail", "30", containerId).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logs.append(line).append("\n");
            }
        } catch (Exception e) {
            logs.append("로그를 불러오지 못했습니다.");
        }
        return logs.toString();
    }

public String makeContianer(UserData user, String domain, int mem) {
    try {
        String containerName = "jupyter_" + user.getStudentNumber();

        String osName = System.getProperty("os.name").toLowerCase();
        boolean isLinux = osName.contains("linux");

        List<String> command = new ArrayList<>(Arrays.asList(
            "docker", "run", "-d",
            "--name", containerName
        ));

        // 리눅스에서만 storage-opt 옵션 적용
        if (isLinux) {
            command.addAll(Arrays.asList("--storage-opt", "size=" + mem + "g"));
        }

        if (user.getJupyterUrl() == null || user.getJupyterUrl().isEmpty()) {
            command.addAll(Arrays.asList("-p", "0:8888")); // 무작위 포트
        } else {
            command.addAll(Arrays.asList("-p", user.getPort())); 
        }

        // 환경 변수 및 Jupyter 설정
        command.addAll(Arrays.asList(
            "-e", "JUPYTER_TOKEN=" + user.getApiKey(),
            "jupyter/base-notebook",
            "start-notebook.sh",
            "--NotebookApp.token=" + user.getApiKey(),
            "--NotebookApp.default_url=/lab",
            "--NotebookApp.ip=0.0.0.0",
            "--NotebookApp.allow_remote_access=True"
        ));

        // 컨테이너 실행
        Process process = new ProcessBuilder(command).start();
        process.waitFor();

        // 무작위 포트인 경우에만 실제 포트 확인
        String hostPort;
        if (user.getJupyterUrl() == null || user.getJupyterUrl().isEmpty()) {
            Process portProcess = new ProcessBuilder(
                "docker", "port", containerName, "8888"
            ).start();

            try (Scanner scanner = new Scanner(portProcess.getInputStream())) {
                String portLine = scanner.hasNextLine() ? scanner.nextLine() : null;
                if (portLine != null && portLine.contains(":")) {
                    hostPort = portLine.split(":")[1].trim();
                } else {
                    return null;
                }
            }
        } else {
            // 이미 등록된 포트 사용
            hostPort = user.getPort().split(":")[0].trim();
        }

        // 최종 접속 URL 생성
        String jupyterUrl = "http://" + domain + ":" + hostPort + "/?token=" + user.getApiKey();
        return jupyterUrl;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}


    public boolean removeContainer(UserData user) {
        String containerName = "jupyter_" + user.getStudentNumber();

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", containerName);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("도커 컨테이너 삭제 실패: " + containerName);
                return false;
            }
        } 
        catch (Exception e) {
            System.err.println("도커 컨테이너 삭제 실패: " + containerName);
            return false;
        }

        return true;
    } 

    public boolean contains(String name) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "ps", "-a", "--filter", "name=" + name, "--format", "{{.Names}}");
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals(name)) {
                        return true;  // 정확히 일치하는 컨테이너 이름이 있으면 true 반환
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Docker 검색 명령어 실행 실패, exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
