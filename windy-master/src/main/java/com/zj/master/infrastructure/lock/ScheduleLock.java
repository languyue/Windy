package com.zj.master.infrastructure.lock;

import com.zj.common.utils.IpUtils;
import com.zj.infrastructure.lock.OptimisticLockStore;
import com.zj.infrastructure.lock.OptimisticLockStore.LockLease;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 基于 optimistic_lock 表的租约式互斥锁，本机持有一份已获锁的缓存。
 *
 * <p>表的读写细节收在 {@link OptimisticLockStore}，本类只负责租约策略。</p>
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

  private final OptimisticLockStore lockStore;

  /**
   * 只记录本机已经拿到的锁，停机时据此释放
   */
  private final Map<String, LockLease> holdLocks = new ConcurrentHashMap<>();

  public ScheduleLock(OptimisticLockStore lockStore) {
    this.lockStore = lockStore;
  }

  @Override
  public boolean hasLock(String bizCode) {
    LockLease lease = holdLocks.get(bizCode);
    if (Objects.isNull(lease)) {
      return false;
    }
    return Objects.equals(IpUtils.getLocalIP(), lease.getIp())
        && lease.getEndTime() > System.currentTimeMillis();
  }

  @Override
  public boolean tryLock(String bizCode) {
    try {
      if (hasLock(bizCode)) {
        return true;
      }

      LockLease lease = lockStore.get(bizCode);
      if (Objects.isNull(lease)) {
        return createLock(bizCode);
      }

      long delta = lease.getEndTime() - System.currentTimeMillis();
      if (delta > RENEW_AHEAD) {
        //租期还早，说明锁在别的节点手里
        return false;
      }

      boolean result = renewLock(bizCode, lease);
      log.info("lock bizCode={} result={}", bizCode, result);
      return result;
    } catch (Exception e) {
      log.error("try schedule lock error bizCode={}", bizCode, e);
      return false;
    }
  }

  private boolean createLock(String bizCode) {
    DateTime dateNow = new DateTime();
    LockLease lease = lockStore.create(bizCode, IpUtils.getLocalIP(), IpUtils.getHostName(),
        dateNow.getMillis(), dateNow.plusHours(LEASE_HOURS).getMillis());
    if (Objects.isNull(lease)) {
      log.info("other node lock the bizCode={}", bizCode);
      return false;
    }
    log.info("lock result=true bizCode={}", bizCode);
    //创建即持有，否则下一轮会因为租期未满而把自己的锁当成别人的
    holdLocks.put(bizCode, lease);
    return true;
  }

  private boolean renewLock(String bizCode, LockLease lease) {
    DateTime dateNow = new DateTime();
    LockLease renewed = lockStore.renew(bizCode, lease.getVersion(), IpUtils.getLocalIP(),
        IpUtils.getHostName(), dateNow.getMillis(), dateNow.plusHours(LEASE_HOURS).getMillis());
    if (Objects.isNull(renewed)) {
      return false;
    }
    holdLocks.put(bizCode, renewed);
    return true;
  }

  @Override
  public void destroy() {
    List<String> lockKeys = new ArrayList<>(holdLocks.keySet());
    lockKeys.removeIf(key -> !hasLock(key));
    if (CollectionUtils.isEmpty(lockKeys)) {
      return;
    }

    lockStore.release(lockKeys);
    log.info("release biz code keys ={}", lockKeys);
  }
}
