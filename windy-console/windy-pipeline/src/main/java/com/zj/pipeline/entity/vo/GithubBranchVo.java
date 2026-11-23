package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubBranchVo {

    private String name;

    @JsonProperty("protected")
    private Boolean isProtected;
}
