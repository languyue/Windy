package com.zj.contract.pipeline;

import lombok.Data;

@Data
public class SSHParams implements EnvAccessParams {
    /**
     * 文件运行目录
     */
    private String remotePath;

    /**
     * sshIP
     */
    private String sshIp;

    /**
     * ssh端口
     */
    private Integer sshPort;

    /**
     * ssh 用户
     */
    private String user;

    /**
     * ssh密码
     */
    private String password;
}
