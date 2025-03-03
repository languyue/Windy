package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.demand.entity.SpaceDto;
import com.zj.demand.service.SpaceService;
import com.zj.domain.entity.bo.demand.SpaceBO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops")
public class SpaceRest {

    private final SpaceService spaceService;

    public SpaceRest(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping("/spaces")
    public ResponseMeta<List<SpaceBO>> getSpaceList() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, spaceService.getSpaceList());
    }

    @PostMapping("/spaces")
    public ResponseMeta<SpaceBO> createSpace(@Validated(Create.class) @RequestBody SpaceDto spaceDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, spaceService.createSpace(spaceDto));
    }

    @PutMapping("/spaces/{spaceId}")
    public ResponseMeta<Boolean> updateSpace(@PathVariable("spaceId") String spaceId,
                                             @Validated(Update.class) @RequestBody SpaceDto spaceDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, spaceService.updateSpace(spaceId, spaceDto));
    }

    @DeleteMapping("/spaces/{spaceId}")
    public ResponseMeta<Boolean> deleteSpace(@PathVariable("spaceId") String spaceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, spaceService.deleteSpace(spaceId));
    }

    @GetMapping("/spaces/{spaceId}")
    public ResponseMeta<SpaceBO> getSpace(@PathVariable("spaceId") String spaceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, spaceService.getSpace(spaceId));
    }
}
