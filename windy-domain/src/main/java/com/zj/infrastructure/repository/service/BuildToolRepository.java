package com.zj.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.BuildToolBO;
import com.zj.infrastructure.entity.po.service.BuildTool;
import com.zj.infrastructure.mapper.service.IBuildToolMapper;
import com.zj.domain.repository.service.IBuildToolCommandRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuildToolRepository extends ServiceImpl<IBuildToolMapper, BuildTool> implements IBuildToolCommandRepository {
    @Override
    public boolean saveBuildTool(BuildToolBO buildToolBO) {
        BuildTool buildTool = OrikaUtil.convert(buildToolBO, BuildTool.class);
        buildTool.setCreateTime(System.currentTimeMillis());
        buildTool.setUpdateTime(System.currentTimeMillis());
        return save(buildTool);
    }

    @Override
    public boolean deleteBuildTool(String buildToolId) {
        return remove(Wrappers.lambdaQuery(BuildTool.class).eq(BuildTool::getToolId, buildToolId));
    }

    @Override
    public List<BuildToolBO> getBuildToolList() {
        List<BuildTool> buildTools = list();
        return OrikaUtil.convertList(buildTools, BuildToolBO.class);
    }

    @Override
    public boolean updateBuildTool(BuildToolBO buildToolBO) {
        BuildTool buildTool = OrikaUtil.convert(buildToolBO, BuildTool.class);
        buildTool.setUpdateTime(System.currentTimeMillis());
        return update(buildTool, Wrappers.lambdaUpdate(BuildTool.class).eq(BuildTool::getToolId, buildTool.getToolId()));
    }
}
