package com.zj.infrastructure.lock;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.infrastructure.entity.po.pipeline.OptimisticLock;
import com.zj.infrastructure.mapper.pipeline.OptimisticLockMapper;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * optimistic_lock 表的技术访问门面。
 *
 * <p>这张表是纯技术表，不属于任何业务聚合，所以不进 com.zj.domain.repository 的仓储接口；
 * 但 mapper 与 PO 也只能出现在 com.zj.infrastructure 包里，master 通过本类读写租约。</p>
 *
 * @author guyuelan
 */
@Component
public class OptimisticLockStore {

  private final OptimisticLockMapper lockMapper;

  public OptimisticLockStore(OptimisticLockMapper lockMapper) {
    this.lockMapper = lockMapper;
  }

  /**
   * 读取 bizCode 当前的租约，不存在返回 null
   */
  public LockLease get(String bizCode) {
    List<OptimisticLock> locks = lockMapper.selectList(Wrappers.lambdaQuery(OptimisticLock.class)
        .eq(OptimisticLock::getBizCode, bizCode));
    if (CollectionUtils.isEmpty(locks)) {
      return null;
    }
    return convert(locks.get(0));
  }

  /**
   * 创建租约，bizCode 已被别的节点占用时返回 null
   */
  public LockLease create(String bizCode, String ip, String nodeName, long startTime, long endTime) {
    OptimisticLock lock = new OptimisticLock();
    lock.setBizCode(bizCode);
    lock.setIp(ip);
    lock.setNodeName(nodeName);
    lock.setVersion(1L);
    lock.setStartTime(startTime);
    lock.setEndTime(endTime);
    try {
      if (lockMapper.insert(lock) <= 0) {
        return null;
      }
      return convert(lock);
    } catch (DuplicateKeyException e) {
      return null;
    }
  }

  /**
   * 按版本号续租，版本已被别人推进时返回 null
   */
  public LockLease renew(String bizCode, long expectedVersion, String ip, String nodeName, long startTime,
      long endTime) {
    OptimisticLock lock = new OptimisticLock();
    lock.setBizCode(bizCode);
    lock.setIp(ip);
    lock.setNodeName(nodeName);
    lock.setStartTime(startTime);
    lock.setEndTime(endTime);
    lock.setVersion(expectedVersion + 1);
    int updated = lockMapper.update(lock, Wrappers.lambdaUpdate(OptimisticLock.class)
        .eq(OptimisticLock::getBizCode, bizCode)
        .eq(OptimisticLock::getVersion, expectedVersion));
    return updated > 0 ? convert(lock) : null;
  }

  /**
   * 批量释放租约
   */
  public void release(List<String> bizCodes) {
    if (CollectionUtils.isEmpty(bizCodes)) {
      return;
    }
    lockMapper.delete(Wrappers.lambdaQuery(OptimisticLock.class).in(OptimisticLock::getBizCode, bizCodes));
  }

  private static LockLease convert(OptimisticLock lock) {
    LockLease lease = new LockLease();
    lease.setVersion(lock.getVersion());
    lease.setIp(lock.getIp());
    lease.setEndTime(lock.getEndTime());
    return lease;
  }

  /**
   * 租约快照，只含持锁判断需要的字段
   */
  @Data
  public static class LockLease {

    private Long version;

    private String ip;

    private Long endTime;
  }
}
