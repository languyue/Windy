package com.zj.master.rest;

import com.zj.contract.exception.ErrorCode;
import com.zj.contract.rpc.MasterCollectDto;
import com.zj.contract.rpc.ResponseMeta;
import com.zj.master.service.MasterMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@RestController
@RequestMapping("/v1/devops/master")
public class MasterMonitorRest {

  private final MasterMonitor masterMonitor;

  public MasterMonitorRest(MasterMonitor masterMonitor) {
    this.masterMonitor = masterMonitor;
  }

  @GetMapping("/instance")
  public ResponseMeta<MasterCollectDto> getInstance() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, masterMonitor.getInstanceInfo());
  }
}
