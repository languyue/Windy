package com.zj.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.zj.domain.repository.marker.ConfigCommandRepository;
import com.zj.domain.repository.marker.RuntimeCommandRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 表级唯一写 owner 的架构守卫。
 *
 * <p>repository 的写接口用 marker 父接口分类：{@link ConfigCommandRepository} 只能被
 * console 注入，{@link RuntimeCommandRepository} 只能被 master 注入。一旦有人越过进程边界
 * 直接持有对方的写接口，这里必须失败，而不是靠约定。</p>
 *
 * <p>被校验的字节码直接取各模块的 target/classes，不走 classpath 扫描：三个可执行模块是
 * spring-boot 重新打包的 fat jar，类在 BOOT-INF/classes 下，classpath 扫描会扫不到而出现
 * 规则空跑。</p>
 *
 * @author guyuelan
 */
public class ArchitectureGuardTest {

  /**
   * 参与校验的模块，路径相对仓库根目录
   */
  private static final List<String> ANALYZED_MODULES = Arrays.asList(
      "windy-contract", "windy-common", "windy-domain", "windy-master", "windy-client",
      "windy-console/windy-auth", "windy-console/windy-demand", "windy-console/windy-feature",
      "windy-console/windy-pipeline", "windy-console/windy-service",
      "windy-console/windy-metrics", "windy-console/windy-starter");

  /**
   * 每个模块至少有一个类被导入，否则说明路径配错，规则会静默通过
   */
  private static final List<String> REQUIRED_CLASSES = Arrays.asList(
      "com.zj.contract.json.ContractJson",
      "com.zj.contract.rpc.ResponseMeta",
      "com.zj.master.dispatch.pipeline.listener.PublishEndListener",
      "com.zj.client.handler.pipeline.executer.NodeExecutor",
      "com.zj.auth.service.RoleService",
      "com.zj.demand.service.IterationService",
      "com.zj.feature.service.FeatureService",
      "com.zj.pipeline.service.PipelineService",
      "com.zj.service.service.MicroserviceService",
      "com.zj.metrics.calculate.strategy.BaseMetric",
      "com.zj.starter.WindyApplication");

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importModuleClasses() {
    Path repositoryRoot = findRepositoryRoot();
    List<Path> classDirectories = new ArrayList<>();
    for (String module : ANALYZED_MODULES) {
      Path classDirectory = repositoryRoot.resolve(module).resolve("target").resolve("classes");
      assertTrue(Files.isDirectory(classDirectory),
          "模块未编译，找不到类目录: " + classDirectory + "，请先执行 mvn package");
      classDirectories.add(classDirectory);
    }
    importedClasses = new ClassFileImporter()
        .importPaths(classDirectories.toArray(new Path[0]));
  }

  @Test
  void importedClassesShouldCoverEveryModule() {
    Set<String> classNames = importedClasses.stream().map(JavaClass::getName).collect(Collectors.toSet());
    List<String> missingClasses = REQUIRED_CLASSES.stream().filter(name -> !classNames.contains(name))
        .collect(Collectors.toList());
    assertTrue(missingClasses.isEmpty(), "以下类没有被导入，架构规则会空跑: " + missingClasses);
  }

  /**
   * master 只读配置域与业务域，写配置域的 repository 一律不允许出现在 master 里
   */
  @Test
  void masterMustNotWriteConfigDomain() {
    ArchRule rule = noClasses().that().resideInAPackage("com.zj.master..")
        .should().dependOnClassesThat().areAssignableTo(ConfigCommandRepository.class)
        .because("配置域与业务域的唯一写 owner 是 console，master 只能依赖 Query 接口");
    rule.check(importedClasses);
  }

  /**
   * console 只读运行态域，写运行态表的 repository 一律不允许出现在 console 里
   */
  @Test
  void consoleMustNotWriteRuntimeDomain() {
    ArchRule rule = noClasses().that()
        .resideInAnyPackage("com.zj.auth..", "com.zj.demand..", "com.zj.feature..",
            "com.zj.pipeline..", "com.zj.service..", "com.zj.metrics..", "com.zj.starter..")
        .should().dependOnClassesThat().areAssignableTo(RuntimeCommandRepository.class)
        .because("运行态表的唯一写 owner 是 master，console 只能下发命令或经 Query 接口读取");
    rule.check(importedClasses);
  }

  /**
   * client 不连数据库：不依赖 domain（repository 与 PO）、不依赖 master、不依赖 console
   */
  @Test
  void clientMustNotDependOnDomainOrOtherProcesses() {
    ArchRule rule = noClasses().that().resideInAPackage("com.zj.client..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.zj.domain..", "com.zj.master..", "com.zj.service..")
        .because("client 只通过 contract 与 HTTP 与另外两个进程交互，不共享数据库");
    rule.check(importedClasses);
  }

  /**
   * contract 是三个进程共享的契约层，只能依赖 JDK、序列化、校验与插件 SDK，
   * 一旦反向依赖 common/domain 或任一业务进程，「跨进程只走契约」就不成立了
   */
  @Test
  void contractMustStaySelfContained() {
    ArchRule rule = classes().that().resideInAPackage("com.zj.contract..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "com.zj.contract..", "java..", "javax..", "com.fasterxml.jackson..",
            "lombok..", "com.zj.plugin..", "org.apache.commons.lang..", "org.apache.commons.lang3..")
        .because("windy-contract 必须自包含，否则 client/master/console 会重新共享业务代码");
    rule.check(importedClasses);
  }

  /**
   * 全链路只在「对象 ↔ 数据库 JSON 列」一处出现字符串，此外还剩 client 解析第三方报文
   * 这一个入口；fastjson 一旦进入契约/领域/公共/调度，就说明又有业务代码在手工拼 JSON
   */
  @Test
  void fastjsonMustStayInClient() {
    ArchRule rule = noClasses().that()
        .resideInAnyPackage("com.zj.contract..", "com.zj.common..", "com.zj.domain..", "com.zj.master..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.alibaba.fastjson..", "com.alibaba.fastjson2..")
        .because("业务交互一律用具体对象，JSON 序列化只能经 ContractJson/WireJson，第三方报文解析收敛在 client");
    rule.check(importedClasses);
  }

  private static Path findRepositoryRoot() {
    Path current = Paths.get("").toAbsolutePath();
    for (int i = 0; i < 5 && current != null; i++) {
      if (Files.isRegularFile(current.resolve("pom.xml"))
          && Files.isDirectory(current.resolve("windy-domain"))) {
        return current;
      }
      current = current.getParent();
    }
    throw new IllegalStateException("找不到仓库根目录，请从 windy-arch-test 模块目录执行测试");
  }
}
