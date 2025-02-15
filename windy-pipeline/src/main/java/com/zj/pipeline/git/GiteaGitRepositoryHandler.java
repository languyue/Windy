package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.pipeline.entity.vo.BranchInfo;
import com.zj.pipeline.entity.vo.CreateBranchVo;
import com.zj.pipeline.entity.vo.GiteaRepositoryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Slf4j
@Service
public class GiteaGitRepositoryHandler implements IGitRepositoryHandler {

  private final GitRequestProxy gitRequestProxy;

  public GiteaGitRepositoryHandler(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
  }

  private Map<String, String> getTokenHeader(GitAccessInfo gitAccessInfo) {
    Map<String, String> header = new HashMap<>();
    String accessToken = gitAccessInfo.getAccessToken();
    header.put("Private-Token", accessToken);
    return header;
  }

  @Override
  public String gitType() {
    return GitType.Gitea.name();
  }

  @Override
  public void createBranch(String branchName, GitAccessInfo accessInfo) {
    String owner = accessInfo.getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches", owner, accessInfo.getGitServiceName());
    CreateBranchVo createBranchVO = new CreateBranchVo();
    createBranchVO.setBranchName(branchName);
    String result = gitRequestProxy.post(gitPath, JSON.toJSONString(createBranchVO), getTokenHeader(accessInfo));
    log.info("gitea create branch result = {}", result);
    BranchInfo branchInfo = JSON.parseObject(result, BranchInfo.class);
    if (Objects.isNull(branchInfo) || !Objects.equals(branchInfo.getName(), branchName)){
      throw new ApiException(ErrorCode.CREATE_BRANCH_ERROR);
    }

  }

  @Override
  public void deleteBranch(String branchName, GitAccessInfo accessInfo) {
    String owner = accessInfo.getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches/%s", owner, accessInfo.getGitServiceName(),
        branchName);
    gitRequestProxy.delete(gitPath, getTokenHeader(accessInfo));
  }

  @Override
  public void checkRepository(GitAccessInfo accessInfo) {
    String result = gitRequestProxy.get("/api/v1/user/repos", getTokenHeader(accessInfo));
    log.info("query repository result ={}", result);
    List<GiteaRepositoryVo> repositories = JSON.parseArray(result, GiteaRepositoryVo.class);
    if (CollectionUtils.isEmpty(repositories)) {
      throw new ApiException(ErrorCode.REPO_NOT_EXIST);
    }

    Optional<GiteaRepositoryVo> optional = repositories.stream()
        .filter(repo -> Objects.equals(repo.getName(), accessInfo.getGitServiceName())).findAny();
    if (!optional.isPresent()) {
      throw new ApiException(ErrorCode.USER_NO_PERMISSION);
    }

    boolean permission = optional.get().getPermissions().checkPermission();
    if (!permission) {
      throw new ApiException(ErrorCode.GIT_NO_PERMISSION);
    }
  }

  @Override
  public List<String> listBranch(GitAccessInfo accessInfo) {
    String owner = accessInfo.getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches", owner, accessInfo.getGitServiceName());
    String result = gitRequestProxy.get(gitPath, getTokenHeader(accessInfo));
    List<BranchInfo> branches = JSON.parseArray(result, BranchInfo.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    log.info("get list={}", result);
    return branches.stream().map(BranchInfo::getName)
        .filter(branch -> !branch.startsWith("temp_"))
        .collect(Collectors.toList());
  }
}
