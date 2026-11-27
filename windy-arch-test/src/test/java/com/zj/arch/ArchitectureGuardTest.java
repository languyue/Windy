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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
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
      "windy-contract", "windy-common", "windy-remote", "windy-domain",
      "windy-master", "windy-client",
      "windy-console/windy-auth", "windy-console/windy-demand", "windy-console/windy-feature",
      "windy-console/windy-pipeline", "windy-console/windy-service",
      "windy-console/windy-metrics", "windy-console/windy-starter");

  /**
   * 每个模块至少有一个类被导入，否则说明路径配错，规则会静默通过
   */
  private static final List<String> REQUIRED_CLASSES = Arrays.asList(
      "com.zj.contract.json.ContractJson",
      "com.zj.contract.rpc.ResponseMeta",
      "com.zj.contract.port.IMasterApi",
      "com.zj.contract.rpc.NodeRecordStatus",
      "com.zj.common.entity.event.CodeChangePublishedEvent",
      "com.zj.common.exception.ApiException",
      "com.zj.remote.invoker.BaseEurekaAdapter",
      "com.zj.common.port.codechange.ICodeChangeReader",
      "com.zj.common.port.relation.IRelationTargetReader",
      "com.zj.demand.adapter.RelationTargetReader",
      "com.zj.pipeline.adapter.CodeChangeReader",
      "com.zj.domain.repository.pipeline.IPipelineCommandRepository",
      "com.zj.infrastructure.mapper.pipeline.PipelineMapper",
      "com.zj.infrastructure.repository.pipeline.PipelineRepository",
      "com.zj.master.dispatch.pipeline.listener.PublishEndListener",
      "com.zj.client.handler.pipeline.executer.NodeExecutor",
      "com.zj.auth.service.RoleService",
      "com.zj.demand.service.IterationService",
      "com.zj.feature.service.FeatureService",
      "com.zj.pipeline.service.PipelineService",
      "com.zj.service.service.MicroserviceService",
      "com.zj.metrics.calculate.strategy.BaseMetric",
      "com.zj.starter.WindyApplication");

  /**
   * 跨限界上下文依赖白名单：key 为业务域，value 为该域允许依赖的其他业务域（不含自身）。
   *
   * <p>矩阵来自存量代码的事实快照：auth 完全独立；metric 只读 demand；demand/pipeline
   * 已解耦为仅经事件与 common 端口协作；其余域之间保留业务上真实的引用。新增跨域依赖
   * 必须先解耦再登记到这张表，而不是随手 import。</p>
   */
  private static final Map<String, Set<String>> ALLOWED_CROSS_DOMAIN = new LinkedHashMap<>();

  static {
    ALLOWED_CROSS_DOMAIN.put("auth", Collections.emptySet());
    ALLOWED_CROSS_DOMAIN.put("demand", new LinkedHashSet<>(Arrays.asList("auth", "service")));
    ALLOWED_CROSS_DOMAIN.put("feature", new LinkedHashSet<>(Arrays.asList("demand", "service")));
    ALLOWED_CROSS_DOMAIN.put("pipeline", new LinkedHashSet<>(Arrays.asList("service")));
    ALLOWED_CROSS_DOMAIN.put("service",
        new LinkedHashSet<>(Arrays.asList("auth", "demand", "feature", "pipeline")));
    ALLOWED_CROSS_DOMAIN.put("metric", new LinkedHashSet<>(Arrays.asList("demand")));
  }

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
   * 端口与它的签名类型必须同层：com.zj.contract.port 里一旦出现 common/domain 的类型，
   * 就说明又有一个方法签名横跨两个模块，端口边界会退化成假的
   */
  @Test
  void contractPortsMustOnlyReferenceContractTypes() {
    ArchRule rule = classes().that().resideInAPackage("com.zj.contract.port..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("com.zj.contract..", "java..", "javax..")
        .because("跨进程端口的方法签名类型必须全部位于 windy-contract 内");
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

  /**
   * 持久化技术细节只能出现在 com.zj.infrastructure 包（与领域同属 windy-domain）：应用进程与领域包一律不许直接摸 mapper、PO
   */
  @Test
  void persistenceDetailMustStayInInfrastructure() {
    ArchRule rule = noClasses().that()
        .resideInAnyPackage("com.zj.auth..", "com.zj.demand..", "com.zj.feature..", "com.zj.pipeline..",
            "com.zj.service..", "com.zj.metrics..", "com.zj.starter..", "com.zj.master..", "com.zj.client..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.zj.infrastructure.mapper..", "com.zj.infrastructure.entity.po..")
        .because("PO 与 Mapper 只允许出现在 com.zj.infrastructure 包，应用层只能依赖仓储接口");
    rule.check(importedClasses);
  }

  /**
   * 领域包不允许被持久化框架污染：BO 与仓储接口不得 import MyBatis，也不得反向依赖持久化实现
   */
  @Test
  void domainMustNotDependOnPersistenceFramework() {
    ArchRule rule = noClasses().that()
        .resideInAnyPackage("com.zj.domain.entity.bo..", "com.zj.domain.repository..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.baomidou..", "org.springframework.web..", "org.apache.ibatis..")
        .because("领域层的 BO 与仓储接口必须与技术无关，持久化实现只能在 com.zj.infrastructure 包");
    rule.check(importedClasses);
  }

  /**
   * 模块合并后不得反向依赖：BO 与仓储接口在 windy-domain 内，持久化实现在 com.zj.infrastructure 包，
   * 领域包一旦 import 实现包，依赖倒置就退化成同模块内的约定
   */
  @Test
  void domainPackagesMustNotDependOnInfrastructure() {
    ArchRule rule = noClasses().that()
        .resideInAnyPackage("com.zj.domain.entity.bo..", "com.zj.domain.repository..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.zj.infrastructure..")
        .because("领域包只能定义 BO 与仓储接口，持久化实现不得被领域包反向引用");
    rule.check(importedClasses);
  }

  /**
   * console 的业务子模块与各层之间不得出现循环依赖
   */
  @Test
  void moduleSlicesShouldBeFreeOfCycles() {
    ArchRule rule = slices().matching("com.zj.(*)..")
        .should().beFreeOfCycles()
        .because("模块间循环依赖会让发布顺序与变更影响不可控");
    rule.check(importedClasses);
  }

  /**
   * 跨域依赖必须命中白名单：
   *
   * <p>demand 与 pipeline 只经 CodeChangePublishedEvent/CodePushedEvent 与 common 端口协作，
   * 双向均无编译期依赖；其余域的跨域引用被固化为白名单，新增即失败。</p>
   */
  @Test
  void crossDomainDependenciesMustFollowWhitelist() {
    ALLOWED_CROSS_DOMAIN.forEach((domain, allowed) -> {
      List<String> forbidden = ALLOWED_CROSS_DOMAIN.keySet().stream()
          .filter(other -> !Objects.equals(other, domain) && !allowed.contains(other))
          .collect(Collectors.toList());
      if (forbidden.isEmpty()) {
        return;
      }
      ArchRule rule = noClasses().that().resideInAnyPackage(bcPackages(domain))
          .should().dependOnClassesThat().resideInAnyPackage(bcPackages(forbidden))
          .because(domain + " 只允许依赖 " + allowed + "，新增跨域依赖必须先解耦再登记白名单");
      rule.check(importedClasses);
    });
  }

  /**
   * 一个业务域在各层（应用/领域/持久化实现）的包名集合
   */
  private static String[] bcPackages(String domain) {
    String consolePackage = "metric".equals(domain) ? "com.zj.metrics.." : "com.zj." + domain + "..";
    return new String[] {
        consolePackage,
        "com.zj.domain.entity.bo." + domain + "..",
        "com.zj.domain.repository." + domain + "..",
        "com.zj.infrastructure.repository." + domain + "..",
        "com.zj.infrastructure.entity.po." + domain + "..",
        "com.zj.infrastructure.mapper." + domain + ".."
    };
  }

  private static String[] bcPackages(List<String> domains) {
    return domains.stream().flatMap(domain -> Arrays.stream(bcPackages(domain)))
        .toArray(String[]::new);
  }

  /**
   * common 是共享端口层：只允许接口、DTO 与轻量注解，不得引入 Web/Eureka/OkHttp/Servlet
   */
  @Test
  void commonMustStayFrameworkFree() {
    ArchRule rule = noClasses().that().resideInAPackage("com.zj.common..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework.web..", "org.springframework.boot..", "org.springframework.cloud..",
            "com.netflix..", "okhttp3..", "javax.servlet..")
        .because("HTTP 客户端、服务发现与 Servlet 能力都属于 windy-remote，common 只保留进程内共享内核；跨进程协议接口已迁至 com.zj.contract.port");
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
