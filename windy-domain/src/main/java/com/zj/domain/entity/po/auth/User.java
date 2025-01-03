package com.zj.domain.entity.po.auth;

import lombok.Data;

@Data
public class User {

    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 密码加密盐
     */
    private String salt;

    /**
     * 组织ID
     */
    private String groupId;

    /**
     * 用户状态 1 正常 0 冻结
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
