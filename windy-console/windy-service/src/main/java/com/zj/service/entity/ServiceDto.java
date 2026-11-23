package com.zj.service.entity;

import com.zj.contract.pipeline.ServiceConfig;
import com.zj.domain.entity.vo.Create;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ServiceDto {

    private String serviceId;

    /**
     * 服务名
     * */
    private String serviceName;

    /**
     * 服务git地址
     * */
    private String gitUrl;

    /**
     * 服务描述
     * */
    private String description;

    /**
     * 服务拥有者
     * */
    private String owner;

    /**
     * 服务优先级，用来服务列表排序
     * */
    private Integer priority;

    /**
     * 服务部署容器配置
     */
    @NotNull(groups = Create.class)
    private ServiceConfig serviceConfig;

    /**
     * 创建时间
     */
    private Long createTime;

}
