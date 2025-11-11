package com.shaorn77770.kpsc_wargame.database.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.shaorn77770.kpsc_wargame.data_class.ContainerData;
import com.shaorn77770.kpsc_wargame.data_class.UserData;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DockerService {
    private static final Logger logger = LogManager.getLogger(DockerService.class);
    private final UserService userService;

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
                    String logs = getContainerInfo(id);

                    list.add(new ContainerData(id, name, isRunning, logs));
                }
            }
        } catch (Exception e) {
            logger.error("getAllContainers 예외 발생", e);
        }
        return list;
    }

    public ContainerData getContainer(String name) {
        try {
            Process process = new ProcessBuilder("docker", "ps",
                "-a",
                "--filter", "name="+ name,                    
                "--format", "{{.ID}} {{.Names}} {{.Status}}"
            ).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                if (parts.length >= 3) {
                    String id = parts[0];
                    boolean isRunning = parts[2].toLowerCase().contains("up");

                    if(!name.startsWith("jupyter_")){
                        continue;
                    }

                    // 로그 가져오기
                    String logs = getContainerInfo(id);

                    return new ContainerData(id, name, isRunning, logs);
                }
            }
        } catch (Exception e) {
            logger.error("getContainer 예외 발생: {}", name, e);
        }
        return null;
    }

    public boolean stopContainer(String containerId) {
        boolean result = runCommand("docker", "stop", containerId);
        if (!result) logger.error("컨테이너 중지 실패: {}", containerId);
        return result;
    }

    public boolean startContainer(String containerId) {
        boolean result = runCommand("docker", "start", containerId);
        if (!result) logger.error("컨테이너 실행 실패: {}", containerId);
        return result;
    }

    private boolean runCommand(String... command) {
        try {
            Process process = new ProcessBuilder(command).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            logger.error("runCommand 예외 발생: {}", String.join(" ", command), e);
            return false;
        }
    }

    private String getContainerInfo(String containerId) {
        StringBuilder info = new StringBuilder();

        try {
            Process statsProcess = new ProcessBuilder("docker", "stats", "--no-stream", "--format",
                    "\"{{.CPUPerc}}|{{.MemUsage}}\"", containerId).start();

            BufferedReader statsReader = new BufferedReader(new InputStreamReader(statsProcess.getInputStream()));
            String statsLine = statsReader.readLine();
            if (statsLine != null) {
                statsLine = statsLine.replace("\"", "");
                String[] parts = statsLine.split("\\|");
                if (parts.length == 2) {
                    info.append("🧠 메모리 사용량: ").append(parts[1]).append("\n");
                    info.append("⚙️ CPU 사용량: ").append(parts[0]).append("\n");
                }
            } else {
                info.append("리소스 정보 없음 (docker stats 실패)\n");
            }

            Process inspectProcess = new ProcessBuilder("docker", "inspect", "-f",
                    "{{ range .Mounts }}{{ .Source }}{{ end }}", containerId).start();

            BufferedReader inspectReader = new BufferedReader(new InputStreamReader(inspectProcess.getInputStream()));
            String volumePath = inspectReader.readLine();

            if (volumePath != null && !volumePath.isEmpty()) {
                // du -sh 로 실제 사용량 확인
                Process duProcess = new ProcessBuilder("du", "-sh", volumePath).start();
                BufferedReader duReader = new BufferedReader(new InputStreamReader(duProcess.getInputStream()));
                String duLine = duReader.readLine();
                if (duLine != null && duLine.contains("\t")) {
                    String[] duParts = duLine.split("\t");
                    info.append("💾 디스크 사용량: ").append(duParts[0]).append(" (경로: ").append(volumePath).append(")").append("\n");
                }
            } else {
                info.append("스토리지 정보 없음 (볼륨 마운트 없음)\n");
            }

            Process gpuProcess = new ProcessBuilder(
                    "nvidia-smi",
                    "--query-gpu=name,utilization.gpu,memory.total,memory.used",
                    "--format=csv,noheader,nounits"
            ).start();

            BufferedReader gpuReader = new BufferedReader(new InputStreamReader(gpuProcess.getInputStream()));
            String gpuLine;
            int gpuIndex = 0;
            while ((gpuLine = gpuReader.readLine()) != null) {
                String[] gpuParts = gpuLine.split(",\\s*");
                if (gpuParts.length == 4) {
                    info.append(String.format("🎮 GPU%d (%s): 사용률 %s%%, 메모리 %sMB / %sMB\n",
                            gpuIndex++, gpuParts[0], gpuParts[1], gpuParts[3], gpuParts[2]));
                }
            }
        } 
        catch (Exception e) {
            logger.error("getContainerInfo 예외 발생: {}", containerId, e);
            info.append("⚠️ 리소스 정보를 불러오지 못했습니다.\n");
        }

        return info.toString();
    }

    public String makeContainer(UserData user, String domain) {
        try {
            String containerName = "jupyter_" + user.getStudentNumber();
            String apiKey = user.getApiKey();

            List<String> command = new ArrayList<>(Arrays.asList(
                "docker", "run", "-d",
                "--name", containerName
            ));

            if (isNvidiaSmiAvailable()) {
                command.addAll(Arrays.asList("--gpus", "all"));
            }

            if (user.getJupyterUrl() == null || user.getJupyterUrl().isEmpty()) {
                String port = getSafeRandomPort();
                command.addAll(Arrays.asList("-p", port + ":8888")); 
            } 
            else {
                command.addAll(Arrays.asList("-p", user.getPort())); 
            }

            command.addAll(Arrays.asList("-e", "JUPYTER_TOKEN=" + apiKey));

            // 루트 유저로 실행
            String setupScript = String.join(" && ", Arrays.asList(
                "jupyter lab " +
                "--NotebookApp.token=" + apiKey +
                " --NotebookApp.default_url=/lab" +
                " --NotebookApp.ip=0.0.0.0" +
                " --NotebookApp.allow_remote_access=True" +
                " --no-browser --port=8888"
            ));

            command.addAll(Arrays.asList(
                "nvcr.io/nvidia/pytorch:24.03-py3", 
                "bash", "-c", setupScript
            ));

            Process process = new ProcessBuilder(command).start();
            process.waitFor();

            String hostPort;
            if (user.getJupyterUrl() == null || user.getJupyterUrl().isEmpty()) {
                Process portProcess = new ProcessBuilder(
                    "docker", "port", containerName, "8888"
                ).start();
                portProcess.waitFor();

                try (Scanner scanner = new Scanner(portProcess.getInputStream())) {
                    String portLine = scanner.hasNextLine() ? scanner.nextLine() : null;

                    if (portLine != null && portLine.contains(":")) {
                        hostPort = portLine.split(":")[1].trim();
                    } 
                    else {
                        logger.error("makeContainer 에러 발생: host port: {}", portLine);
                        return null;
                    }
                }
            } 
            else {
                hostPort = user.getPort().split(":")[0].trim();
            }

            return "http://" + domain + ":" + hostPort + "/?token=" + apiKey;

        } 
        catch (Exception e) {
            logger.error("makeContainer 예외 발생: user={}, domain={}", user.getStudentNumber(), domain, e);
        }

        return null;
    }


    // nvidia-smi 명령어가 존재하는지 확인하는 메서드 추가
    private boolean isNvidiaSmiAvailable() {
        try {
            Process process = new ProcessBuilder("which", "nvidia-smi").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.warn("nvidia-smi 존재 확인 중 예외 발생", e);
            return false;
        }
    }

    public boolean removeContainer(UserData user) {
        String containerName = "jupyter_" + user.getStudentNumber();

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", containerName);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.error("도커 컨테이너 삭제 실패: {} (exitCode={})", containerName, exitCode);
                return false;
            }
        } 
        catch (Exception e) {
            logger.error("도커 컨테이너 삭제 실패(예외): {}", containerName, e);
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
                logger.warn("Docker 검색 명령어 실행 실패, exit code: {}", exitCode);
            }
        } 
        catch (Exception e) {
            logger.error("contains 예외 발생: {}", name, e);
        }
        return false;
    }

    private Set<Integer> getDockerUsedPorts() {
        Set<Integer> used = new HashSet<>();
        try {
            Process process = new ProcessBuilder(
                "docker", "ps", "--format", "{{.Ports}}"
            ).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->") && line.contains(":")) {
                    String beforeArrow = line.split("->")[0];
                    String portStr = beforeArrow.substring(beforeArrow.lastIndexOf(":") + 1);
                    if (portStr.matches("\\d+")) {
                        used.add(Integer.parseInt(portStr));
                    }
                }
            }
        } 
        catch (Exception e) {
            logger.error("Docker 사용중 포트 조회 실패", e);
        }
        return used;
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } 
        catch (Exception e) {
            return false;
        }
    }

    private String getSafeRandomPort() {
        Set<Integer> dockerUsed = getDockerUsedPorts();

        int port;
        do {
            port = 1024 + (int)(Math.random() * (65535 - 1024 + 1));
        } while (!isPortAvailable(port) || dockerUsed.contains(port));

        return String.valueOf(port);
    }
}
