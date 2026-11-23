package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.contract.rpc.PageSize;
import com.zj.common.exception.ApiException;
import com.zj.contract.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.WorkTaskDto;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.repository.demand.IBusinessStatusQueryRepository;
import com.zj.domain.repository.demand.IWorkTaskCommandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class WorkTaskService {

    private final UniqueIdService uniqueIdService;
    private final IWorkTaskCommandRepository workTaskRepository;
    private final IBusinessStatusQueryRepository businessStatusRepository;
    private final IAuthService authService;

    public WorkTaskService(UniqueIdService uniqueIdService, IWorkTaskCommandRepository workTaskRepository,
                           IBusinessStatusQueryRepository businessStatusRepository, IAuthService authService) {
        this.uniqueIdService = uniqueIdService;
        this.workTaskRepository = workTaskRepository;
        this.businessStatusRepository = businessStatusRepository;
        this.authService = authService;
    }

    public WorkTaskBO createWorkTask(WorkTaskDto workTaskDto) {
        WorkTaskBO workTaskBO = OrikaUtil.convert(workTaskDto, WorkTaskBO.class);
        workTaskBO.setTaskId(uniqueIdService.getUniqueId());
        workTaskBO.setCreator(authService.getCurrentUserId());
        boolean result = workTaskRepository.createTask(workTaskBO);
        return result ? workTaskBO : null;
    }

    public Boolean updateWorkTask(WorkTaskDto workTaskDto) {
        WorkTaskBO workTask = workTaskRepository.getWorkTask(workTaskDto.getTaskId());
        boolean unchangeableStatus = businessStatusRepository.isUnchangeableStatus(workTask.getStatus(),
                BusinessStatusType.WORK.name());
        if (Objects.nonNull(workTaskDto.getStatus()) && unchangeableStatus) {
            log.info("work task status is unchangeable status= {}", workTask.getStatus());
            throw new ApiException(ErrorCode.STATUS_UNCHANGEABLE_ERROR);
        }
        return workTaskRepository.updateWorkTask(OrikaUtil.convert(workTaskDto, WorkTaskBO.class));
    }

    public PageSize<WorkTaskBO> getWorkTaskPage(Integer page, Integer size, String name, Integer status) {
        String userId = authService.getCurrentUserId();
        TaskQueryBO taskQueryBO =
                TaskQueryBO.builder().page(page).size(size).name(name).userId(userId).status(status).build();
        return workTaskRepository.getWorkTaskPage(taskQueryBO);
    }

    public WorkTaskBO getWorkTask(String taskId) {
        return workTaskRepository.getWorkTask(taskId);
    }

    public List<BusinessStatusBO> getWorkTaskStatuses() {
        return businessStatusRepository.getWorkTaskStatuses();
    }

    public Boolean deleteWorkTask(String taskId) {
        return workTaskRepository.deleteWorkTask(taskId);
    }
}
