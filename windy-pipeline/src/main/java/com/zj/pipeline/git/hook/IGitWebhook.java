package com.zj.pipeline.git.hook;

import com.zj.pipeline.entity.vo.GitPushResultVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
public interface IGitWebhook {

  String platform();

  GitPushResultVo webhook(String data, HttpServletRequest request);
}
