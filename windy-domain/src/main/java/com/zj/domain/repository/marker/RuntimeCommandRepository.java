package com.zj.domain.repository.marker;

/**
 * 运行态域的写接口，唯一写 owner 是 windy-master。
 * windy-console 只允许依赖对应的 Query 接口，写入一律走 master 命令。
 *
 * @author guyuelan
 */
public interface RuntimeCommandRepository extends CommandRepository {
}
