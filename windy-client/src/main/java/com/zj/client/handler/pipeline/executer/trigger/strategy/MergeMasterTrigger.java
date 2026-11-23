package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.GitMeta;
import com.zj.client.handler.pipeline.executer.vo.MergeStatus;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.contract.pipeline.node.RefreshContext;
import com.zj.contract.pipeline.node.TaskNode;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.json.ContractJson;
import com.zj.contract.pipeline.node.MergeMasterContext;
import com.zj.common.exception.ApiException;
import com.zj.common.utils.GitUtils;
import com.zj.common.enums.ExecuteType;
import com.zj.contract.enums.ProcessStatus;
import com.zj.contract.exception.ErrorCode;
import com.zj.common.exception.ExecuteException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 合并master
 */
@Slf4j
@Component
public class MergeMasterTrigger implements INodeTrigger<MergeMasterContext> {

    public static final String ORIGIN = "origin";
    public static final String MERGER_ERROR_TIPS = "merger error, find conflict files: ";
    public static final String REFS_HEADS = "refs/heads/";
    public static final String DELETE_BRANCH_FLAG = "1";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyy-MM-dd");
    private final IGitProcessor gitProcessor;
    private final GlobalEnvConfig globalEnvConfig;
    private final Map<String, MergeStatus> statusMap = new ConcurrentHashMap<>();

    public MergeMasterTrigger(IGitProcessor gitProcessor, GlobalEnvConfig globalEnvConfig) {
        this.gitProcessor = gitProcessor;
        this.globalEnvConfig = globalEnvConfig;
    }

    @Override
    public ExecuteType type() {
        return ExecuteType.MERGE;
    }

    @Override
    public void triggerRun(MergeMasterContext context, TaskNode taskNode) {
        log.info("start run merge code context = {}", ContractJson.write(context));
        statusMap.put(taskNode.getRecordId(), new MergeStatus(ProcessStatus.RUNNING.getType()));
        GitMeta gitMeta = OrikaUtil.convert(context, GitMeta.class);
        try {
            //1 拉取代码到本地
            String serviceName = GitUtils.getServiceFromUrl(context.getGitUrl());
            Git git = gitProcessor.pullCodeFromGit(gitMeta, context.getMainBranch(),
                    globalEnvConfig.getPipelineWorkspace(serviceName, context.getPipelineId()));

            //2 合并代码
            MergeCommand mergeCommand = git.merge();
            List<Ref> branchesRef = gitProcessor.getBranchesRef(git, context.getBranches());
            branchesRef.forEach(mergeCommand::include);
            MergeResult mergeResult = mergeCommand.call();

            //合并成功推送至远端master分支
            if (mergeResult.getMergeStatus().isSuccessful()) {
                log.info("merge success branches ={}", context.getBranches());
                push2Repository(context, gitMeta, git);
                createCodeTag(context, gitMeta, git);
                deleteBranch(context, gitMeta, git);
                statusMap.put(taskNode.getRecordId(), new MergeStatus(ProcessStatus.SUCCESS.getType()));
                return;
            }

            //存在冲突则回退本地的merge状态
            ObjectId objectId = git.getRepository().findRef(context.getMainBranch()).getObjectId();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(objectId.getName()).call();

            List<String> fileNames = new ArrayList<>();
            fileNames.add(MERGER_ERROR_TIPS);
            fileNames.addAll(mergeResult.getConflicts().keySet());
            statusMap.put(taskNode.getRecordId(),
                    new MergeStatus(ProcessStatus.FAIL.getType(), fileNames));
        } catch (Exception e) {
            log.warn("merger code error", e);
            statusMap.put(taskNode.getRecordId(),
                    new MergeStatus(ProcessStatus.FAIL.getType(), Collections.singletonList(e.getMessage())));
        }
    }

    private void createCodeTag(MergeMasterContext context, GitMeta gitMeta, Git git) {
        log.info("start create tag ={}", context.getTagName());
        try {
            String date = dateFormat.format(new Date());
            git.tag().setName(context.getTagName() + date).setMessage(context.getMessage()).call();
            git.push().setCredentialsProvider(getCredentialsProvider(gitMeta.getTokenName(),
                    gitMeta.getToken())).setPushTags().call();
        } catch (Exception e) {
            log.info("create code tag error", e);
        }
    }

    private void deleteBranch(MergeMasterContext context, GitMeta gitMeta, Git git) throws GitAPIException {
        if (!Objects.equals(context.getDeleteBranch(), DELETE_BRANCH_FLAG)) {
            return;
        }
        List<String> remoteRefNames = context.getBranches().stream()
                .map(branch -> REFS_HEADS + branch).collect(Collectors.toList());
        List<String> strings = git.branchDelete().setBranchNames(remoteRefNames.toArray(new String[]{}))
                .setForce(true).call();

        List<RefSpec> refSpecs = context.getBranches().stream().map(
                branch -> new RefSpec().setSource(null).setForceUpdate(true)
                        .setDestination(REFS_HEADS + branch)).collect(Collectors.toList());
        git.push().setRefSpecs(refSpecs).setRemote(ORIGIN)
                .setCredentialsProvider(getCredentialsProvider(gitMeta.getTokenName(),
                        gitMeta.getToken())).call();
        log.info("delete branches remoteRefNames={} result={}", remoteRefNames, strings);
    }

    private void push2Repository(MergeMasterContext context, GitMeta gitMeta, Git git) throws GitAPIException {
        // 推送合并后的代码到远程仓库的目标分支
        Iterable<PushResult> results = git.push().setRemote(ORIGIN).setRefSpecs(new RefSpec(context.getMainBranch() + ":" + context.getMainBranch()))
                .setCredentialsProvider(
                        getCredentialsProvider(gitMeta.getTokenName(), gitMeta.getToken())).call();
        Optional<RemoteRefUpdate> optional =
                StreamSupport.stream(results.spliterator(), false).map(PushResult::getRemoteUpdates)
                .flatMap(Collection::stream).filter(remoteRefUpdate -> {
                    log.info("Update status: {}, ref: {}, message: {}", remoteRefUpdate.getStatus(),
                            remoteRefUpdate.getRemoteName(), remoteRefUpdate.getMessage());
                    return !Objects.equals(remoteRefUpdate.getStatus(), Status.OK);
                }).findFirst();
        if (optional.isPresent()) {
            RemoteRefUpdate remoteRefUpdate =  optional.get();
            log.info("Update status: {}, ref: {}, message: {}", remoteRefUpdate.getStatus(),
                    remoteRefUpdate.getRemoteName(), remoteRefUpdate.getMessage());
            String errorMsg = ErrorCode.MERGE_CODE_ERROR.getMessage() + ":" + remoteRefUpdate.getStatus();
            throw new ExecuteException(errorMsg);
        }
    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider(String tokenName,
                                                                       String token) {
        return new UsernamePasswordCredentialsProvider(tokenName, token);
    }

    @Override
    public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
        MergeStatus mergeStatus = statusMap.get(taskNode.getRecordId());
        QueryResponseModel loopQueryResponse = new QueryResponseModel();
        loopQueryResponse.setStatus(mergeStatus.getStatus());
        loopQueryResponse.setData(Collections.singletonMap("status", loopQueryResponse.getStatus()));
        loopQueryResponse.setMessage(mergeStatus.getMessage());
        return loopQueryResponse;
    }
}
