package com.zj.contract.git;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GitAccessInfo {

    /**
     * 访问Git的API域名
     */
    @NotBlank
    private String gitDomain;

    /**
     * 访问git的token
     */
    private String accessToken;

    /**
     * 服务的拥有者(github、gitea需要)
     */
    @NotBlank
    private String owner;

    /**
     * git的类型
     */
    private String gitType;

    /**
     * 服务的git地址
     */
    private String gitUrl;

    /**
     * 三方git推送事件secret
     */
    private String pushSecret;

    /**
     * 主干分支名称
     */
    private String mainBranch;
    public String getGitServiceName() {
        if (gitUrl == null || gitUrl.trim().isEmpty()) {
            return "";
        }
        String[] strings = gitUrl.split("/");
        return strings[strings.length - 1].split("\\.")[0];
    }
}
