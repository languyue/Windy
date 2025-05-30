package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.demand.entity.BusinessDictionaryDto;
import com.zj.demand.entity.DemandDetailDto;
import com.zj.demand.entity.DemandDto;
import com.zj.demand.service.DemandService;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops")
public class DemandRest {

    private final DemandService demandService;

    public DemandRest(DemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping("/demands")
    public ResponseMeta<DemandBO> createDemand(@Validated(Create.class) @RequestBody DemandDto demandDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.createDemand(demandDto));
    }

    @PutMapping("/demand")
    public ResponseMeta<Boolean> updateDemand(@Validated(Update.class) @RequestBody DemandDto demandDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.updateDemand(demandDto));
    }

    @GetMapping("/demands")
    public ResponseMeta<PageSize<DemandBO>> getDemandPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                          @RequestParam(value = "name", required = false) String name,
                                                          @RequestParam(value = "spaceId", required = false) String spaceId,
                                                          @RequestParam(value = "iterationId", required = false) String iterationId,
                                                          @RequestParam(value = "acceptor", required = false) String acceptor,
                                                          @RequestParam(value = "status", required = false) Integer status,
                                                          @RequestParam(value = "type", required = false) Integer type) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandPage(page, size, name, status, spaceId,
                iterationId, acceptor, type));
    }

    @GetMapping("/iterations/{iterationId}/demands")
    public ResponseMeta<List<DemandBO>> getIterationDemands(@PathVariable("iterationId") String iterationId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getIterationDemands(iterationId));
    }

    @GetMapping("/user/demands")
    public ResponseMeta<PageSize<DemandBO>> getUserDemands(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                           @RequestParam(value = "status", required = false) Integer status,
                                                           @RequestParam(value = "name", required = false) String name) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getUserDemands(page, size, status, name));
    }

    @GetMapping("/demand/tags")
    public ResponseMeta<List<BusinessDictionaryDto>> getDemandTags() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandTags());
    }

    @GetMapping("/demand/statuses")
    public ResponseMeta<List<BusinessDictionaryDto>> getDemandStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemandStatuses());
    }

    @GetMapping("/demand/customer/values")
    public ResponseMeta<List<BusinessDictionaryDto>> getCustomerValues() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getCustomerValues());
    }

    @GetMapping("/demands/{demandId}")
    public ResponseMeta<DemandDetailDto> getDemand(@PathVariable("demandId") String demandId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.getDemand(demandId));
    }

    @DeleteMapping("/demands/{demandId}")
    public ResponseMeta<Boolean> deleteDemand(@PathVariable("demandId") String demandId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, demandService.deleteDemand(demandId));
    }
}
