package com.zj.contract.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  SUCCESS(200, "Common.000000", "请求成功"),
  ERROR(500, "Common.000001", "请求失败"),
  PARAM_VALIDATE_ERROR(400, "Common.000002", "参数校验失败"),
  STATUS_UNCHANGEABLE_ERROR(400, "Common.00003", "状态不可变更"),
  /*===================流水线=================*/
  NOT_FOUND_PIPELINE(404, "Pipeline.000002", "流水线未找到"),
  NOT_FOUND_CODE_CHANGE(404, "Pipeline.000003", "服务变更未找到"),
  NOT_FOUND_PIPELINE_GIT_BIND(404, "Pipeline.000004", "流水关联分支未找到"),
  UPDATE_PIPELINE_ERROR(500, "Pipeline.000005", "更新流水线失败"),
  NOT_FOUND_SERVICE(404, "Pipeline.000006", "未找到服务"),
  REQUEST_GIT_SERVER_FAILED(500, "Pipeline.000007", "请求Git服务错误"),
  DELETE_PIPELINE_ERROR(500, "Pipeline.000008", "删除流水线错误"),
  BRANCH_ALREADY_BIND(500, "Pipeline.000009", "分支已经绑定"),
  CREATE_PIPELINE(500, "Pipeline.000010", "创建流水线失败"),
  NOT_FIND_BRANCH(400, "Pipeline.000011", "没有找到绑定分支，无法执行"),
  PIPELINE_NOT_BIND(500, "Pipeline.000012", "流水线未绑定分支，无法发布"),
  SERVICE_BRANCH_PUBLISH_EXIST(500, "Pipeline.000013", "当前分支已推送发布，请查看发布列表"),
  PUBLISH_PIPELINE_EXIST(400, "Pipeline.000014", "当前服务已存在发布流水线,无法创建"),
  NOT_FIND_PIPELINE(400, "Pipeline.000015", "当前流水线不存在"),
  CREATE_BRANCH_ERROR(400, "Pipeline.000016", "创建分支失败"),
  PIPELINE_RUNNING_NOT_DELETE_PUBLISH(500, "Pipeline.000017", "流水线正在运行，无法删除发布分支"),
  PIPELINE_NOT_BELONG_SERVICE(400, "Pipeline.000018", "流水线不属于当前服务"),
  RUN_PIPELINE_ERROR(500, "Pipeline.000019", "流水线任务调度失败,请稍后重试"),
  SERVICE_CREATE_PUBLISH_PIPELINE(500, "Pipeline.000020", "当前服务未创建发布流水线，无法推送发布"),
  PIPELINE_IS_RUNNING(500, "Pipeline.000021", "流水线正在运行中，无法再次执行"),
  /*==================Service服务==================*/
  NOT_FIND_REPO_CONFIG(400, "Service.000001", "git访问失败，请检查Git配置是否正确"),
  REPO_NOT_EXIST(400, "Service.000002", "未发现用户可访问的仓库列表"),
  SERVICE_EXIST_PIPELINE(400, "Service.000003", "服务下存在流水线无法删除"),
  SERVICE_EXIST_FEATURE(400, "Service.000004", "服务下存在测试集无法删除"),
  GIT_NO_PERMISSION(400, "Service.000005", "配置的用户token无权限访问"),
  USER_NO_PERMISSION(400, "Service.000006", "用户未被授权访问当前git地址"),
  MAVEN_NOT_CONFIG(400, "Service.000007", "系统未配置maven仓库地址，请先配置再尝试生成"),
  GENERATE_VERSION_EXIST(400, "Service.000008", "构建Maven二方包失败，版本号已存在"),
  SERVICE_GENERATE_NAME_EMPTY(400, "Service.000009", "[%s]接口转化的类名、方法名为空"),
  SERVICE_GENERATE_BODY_NAME_EMPTY(400, "Service.000010", "[%s]接口转化的请求体类名为空"),
  SERVICE_GENERATE_BODY_PARAM_NAME_EMPTY(400, "Service.000011", "[%s]接口请求参数类名为空"),
  SERVICE_GENERATE_RESPONSE_PARAM_NAME_EMPTY(400, "Service.000012", "[%s]接口响应参数类名为空"),
  SERVICE_GENERATE_RESPONSE_NAME_EMPTY(400, "Service.000013", "[%s]接口转化的响应类名为空"),
  SERVICE_API_NOT_FIND(400, "Service.000014", "未发现服务API列表"),
  LOAD_CLIENT_BUILD_TOOL_ERROR(200, "Service.000015", "加载client节点构建工具失败,节点列表: %s"),
  SERVICE_EXIST_API(400, "Service.000016", "服务下存在API无法删除"),
  SERVICE_EXIST_TEMPLATE(400, "Service.000017", "服务下存在用例模版无法删除"),
  /*==================用例服务==================*/
  COMPARE_ERROR(403, "Feature.000002", "feature compare error"),
  EXECUTE_POINT_NOT_FIND(404, "Feature.000003", "can not find execute point"),
  FEATURE_NOT_FIND(404, "Feature.000004", "can not find feature"),
  SYSTEM_EXECUTE_ERROR(400, "Feature.000101", "unknown error occur when execute feature "),
  SUB_FEATURE_EXIST(400, "Feature.000005", "存在子用例不能删除目录"),
  PARSE_PLUGIN_ERROR(400, "Feature.000006", "解析插件错误"),
  TEMPLATE_NOT_FIND(404, "Feature.000007", "未找到模版"),
  BATCH_UPDATE_FEATURE_POINTS_ERROR(500, "Feature.000008", "批量更新用例执行点失败"),
  NO_TEMPLATE_IN_PLUGIN(400, "Feature.000009", "在插件中未找到模版信息，导入失败"),
  TASK_NOT_FIND(400, "Feature.000010", "任务不存在"),
  TASK_RUN_FAILED(500, "Feature.000011", "用例任务调度失败，请稍后重试"),
  FEATURE_RUN_FAILED(500, "Feature.000012", "用例执行调度失败，请稍后重试"),
  COPY_CASE_FAILED(500, "Feature.000013", "复制用例失败"),
  CASE_EXIST_FEATURE(500, "Feature.000014", "测试集下存在用例不允许删除"),
  /*==================需求缺陷管理======================*/
  BUG_NOT_EXIST(400, "Bug.000001", "缺陷不存在"),
  BUG_HAS_BIND_BRANCH(400, "Bug.000002", "缺陷已经关联了分支无法删除"),
  DEMAND_HAS_BIND_BRANCH(400, "Demand.000001", "需求已经关联了分支无法删除"),
  DEMAND_CREATE_ERROR(500, "Demand.000002", "创建需求失败"),
  DEMAND_NOT_EXIST(400, "Demand.000004", "需求不存在"),
  SPACE_NOT_EXIST(400, "Space.000001", "空间不存在"),
  SPACE_HAS_NOT_COMPLETE_ITERATION(400, "Space.000002", "空间下有未完成的迭代无法删除"),
  SPACE_HAS_NOT_COMPLETE_DEMAND(400, "Space.000003", "空间下有未完成的需求"),
  SPACE_HAS_NOT_COMPLETE_BUG(400, "Space.000004", "空间下有未完成的bug无法删除"),
  ITERATION_NOT_EXIST(400, "Iteration.000001", "迭代不存在"),
  ITERATION_HAS_NOT_COMPLETE_DEMAND(400, "Iteration.000002", "迭代下有未完成的需求"),
  ITERATION_HAS_NOT_COMPLETE_BUG(400, "Iteration.00003", "迭代下有未完成的bug无法删除"),
  ITERATION_IS_UNCHANGEABLE(400, "Iteration.000004", "迭代已完成不可新增"),
  /*==================Client端构建运行==================*/
  NOT_FIND_JAR(500, "Client.000001", "构建时未发现JAR包"),
  UNKNOWN_EXECUTE_TYPE(500, "Client.000002", "未找到节点执行类型"),
  RUN_DEPLOY_ERROR(500, "Client.000003", "部署节点失败"),
  MERGE_CODE_ERROR(500, "Client.000004", "合并代码失败"),
  BRANCH_NOT_DIFF(500, "Client.000005", "合并分支%s与master不存在差异，不能发布"),
  /*==================权限校验==================*/
  USER_TOKEN_INVALID(403,"Auth.000001","token无效"),
  USER_NOT_FIND(400,"Auth.000002","用户不存在"),
  USER_PASSWORD_ERROR(400,"Auth.000003","用户名或者密码错误"),
  USER_WAS_BLOCKED(400,"Auth.000004","用户被冻结"),
  USER_NOT_HAVE_PERMISSION(400,"Auth.000005","用户没有权限"),
  RESOURCE_IS_BIND(400,"Auth.000006","资源被绑定无法删除"),
  ROLE_IS_BIND(400,"Auth.000007","角色被绑定无法删除"),
  GROUP_EXIST_USER(500,"Auth.000008","组织下存在用户无法删除"),
  ;

  ErrorCode(int httpStatus, String code, String message) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  private final String code;
  private final String message;
  private final int httpStatus;

}
