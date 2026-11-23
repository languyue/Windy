package com.zj.contract.pipeline;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 环境接入参数的公共标记。
 *
 * <p>一个 environment 行按 {@code envType} 承载两种结构：K8S 集群参数或 SSH 主机参数。
 * 有了这个标记，{@code DeployEnvironmentBO} 就可以直接持有反序列化后的对象，
 * 而不是把 JSON 文本一路带到业务代码里再由调用方手工 parse。</p>
 *
 * @author guyuelan
 */
@JsonDeserialize(using = EnvAccessParamsDeserializer.class)
public interface EnvAccessParams {
}
