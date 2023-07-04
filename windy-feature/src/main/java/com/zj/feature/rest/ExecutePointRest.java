package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecutePointVo;
import com.zj.common.model.PageSize;
import com.zj.feature.service.ExecutePointService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class ExecutePointRest {
    private ExecutePointService executePointService;

    public ExecutePointRest(ExecutePointService executePointService) {
        this.executePointService = executePointService;
    }

    @PostMapping("/batch/execute/point")
    public ResponseMeta<Void> batchAddExecutePoint(@Valid @RequestBody List<ExecutePointVo> executePointDtos){
        executePointService.batchAddTestFeature(executePointDtos);
        return new ResponseMeta<>(ErrorCode.SUCCESS);
    }

    @PostMapping("/execute/point")
    public ResponseMeta<String> createExecutePoint(@Valid @RequestBody ExecutePointVo executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.createExecutePoint(executePointDTO));
    }

    @PutMapping("/execute/point")
    public ResponseMeta<String> updateExecutePoint(@Valid @RequestBody ExecutePointVo executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.updateExecutePoint(executePointDTO));
    }

    @DeleteMapping("/execute/point/{executePointId}")
    public ResponseMeta<Boolean> deleteExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, executePointService.deleteByExecutePointId(executePointId));
    }

    @DeleteMapping("/{featureId}/execute/points")
    public ResponseMeta<Integer> deleteExecutePointByFeatureId(@PathVariable("featureId") String featureId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.deleteByFeatureId(featureId));
    }

    @GetMapping("/execute/point/{executePointId}")
    public ResponseMeta<ExecutePointVo> queryExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.getExecutePoint(executePointId));
    }

    @GetMapping("/{featureId}/execute/points")
    public ResponseMeta<PageSize<ExecutePointVo>> queryExecutePoint(@PathVariable("featureId") String featureId, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.queryExecutePointPage(featureId, page, size));
    }

    @GetMapping("/execute/operators")
    public ResponseMeta<List<String>> queryExecutePointOperators() {
        return new ResponseMeta<List<String>>(ErrorCode.SUCCESS, executePointService.queryExecutePointOperators());
    }
}
