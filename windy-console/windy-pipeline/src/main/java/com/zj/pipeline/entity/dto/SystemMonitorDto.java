package com.zj.pipeline.entity.dto;

import com.zj.contract.rpc.ClientCollectDto;
import com.zj.contract.rpc.MasterCollectDto;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class SystemMonitorDto {

  private List<ClientCollectDto> clients;

  private List<MasterCollectDto> masters;
}
