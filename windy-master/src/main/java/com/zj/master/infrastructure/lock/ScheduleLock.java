package com.zj.master.infrastructure.lock;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.po.pipeline.OptimisticLock;
import com.zj.domain.mapper.pipeline.OptimisticLockMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 基于 optimistic_lock 表的租约式互斥锁，本机持有一份已获锁的缓存。
 *
 * @author guyuelan
 */
@Slf4j
@Component
public class ScheduleLock implements IScheduleLock, DisposableBean {

  /**
   * 锁租期，单位小时
   */
  private static final int LEASE_HOURS = 1;

  /**
   * 距租约到期不足该时间才允许续期，单位毫秒
   */
  private static final long RENEW_AHEAD = 10 * 1000L;

  private final OptimisticLockMapper lockMapper;

  /**
   * 只记录本机已经拿到的锁，停机时据此释放
   */
  private final Map<String, OptimisticLock> holdLocks = new ConcurrentHashMap<>();

  public ScheduleLock(OptimisticLockMapper lockMapper) {
    this.lockMapper = lockMapper;
  }

  @Override
  public boolean hasLock(String bizCode) {
    OptimisticLock lock = holdLocks.get(bizCode);
    if (Objects.isNull(lock)) {
      return false;
    }
    return Objects.equals(IpUtils.getLocalIP(), lock.getIp())
        && lock.getEndTime() > System.currentTimeMillis();
  }

  @Override
  public boolean tryLock(String bizCode) {
    try {
      if (hasLock(bizCode)) {
        return true;
      }

      OptimisticLock lock = selectLock(bizCode);
      if (Objects.isNull(lock)) {
        return createLock(bizCode);
      }

      long delta = lock.getEndTime() - System.currentTimeMillis();
      if (delta > RENEW_AHEAD) {
        //租期还早，说明锁在别的节点手里
        return false;
      }

      boolean result = renewLock(bizCode, lock);
      log.info("lock bizCode={} result={}", bizCode, result);
      if (result) {
        holdLocks.put(bizCode, lock);
      }
      return result;
    } catch (Exception e) {
      log.error("try schedule lock error bizCode={}", bizCode, e);
      return false;
    }
  }

  private boolean createLock(String bizCode) {
    OptimisticLock lock = buildLock(bizCode);
    try {
      boolean lockResult = lockMapper.insert(lock) > 0;
      log.info("lock result={} bizCode={}", lockResult, bizCode);
      if (lockResult) {
        //创建即持有，否则下一轮会因为租期未满而把自己的锁当成别人的
        holdLocks.put(bizCode, lock);
      }
      return lockResult;
    } catch (DuplicateKeyException e) {
      log.info("other node lock the bizCode={}", bizCode);
      return false;
    }
  }

  private boolean renewLock(String bizCode, OptimisticLock lock) {
    Long lockVersion = lock.getVersion();
    DateTime dateNow = new DateTime();
    lock.setStartTime(dateNow.getMillis());
    lock.setEndTime(dateNow.plusHours(LEASE_HOURS).getMillis());
    lock.setIp(IpUtils.getLocalIP());
    lock.setNodeName(IpUtils.getHostName());
    lock.setVersion(lockVersion + 1);
    return lockMapper.update(lock, Wrappers.lambdaUpdate(OptimisticLock.class)
        .eq(OptimisticLock::getBizCode, bizCode)
        .eq(OptimisticLock::getVersion, lockVersion)) > 0;
  }

  private OptimisticLock selectLock(String bizCode) {
    List<OptimisticLock> locks = lockMapper.selectList(Wrappers.lambdaQuery(OptimisticLock.class)
        .eq(OptimisticLock::getBizCode, bizCode));
    return CollectionUtils.isEmpty(locks) ? null : locks.get(0);
  }

  private OptimisticLock buildLock(String bizCode) {
    OptimisticLock lock = new OptimisticLock();
    lock.setBizCode(bizCode);
    lock.setIp(IpUtils.getLocalIP());
    lock.setVersion(1L);
    lock.setNodeName(IpUtils.getHostName());

    DateTime dateTime = new DateTime();
    lock.setStartTime(dateTime.getMillis());
    lock.setEndTime(dateTime.plusHours(LEASE_HOURS).getMillis());
    return lock;
  }

  @Override
  public void destroy() {
    List<String> lockKeys = new ArrayList<>(holdLocks.keySet());
    lockKeys.removeIf(key -> !hasLock(key));
    if (CollectionUtils.isEmpty(lockKeys)) {
      return;
    }

    boolean clearResult = lockMapper.delete(Wrappers.lambdaQuery(OptimisticLock.class)
        .in(OptimisticLock::getBizCode, lockKeys)) > 0;
    log.info("clear biz code keys ={} result={}", lockKeys, clearResult);
  }
}
