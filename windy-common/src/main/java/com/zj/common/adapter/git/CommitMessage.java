package com.zj.common.adapter.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommitMessage {

    /**
     * 代码提交ID
     */
    @JsonProperty("id")
    private String commitId;

    /**
     * 代码提交短ID
     */
    @JsonProperty("short_id")
    private String shortId;

    /**
     * 提交的描述信息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 提交的用户
     */
    @JsonProperty("author_name")
    private String commitUser;

    /**
     * 提交时间
     */
    private Long commitTime;
}
