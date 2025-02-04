package com.zj.client.handler.pipeline.build.go;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.pipeline.build.CodeBuildContext;
import com.zj.client.handler.pipeline.build.IBuildNotifyListener;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Component
public class GoCodeBuilder implements ICodeBuilder {

    @Value("${windy.build.path:/opt/windy/build}")
    private String buildVersionPath;

    @Override
    public String codeType() {
        return ToolType.GO.getType();
    }

    @Override
    public Integer build(CodeBuildContext context, IBuildNotifyListener notifyListener) {
        log.info("start build go code = {}", JSON.toJSONString(context));
        File targetFile = new File(context.getBuildFile());
        String buildFilePath = context.getTargetDir() + File.separator + "go_build.sh";
        notifyListener.notifyMessage("执行构建文件目录:" + buildFilePath);
        copyBuildFile(buildFilePath);
        String goPath = context.getBuildPath();
        ProcessBuilder processBuilder = new ProcessBuilder(buildFilePath, context.getServiceName(), "1.0.0",
                targetFile.getParentFile().getAbsolutePath(), goPath, context.getTargetDir());
        processBuilder.redirectErrorStream(true); // 合并标准错误流和标准输出流
        try {
            // 启动进程
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 实时读取输出
            String line;
            while ((line = reader.readLine()) != null) {
                notifyListener.notifyMessage(line);
            }

            int exitCode = process.waitFor();
            log.info("go build file execute result = {}", exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            log.info("execute shell error", e);
            notifyListener.notifyMessage(ExceptionUtils.getSimplifyError(e));
        }
        return -1;
    }

    private static void copyBuildFile(String targetPath) {
        try {
            Resource resource = new ClassPathResource("build/go_build.sh");
            File targetFile = new File(targetPath);
            // 确保目标目录存在
            targetFile.getParentFile().mkdirs();
            FileUtils.touch(targetFile);
            boolean isExecutable = targetFile.setExecutable(true, false);
            log.info("config shell file access model result={}", isExecutable);
            FileUtils.copyInputStreamToFile(resource.getInputStream(), targetFile);
        } catch (IOException e) {
            log.info("can not copy go build file", e);
        }
    }
}
