package com.zj.domain.repository.marker;

/**
 * 写接口标记根。所有 *CommandRepository 都必须继承本接口，
 * 架构守卫据此判定某个进程是否越界写了不属于自己的表。
 *
 * @author guyuelan
 */
public interface CommandRepository {
}
