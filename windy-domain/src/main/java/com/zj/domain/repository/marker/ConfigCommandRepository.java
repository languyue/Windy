package com.zj.domain.repository.marker;

/**
 * 配置域与业务域的写接口，唯一写 owner 是 windy-console。
 * windy-master 只允许依赖对应的 Query 接口。
 *
 * @author guyuelan
 */
public interface ConfigCommandRepository extends CommandRepository {
}
