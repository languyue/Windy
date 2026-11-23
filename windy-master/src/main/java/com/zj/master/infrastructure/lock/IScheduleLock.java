package com.zj.master.infrastructure.lock;

/**
 * 定时任务互斥锁。
 *
 * 它是调度进程的基础设施而不是领域概念：锁的租期、续期与停机释放都只在 master 侧有意义，
 * 因此从 windy-domain 的仓储层搬到这里。
 *
 * @author guyuelan
 */
public interface IScheduleLock {

  /**
   * 本机是否持有该业务编码的锁，并且仍在租期内
   *
   * @param bizCode 业务编码
   * @return 是否持有
   */
  boolean hasLock(String bizCode);

  /**
   * 尝试持有或续期该业务编码的锁
   *
   * @param bizCode 业务编码
   * @return 是否可以执行本机独占逻辑
   */
  boolean tryLock(String bizCode);
}
