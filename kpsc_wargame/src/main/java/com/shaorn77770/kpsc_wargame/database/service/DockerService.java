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
                    String logs = getContainerInfo(id);

                    list.add(new ContainerData(id, name, isRunning, logs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ContainerData getContainer(String name) {
        try {
            Process process = new ProcessBuilder("docker", "ps",
                "-a",
                "--filter", "\"name="+ name + "\"",                    
                "--format", "\"{{.ID}} {{.Names}} {{.Status}}\""
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
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
            info.append("⚠️ 리소스 정보를 불러오지 못했습니다.\n");
        }

        return info.toString();
    }

    public String makeContianer(UserData user, String domain) {
        try {
            String containerName = "jupyter_" + user.getStudentNumber();

            List<String> command = new ArrayList<>(Arrays.asList(
                "docker", "run", "-d",
                "--name", containerName,
                "--gpus", "all"
            ));

            if (user.getJupyterUrl() == null || user.getJupyterUrl().isEmpty()) {
                command.addAll(Arrays.asList("-p", "0:8888")); // 무작위 포트
            } 
            else {
                command.addAll(Arrays.asList("-p", user.getPort()));
            }

            // sudo 설치 및 무비밀번호 설정 포함한 entrypoint를 bash -c 로 처리
            String apiKey = user.getApiKey();
            String setupScript = String.join(" && ", Arrays.asList(
                "apt update",
                "apt install -y sudo",
                "echo 'jovyan ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers",
                "start-notebook.sh " +
                "--NotebookApp.token=" + apiKey +
                " --NotebookApp.default_url=/lab" +
                " --NotebookApp.ip=0.0.0.0" +
                " --NotebookApp.allow_remote_access=True"
            ));

            // 환경 변수와 커맨드 추가
            command.addAll(Arrays.asList(
                "-e", "JUPYTER_TOKEN=" + apiKey,
                "jupyter/base-notebook",
                "bash", "-c", setupScript
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
                hostPort = user.getPort().split(":")[0].trim();
            }

            return "http://" + domain + ":" + hostPort + "/?token=" + apiKey;

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
