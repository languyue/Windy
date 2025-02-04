package com.zj.service.entity;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImportApiResult {

    private List<ServiceApiBO> apiList;
}
