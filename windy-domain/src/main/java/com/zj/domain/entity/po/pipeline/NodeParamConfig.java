package com.zj.domain.entity.po.pipeline;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 节点参数配置，对应 node_config 表
 */
@Data
@TableName("node_config")
public class NodeParamConfig {

    private Long id;

    /**
     * 配置Id
     * */
    private String configId;

    /**
     * 配置名称
     * */
    private String configName;

    /**
     * 配置类型
     * */
    private Integer type;

    /**
     * 配置信息
     * */
    private String configDetail;

    /**
     * 排序
     * */
    private Integer sortOrder;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;
}
