package com.zj.demand.adapter;

import com.zj.common.port.relation.IRelationTargetReader;
import com.zj.common.port.relation.RelationTargetView;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.repository.demand.IBugQueryRepository;
import com.zj.domain.repository.demand.IDemandQueryRepository;
import com.zj.domain.repository.demand.IWorkTaskQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系目标只读检索端口的实现。
 *
 * <p>需求域自己读自己的三张表，把结果收敛成最小视图交给调用方；
 * 调用方（流水线域）不再直接依赖需求域的仓储与 BO。</p>
 *
 * @author guyuelan
 */
@Slf4j
@Service
public class RelationTargetReader implements IRelationTargetReader {

  private final IBugQueryRepository bugRepository;
  private final IDemandQueryRepository demandRepository;
  private final IWorkTaskQueryRepository workTaskRepository;

  public RelationTargetReader(IBugQueryRepository bugRepository, IDemandQueryRepository demandRepository,
      IWorkTaskQueryRepository workTaskRepository) {
    this.bugRepository = bugRepository;
    this.demandRepository = demandRepository;
    this.workTaskRepository = workTaskRepository;
  }

  @Override
  public List<RelationTargetView> searchByName(String name) {
    List<RelationTargetView> targets = new ArrayList<>();
    try {
      List<BugBO> bugs = bugRepository.getBugsFuzzyByName(name);
      bugs.forEach(bug -> targets.add(buildView(bug.getBugId(), RelationType.BUG, bug.getBugName())));

      List<DemandBO> demands = demandRepository.getDemandsByFuzzName(name);
      demands.forEach(demand -> targets.add(
          buildView(demand.getDemandId(), RelationType.DEMAND, demand.getDemandName())));

      List<WorkTaskBO> works = workTaskRepository.getWorkTaskByName(name);
      works.forEach(work -> targets.add(buildView(work.getTaskId(), RelationType.WORK, work.getTaskName())));
      return targets;
    } catch (Exception e) {
      log.info("query related demand or bug or work error", e);
    }
    return new ArrayList<>();
  }

  private RelationTargetView buildView(String relationId, RelationType relationType, String relationName) {
    RelationTargetView view = new RelationTargetView();
    view.setRelationId(relationId);
    view.setRelationType(relationType.getType());
    view.setRelationName(relationName);
    return view;
  }
}
