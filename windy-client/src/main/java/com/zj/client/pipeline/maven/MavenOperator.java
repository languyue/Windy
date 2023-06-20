package com.zj.client.pipeline.maven;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Component
public class MavenOperator {

  public static final String DEPLOY = "deploy";
  public static final String SH_COMMAND_FORMAT = "nohup java -jar %s > app.log 2>&1 &";
  @Autowired
  private GlobalEnvConfig globalEnvConfig;

  public Integer build(String pomPath, String servicePath) throws Exception {
    File pomFile = new File(pomPath);
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(pomFile);
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(Arrays.asList("clean", "package"));

    String mavenDir = globalEnvConfig.getMavenPath();
    Preconditions.checkNotNull(mavenDir, "maven path can not find , consider to fix it");
    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenDir));
    ideaInvoker.setOutputHandler(System.out::println);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    copyJar2DeployDir(pomFile, servicePath);
    return ideaResult.getExitCode();
  }

  private void copyJar2DeployDir(File pomFile, String servicePath) throws Exception {
    Collection<File> files = FileUtils.listFiles(pomFile.getParentFile(), TrueFileFilter.INSTANCE,
        TrueFileFilter.INSTANCE);
    File jarFile = files.stream().filter(file -> file.isFile() && file.getName().endsWith(".jar"))
        .findAny().orElse(null);
    if (Objects.isNull(jarFile)) {
      throw new ApiException(ErrorCode.NOT_FIND_JAR);
    }


    String destDir = servicePath + File.separator + DEPLOY;
    File dir = new File(destDir);
    createSHFileIfNeed(jarFile.getName(), destDir, dir);

    FileUtils.copyToDirectory(jarFile, dir);
  }

  private void createSHFileIfNeed(String jarName, String destDir, File dir) {
    Collection<File> deployFiles = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE,
        TrueFileFilter.INSTANCE);
    File shFile = deployFiles.stream().filter(file -> file.isFile() && file.getName().endsWith(".sh"))
        .findAny().orElse(null);
    if (Objects.isNull(shFile) || !shFile.exists()) {
      createDefaultSHFile(destDir, jarName);
    }
  }

  private void createDefaultSHFile(String destDir, String name) {
    CharSink sink = Files.asCharSink(new File(destDir + File.separator + "start.sh"),
        Charsets.UTF_8, FileWriteMode.APPEND);
    try {
      String command = String.format(SH_COMMAND_FORMAT, name);
      sink.write(command);
    } catch (IOException e) {
      log.error("write event to file error", e);
    }
  }
}
