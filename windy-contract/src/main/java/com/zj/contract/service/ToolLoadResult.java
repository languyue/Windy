package com.zj.contract.service;

import lombok.Data;

@Data
public class ToolLoadResult {
    /**
     * 是否加载成功
     */
    private Boolean success;

    /**
     * 节点IP
     */
    private String nodeIP;
}
