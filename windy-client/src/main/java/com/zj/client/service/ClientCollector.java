package com.zj.client.service;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.MavenConfigDto;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.contract.monitor.PhysicsCollect;
import com.zj.contract.rpc.ClientCollectDto;
import com.zj.contract.service.ToolLoadResult;
import com.zj.contract.service.ToolVersionDto;
import com.zj.contract.enums.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Slf4j
@Service
public class ClientCollector {

    @Value("${windy.client.version}")
    private String clientVersion;
    private final NodeStatusQueryLooper nodeStatusQueryLooper;

    public ClientCollector(NodeStatusQueryLooper nodeStatusQueryLooper) {
        this.nodeStatusQueryLooper = nodeStatusQueryLooper;
    }

    public ClientCollectDto getInstanceInfo() {
        ClientCollectDto clientCollectDto = new ClientCollectDto();
        PhysicsCollect physics = InstanceCollector.collectPhysics();
        clientCollectDto.setPhysics(physics);
        Integer waitQuerySize = nodeStatusQueryLooper.getWaitQuerySize();
        clientCollectDto.setWaitQuerySize(waitQuerySize);
        clientCollectDto.setVersion(clientVersion);
        return clientCollectDto;
    }

    public ToolLoadResult loadLocalToolVersion(ToolVersionDto toolVersionDto) {
        boolean toolExist = checkToolExist(toolVersionDto.getType(), toolVersionDto.getInstallPath());
        boolean configSetting = true;
        if (toolExist && Objects.equals(toolVersionDto.getType(), ToolType.MAVEN.getType())) {
            MavenConfigDto mavenConfig = new MavenConfigDto();
            mavenConfig.setMavenPath(toolVersionDto.getInstallPath());
            List<MavenConfigDto.RemoteRepository> repositories = JSON.parseArray(toolVersionDto.getBuildConfig(),
                    MavenConfigDto.RemoteRepository.class);
            mavenConfig.setRemoteRepositories(repositories);
            configSetting = MavenSettingHelper.configSetting(mavenConfig);
        }

        File localFile = new File(toolVersionDto.getInstallPath());
        ToolLoadResult toolLoadResult = new ToolLoadResult();
        toolLoadResult.setSuccess(localFile.exists() && localFile.isDirectory() && toolExist && configSetting);
        return toolLoadResult;
    }

    public boolean checkToolExist(String type, String installPath) {
        try {
            String command = exchangeCommand(type, installPath);
            ProcessBuilder processBuilder = new ProcessBuilder();

            // 根据操作系统选择不同的命令解释器
            if (isWindows()) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }

            Process process = processBuilder.start();

            // 读取标准输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("load shell info: " + line);
                }
            }

            // 读取错误输出
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("load shell error: " + errorLine);
                }
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error("get build tool ={} version error ", type, e);
        }
        return false;
    }

    private static String exchangeCommand(String type, String installPath) {
        // 统一处理路径分隔符
        String normalizedPath = installPath.replace("/", File.separator)
                .replace("\\", File.separator);

        StringBuilder command = new StringBuilder();

        if (Objects.equals(ToolType.JAVA.getType(), type)) {
            command.append(normalizedPath)
                    .append(File.separator).append("bin")
                    .append(File.separator).append("java -version");
        } else if (Objects.equals(ToolType.GO.getType(), type)) {
            command.append(normalizedPath)
                    .append(File.separator).append("bin")
                    .append(File.separator).append("go version");
        } else if (Objects.equals(ToolType.MAVEN.getType(), type)) {
            command.append(normalizedPath)
                    .append(File.separator).append("bin")
                    .append(File.separator).append("mvn -v");
        } else {
            command.append(normalizedPath);
        }

        return command.toString();
    }

    // 判断当前操作系统是否为Windows
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
